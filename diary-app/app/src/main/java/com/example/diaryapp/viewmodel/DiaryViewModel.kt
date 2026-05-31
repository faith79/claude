package com.example.diaryapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.model.DiaryEntry
import com.example.diaryapp.data.model.EmotionTag
import com.example.diaryapp.data.model.WeatherTag
import com.example.diaryapp.data.repository.DiaryRepository
import com.example.diaryapp.data.util.DiaryLocalCache
import com.example.diaryapp.data.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val imageCompressor: ImageCompressor,
    private val localCache: DiaryLocalCache
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

    // Design Ref: diary-detail-swipe-performance §design — 페이지별 독립 entry, 프리패치 데이터 보유
    private val _entryMap = MutableStateFlow<Map<String, DiaryEntry?>>(emptyMap())
    val entryMap: StateFlow<Map<String, DiaryEntry?>> = _entryMap.asStateFlow()

    // Design Ref: calendar-swipe-performance §CHANGE-01 — 달별 다이어리 맵, 각 캘린더 페이지 독립 소비
    private val _monthlyDiaryMap = MutableStateFlow<Map<String, List<DiaryEntry>>>(emptyMap())
    val monthlyDiaryMap: StateFlow<Map<String, List<DiaryEntry>>> = _monthlyDiaryMap.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val searchResults: StateFlow<List<DiaryEntry>> = _searchResults.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // Design Ref: joyary-upgrade-v8 §4.2 — L1 메모리 캐시 (세션 내 즉시 조회)
    private val memMonthCache = mutableMapOf<String, List<DiaryEntry>>()
    private val memEntryCache = mutableMapOf<String, DiaryEntry?>()

    init {
        // Design Ref: joyary-upgrade-v8 §4.2 — 앱 시작 시 L2 만료 파일 삭제 (SC-05)
        localCache.cleanupExpired()
    }

    // Plan SC: SC-04 Upsert — 해당 날짜 일기 존재 여부 반환
    suspend fun getEntryByDate(userId: String, date: String): DiaryEntry? =
        diaryRepository.getDiaryByDate(userId, date)

    fun loadMonth(userId: String, yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
        val key = "${userId}_${yearMonth}"
        // Design Ref: joyary-upgrade-v8 §4.3 — L1(메모리) → L2(디스크) → Firestore (SC-02)
        // Design Ref: calendar-swipe-performance §CHANGE-03 — L1 히트 시 monthlyDiaryMap도 갱신
        memMonthCache[key]?.let {
            warmEntryCache(userId, yearMonth, it)
            _diaries.value = it
            return
        }
        // Design Ref: joyary-upgrade-v9 §2.3 — L2 읽기/쓰기 IO dispatcher (SC-02)
        viewModelScope.launch {
            val cached = withContext(Dispatchers.IO) { localCache.getMonth(key) }
            if (cached != null) {
                warmEntryCache(userId, yearMonth, cached)
                memMonthCache[key] = cached
                _diaries.value = cached
                return@launch
            }
            try {
                diaryRepository.getDiariesByMonth(userId, yearMonth).collect { list ->
                    // Design Ref: joyary-upgrade-v9 §2.3 — 월 로드 시 entry 선채움 (SC-04)
                    warmEntryCache(userId, yearMonth, list)
                    memMonthCache[key] = list
                    withContext(Dispatchers.IO) { localCache.putMonth(key, list) }
                    _diaries.value = list
                }
            } catch (e: Exception) {
                _uiState.value = DiaryUiState.Error(e.message ?: "데이터 로드 실패")
            }
        }
    }

    // Design Ref: joyary-upgrade-v9 §2.3 — 월 내 모든 entry를 memEntryCache에 선채움 (SC-04)
    // Design Ref: diary-detail-swipe-performance §design — entryMap도 동시 채우기 (R-04)
    // Design Ref: calendar-swipe-performance §CHANGE-02 — monthlyDiaryMap 동시 채우기 (R-02,R-03)
    private fun warmEntryCache(userId: String, yearMonth: YearMonth, entries: List<DiaryEntry>) {
        val monthKey = "${userId}_${yearMonth}"
        _monthlyDiaryMap.value = _monthlyDiaryMap.value + (monthKey to entries)
        val newMapEntries = mutableMapOf<String, DiaryEntry?>()
        entries.forEach { entry ->
            val entryKey = "${userId}_${entry.date}"
            if (!memEntryCache.containsKey(entryKey)) {
                memEntryCache[entryKey] = entry
            }
            if (!_entryMap.value.containsKey(entry.date)) {
                newMapEntries[entry.date] = entry
            }
        }
        if (newMapEntries.isNotEmpty()) {
            _entryMap.value = _entryMap.value + newMapEntries
        }
    }

    fun loadDiaryByDate(userId: String, date: String) {
        val key = "${userId}_${date}"
        // Design Ref: joyary-upgrade-v8 §4.4 — L1(메모리) → L2(디스크) → Firestore (SC-03)
        // Design Ref: diary-detail-swipe-performance §design — L1 히트 시 entryMap도 업데이트, 스켈레톤 없음
        if (memEntryCache.containsKey(key)) {
            val cached = memEntryCache[key]
            _selectedEntry.value = cached
            _entryMap.value = _entryMap.value + (date to cached)
            return
        }
        // Design Ref: joyary-upgrade-v9 §2.4 — L2 읽기/쓰기 IO dispatcher (SC-03)
        viewModelScope.launch {
            _isDetailLoading.value = true
            val cached = withContext(Dispatchers.IO) { localCache.getEntry(key) }
            if (cached != null) {
                val (_, entry) = cached
                memEntryCache[key] = entry
                _selectedEntry.value = entry
                _entryMap.value = _entryMap.value + (date to entry)
                _isDetailLoading.value = false
                return@launch
            }
            val result = diaryRepository.getDiaryByDate(userId, date)
            memEntryCache[key] = result
            withContext(Dispatchers.IO) { localCache.putEntry(key, result) }
            _selectedEntry.value = result
            _entryMap.value = _entryMap.value + (date to result)
            _isDetailLoading.value = false
        }
    }

    // Design Ref: diary-detail-swipe-performance §design — 인접 날짜 사일런트 선로딩 (R-02, R-03)
    fun prefetchEntry(userId: String, date: String) {
        val key = "${userId}_${date}"
        if (memEntryCache.containsKey(key)) {
            if (!_entryMap.value.containsKey(date)) {
                _entryMap.value = _entryMap.value + (date to memEntryCache[key])
            }
            return
        }
        if (_entryMap.value.containsKey(date)) return
        viewModelScope.launch {
            val diskCached = withContext(Dispatchers.IO) { localCache.getEntry(key) }
            val result = if (diskCached != null) {
                diskCached.second
            } else {
                val r = diaryRepository.getDiaryByDate(userId, date)
                withContext(Dispatchers.IO) { localCache.putEntry(key, r) }
                r
            }
            memEntryCache[key] = result
            _entryMap.value = _entryMap.value + (date to result)
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
                // Design Ref: joyary-upgrade-v8 §4.5 — 저장 성공 시 L1+L2 캐시 무효화 (FR-06)
                invalidateCache(userId, date)
                // Design Ref: joyary-upgrade-v9 §2.2 — Firestore 강제 재조회 (SC-01)
                // 스켈레톤 트리거: 복귀 시 스켈레톤 → 최신 데이터 순서로 표시
                _isDetailLoading.value = true
                _selectedEntry.value = null
                viewModelScope.launch {
                    val key = "${userId}_${date}"
                    val result = diaryRepository.getDiaryByDate(userId, date)
                    memEntryCache[key] = result
                    withContext(Dispatchers.IO) { localCache.putEntry(key, result) }
                    _selectedEntry.value = result
                    _isDetailLoading.value = false
                }
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

    // Design Ref: joyary-upgrade-v8 §4.5 — L1 + L2 동시 무효화 (SC-04)
    // Design Ref: diary-detail-swipe-performance §design — entryMap에서도 제거 (R-06)
    // Design Ref: calendar-swipe-performance §CHANGE-04 — monthlyDiaryMap에서도 달 제거 (R-06)
    private fun invalidateCache(userId: String, date: String) {
        val yearMonth = date.substring(0, 7)
        val monthKey = "${userId}_${yearMonth}"
        val entryKey = "${userId}_${date}"
        memMonthCache.remove(monthKey)
        memEntryCache.remove(entryKey)
        localCache.removeMonth(monthKey)
        localCache.removeEntry(entryKey)
        _entryMap.value = _entryMap.value - date
        _monthlyDiaryMap.value = _monthlyDiaryMap.value - monthKey
    }

    // Design Ref: joyary-ux-improvements §FR-03 — 인접 달 백그라운드 선로딩 (_diaries 미변경)
    // Design Ref: calendar-swipe-performance §CHANGE-03 — monthlyDiaryMap 채우기로 UI 즉시 반영
    fun prefetchMonth(userId: String, yearMonth: YearMonth) {
        val key = "${userId}_${yearMonth}"
        if (memMonthCache.containsKey(key)) {
            warmEntryCache(userId, yearMonth, memMonthCache[key]!!)
            return
        }
        viewModelScope.launch {
            val cached = withContext(Dispatchers.IO) { localCache.getMonth(key) }
            if (cached != null) {
                warmEntryCache(userId, yearMonth, cached)
                memMonthCache[key] = cached
                return@launch
            }
            try {
                diaryRepository.getDiariesByMonth(userId, yearMonth).collect { list ->
                    warmEntryCache(userId, yearMonth, list)
                    memMonthCache[key] = list
                    withContext(Dispatchers.IO) { localCache.putMonth(key, list) }
                }
            } catch (_: Exception) { /* prefetch 실패는 비핵심, 무시 */ }
        }
    }

    fun resetState() { _uiState.value = DiaryUiState.Idle }
    fun clearSelectedEntry() { _selectedEntry.value = null }
}
