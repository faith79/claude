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

    // Design Ref: joyary-upgrade §5.3 — 상세보기 일기 로딩 상태 (G-01 fix)
    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading.asStateFlow()

    private val _diaries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaries: StateFlow<List<DiaryEntry>> = _diaries.asStateFlow()

    private val _selectedEntry = MutableStateFlow<DiaryEntry?>(null)
    val selectedEntry: StateFlow<DiaryEntry?> = _selectedEntry.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val searchResults: StateFlow<List<DiaryEntry>> = _searchResults.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // Design Ref: joyary-upgrade-v7 §2.1 — TTL 래퍼 + 24h 자동 만료 캐시 (FR-03, FR-04)
    private data class CachedValue<T>(val data: T, val cachedAt: Long = System.currentTimeMillis())
    private val TTL_MS = 24L * 60 * 60 * 1000  // 24시간

    private fun CachedValue<*>.isExpired() = System.currentTimeMillis() - cachedAt >= TTL_MS

    // Key: "userId_yearMonth" (예: "abc_2026-05"), Value: 해당 월 일기 목록
    private val monthCache = mutableMapOf<String, CachedValue<List<DiaryEntry>>>()
    // Key: "userId_date" (예: "abc_2026-05-23"), Value: 해당 날짜 일기 (없으면 null 저장)
    private val entryCache = mutableMapOf<String, CachedValue<DiaryEntry?>>()

    init {
        // Design Ref: joyary-upgrade-v7 §2.4 — ViewModel 생성 시 만료 캐시 일괄 정리 (FR-06)
        cleanupExpiredCache()
    }

    // Plan SC: SC-04 Upsert — 해당 날짜 일기 존재 여부 반환
    suspend fun getEntryByDate(userId: String, date: String): DiaryEntry? =
        diaryRepository.getDiaryByDate(userId, date)

    fun loadMonth(userId: String, yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
        val key = "${userId}_${yearMonth}"
        // Design Ref: joyary-upgrade-v7 §2.2 — TTL 유효 시 즉시 반환, 만료 시 lazy eviction (FR-03)
        val cached = monthCache[key]
        if (cached != null) {
            if (!cached.isExpired()) {
                _diaries.value = cached.data
                return
            } else {
                monthCache.remove(key)
            }
        }
        viewModelScope.launch {
            try {
                diaryRepository.getDiariesByMonth(userId, yearMonth).collect { list ->
                    monthCache[key] = CachedValue(list)
                    _diaries.value = list
                }
            } catch (e: Exception) {
                _uiState.value = DiaryUiState.Error(e.message ?: "데이터 로드 실패")
            }
        }
    }

    fun loadDiaryByDate(userId: String, date: String) {
        val key = "${userId}_${date}"
        // Design Ref: joyary-upgrade-v7 §2.2 — TTL 유효 시 즉시 반환, 만료 시 lazy eviction (FR-04)
        val cached = entryCache[key]
        if (cached != null) {
            if (!cached.isExpired()) {
                _selectedEntry.value = cached.data
                return
            } else {
                entryCache.remove(key)
            }
        }
        viewModelScope.launch {
            _isDetailLoading.value = true
            val result = diaryRepository.getDiaryByDate(userId, date)
            entryCache[key] = CachedValue(result)
            _selectedEntry.value = result
            _isDetailLoading.value = false
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
                    if (finalUrls.isNotEmpty()) {
                        diaryRepository.updateDiary(entry.copy(id = newId)).getOrThrow()
                    }
                } else {
                    diaryRepository.updateDiary(entry).getOrThrow()
                }
            }.onSuccess {
                // Design Ref: joyary-upgrade-v6 §5.3 — 저장 성공 시 캐시 무효화 (FR-06)
                invalidateCache(userId, date)
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
                .onSuccess {
                    // Design Ref: joyary-upgrade-v6 §5.3 — 삭제 성공 시 캐시 무효화 (FR-06)
                    invalidateCache(entry.userId, entry.date)
                    _uiState.value = DiaryUiState.Success
                }
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

    // Design Ref: joyary-upgrade-v6 §5.3 — 해당 날짜 + 해당 월 캐시 무효화 (FR-06)
    private fun invalidateCache(userId: String, date: String) {
        val yearMonth = date.substring(0, 7)  // "YYYY-MM"
        monthCache.remove("${userId}_${yearMonth}")
        entryCache.remove("${userId}_${date}")
    }

    // Design Ref: joyary-upgrade-v7 §2.4 — 만료된 모든 캐시 엔트리 일괄 삭제 (FR-06)
    private fun cleanupExpiredCache() {
        monthCache.entries.removeAll { it.value.isExpired() }
        entryCache.entries.removeAll { it.value.isExpired() }
    }

    fun resetState() { _uiState.value = DiaryUiState.Idle }
    fun clearSelectedEntry() { _selectedEntry.value = null }
}
