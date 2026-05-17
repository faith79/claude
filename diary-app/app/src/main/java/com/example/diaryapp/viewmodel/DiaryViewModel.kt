package com.example.diaryapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.model.DiaryEntry
import com.example.diaryapp.data.model.EmotionTag
import com.example.diaryapp.data.model.WeatherTag
import com.example.diaryapp.data.repository.DiaryRepository
import com.example.diaryapp.data.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

sealed class DiaryUiState {
    object Idle : DiaryUiState()
    object Loading : DiaryUiState()
    object Success : DiaryUiState()
    data class Error(val message: String) : DiaryUiState()
}

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val imageCompressor: ImageCompressor
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiaryUiState>(DiaryUiState.Idle)
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    // Design Ref: §4.3 — Lottie 오버레이 제어용 별도 로딩 상태 (SC-11)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _diaries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaries: StateFlow<List<DiaryEntry>> = _diaries.asStateFlow()

    private val _selectedEntry = MutableStateFlow<DiaryEntry?>(null)
    val selectedEntry: StateFlow<DiaryEntry?> = _selectedEntry.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val searchResults: StateFlow<List<DiaryEntry>> = _searchResults.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // Plan SC: SC-04 Upsert — 해당 날짜 일기 존재 여부 반환
    suspend fun getEntryByDate(userId: String, date: String): DiaryEntry? =
        diaryRepository.getDiaryByDate(userId, date)

    fun loadMonth(userId: String, yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
        viewModelScope.launch {
            try {
                diaryRepository.getDiariesByMonth(userId, yearMonth).collect {
                    _diaries.value = it
                }
            } catch (e: Exception) {
                _uiState.value = DiaryUiState.Error(e.message ?: "데이터 로드 실패")
            }
        }
    }

    fun loadDiaryByDate(userId: String, date: String) {
        viewModelScope.launch {
            _selectedEntry.value = diaryRepository.getDiaryByDate(userId, date)
        }
    }

    // Design Ref: §4.3 — 압축 후 다중 업로드 + weather 저장 (SC-03, SC-05)
    fun saveDiary(
        userId: String,
        content: String,
        date: String,
        emotion: EmotionTag?,
        weather: WeatherTag?,
        existingId: String = "",
        newImageUris: List<Uri> = emptyList(),
        existingImageUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = DiaryUiState.Loading
            val now = System.currentTimeMillis()

            runCatching {
                // 신규 이미지 압축 후 업로드
                val uploadedUrls = if (newImageUris.isNotEmpty()) {
                    val tempId = existingId.ifEmpty { "temp_${now}" }
                    val compressed = newImageUris.map { imageCompressor.compress(it) }
                    diaryRepository.uploadImages(userId, tempId, compressed).getOrThrow()
                } else emptyList()

                val finalUrls = existingImageUrls + uploadedUrls

                val entry = DiaryEntry(
                    id = existingId,
                    userId = userId,
                    content = content,
                    date = date,
                    emotion = emotion,
                    weather = weather,
                    imageUrls = finalUrls,
                    createdAt = if (existingId.isEmpty()) now else (_selectedEntry.value?.createdAt ?: now),
                    updatedAt = now
                )

                if (existingId.isEmpty()) {
                    val newId = diaryRepository.saveDiary(entry).getOrThrow()
                    // 업로드된 URL이 있으면 실제 diaryId로 재저장 불필요 (URL은 이미 확정)
                    if (finalUrls.isNotEmpty()) {
                        diaryRepository.updateDiary(entry.copy(id = newId)).getOrThrow()
                    }
                } else {
                    diaryRepository.updateDiary(entry).getOrThrow()
                }
            }.onSuccess {
                _uiState.value = DiaryUiState.Success
            }.onFailure {
                _uiState.value = DiaryUiState.Error(it.message ?: "저장 실패")
            }

            _isLoading.value = false
        }
    }

    // Plan SC: SC-09 — Firestore 삭제 성공 후 Storage 삭제 (순서 보장)
    fun deleteDiary(entry: DiaryEntry) {
        viewModelScope.launch {
            _uiState.value = DiaryUiState.Loading
            diaryRepository.deleteDiaryWithImages(entry)
                .onSuccess { _uiState.value = DiaryUiState.Success }
                .onFailure { _uiState.value = DiaryUiState.Error(it.message ?: "삭제 실패") }
        }
    }

    // Plan SC: SC-10 — 수정 화면에서 이미지 개별 삭제 시 Storage 즉시 삭제
    fun removeImage(imageUrl: String) {
        viewModelScope.launch {
            diaryRepository.deleteImage(imageUrl)
                .onFailure {
                    _uiState.value = DiaryUiState.Error("이미지 삭제 실패: ${it.message}")
                }
        }
    }

    fun searchDiaries(userId: String, query: String) {
        if (query.isBlank()) { _searchResults.value = emptyList(); return }
        viewModelScope.launch {
            _searchResults.value = diaryRepository.searchDiaries(userId, query)
        }
    }

    fun resetState() { _uiState.value = DiaryUiState.Idle }
    fun clearSelectedEntry() { _selectedEntry.value = null }
}
