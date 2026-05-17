package com.example.diaryapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.model.DiaryEntry
import com.example.diaryapp.data.model.EmotionTag
import com.example.diaryapp.data.repository.DiaryRepository
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
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiaryUiState>(DiaryUiState.Idle)
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    private val _diaries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaries: StateFlow<List<DiaryEntry>> = _diaries.asStateFlow()

    private val _selectedEntry = MutableStateFlow<DiaryEntry?>(null)
    val selectedEntry: StateFlow<DiaryEntry?> = _selectedEntry.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val searchResults: StateFlow<List<DiaryEntry>> = _searchResults.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

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

    fun saveDiary(
        userId: String,
        title: String,
        content: String,
        date: String,
        emotion: EmotionTag?,
        existingId: String = "",
        imageUri: Uri? = null,
        existingImageUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = DiaryUiState.Loading
            val now = System.currentTimeMillis()

            var imageUrl = existingImageUrl
            if (imageUri != null && existingId.isNotEmpty()) {
                diaryRepository.uploadImage(userId, existingId, imageUri)
                    .onSuccess { imageUrl = it }
            }

            val entry = DiaryEntry(
                id = existingId,
                userId = userId,
                title = title,
                content = content,
                date = date,
                emotion = emotion,
                imageUrl = imageUrl,
                createdAt = if (existingId.isEmpty()) now else (_selectedEntry.value?.createdAt ?: now),
                updatedAt = now
            )

            val result = if (existingId.isEmpty()) {
                diaryRepository.saveDiary(entry).map { newId ->
                    if (imageUri != null) {
                        diaryRepository.uploadImage(userId, newId, imageUri)
                            .onSuccess { url ->
                                diaryRepository.updateDiary(entry.copy(id = newId, imageUrl = url))
                            }
                    }
                }
            } else {
                diaryRepository.updateDiary(entry)
            }

            result.onSuccess { _uiState.value = DiaryUiState.Success }
                .onFailure { _uiState.value = DiaryUiState.Error(it.message ?: "저장 실패") }
        }
    }

    fun deleteDiary(diaryId: String) {
        viewModelScope.launch {
            _uiState.value = DiaryUiState.Loading
            diaryRepository.deleteDiary(diaryId)
                .onSuccess { _uiState.value = DiaryUiState.Success }
                .onFailure { _uiState.value = DiaryUiState.Error(it.message ?: "삭제 실패") }
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
