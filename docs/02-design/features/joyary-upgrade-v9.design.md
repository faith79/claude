# joyary-upgrade-v9 Design

> **Phase**: Design | **Date**: 2026-05-23 | **Architecture**: Option C — Pragmatic Balance

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 저장 후 구버전 데이터 표시 + 메인 스레드 디스크 I/O = 사용자 체감 속도 저하 |
| **WHO** | 매일 일기를 쓰고 수정하는 조이어리 사용자 |
| **RISK** | 동시성 이슈 (save 중 loadDiaryByDate 경쟁); 스켈레톤 미표시 엣지케이스 |
| **SUCCESS** | 저장 후 최신 데이터 자동 표시; 디스크 I/O IO dispatcher; 스켈레톤 표시 |
| **SCOPE** | DiaryViewModel.kt, DiaryDetailScreen.kt |

---

## 1. Architecture Options

### Option A — Minimal
- `loadDiaryByDate` 강제 재실행 트리거 StateFlow 추가만 → skeleton 없음, 근본 성능 미해결

### Option B — Clean
- ViewModel을 완전 비동기 StateFlow 기반으로 재설계 → 변경 범위 대폭 확대, 위험도 높음

### Option C — Pragmatic Balance ✅
- 기존 구조 유지 + 최소 변경:
  1. `saveDiary()` 내부에서 강제 재조회 로직 추가
  2. `withContext(Dispatchers.IO)` 로 디스크 I/O 이동
  3. 월 로드 시 entry 선채움
  4. `DiaryDetailSkeleton` 컴포저블 추가 (새 파일 없음, 기존 파일에 private 함수)

---

## 2. DiaryViewModel.kt 변경

### 2.1 imports 추가
```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
```

### 2.2 saveDiary() — 강제 재조회 (SC-01)

기존 `.onSuccess { invalidateCache(userId, date); ... }` 블록을:
```kotlin
.onSuccess {
    invalidateCache(userId, date)
    // 저장 완료 후 Firestore에서 즉시 재조회 — _selectedEntry 갱신 보장
    _isDetailLoading.value = true
    _selectedEntry.value = null  // 스켈레톤 트리거
    viewModelScope.launch {
        val key = "${userId}_${date}"
        val result = diaryRepository.getDiaryByDate(userId, date)
        memEntryCache[key] = result
        withContext(Dispatchers.IO) { localCache.putEntry(key, result) }
        _selectedEntry.value = result
        _isDetailLoading.value = false
    }
    _uiState.value = DiaryUiState.Success
}
```

**이유**: 
- `_isDetailLoading = true` + `_selectedEntry = null` 동기적 설정 → 복귀 시 즉시 스켈레톤
- Firestore 재조회 결과가 오면 자동으로 상세화면 갱신

### 2.3 loadMonth() — IO dispatcher + entry 선채움 (SC-02, SC-04)

```kotlin
fun loadMonth(userId: String, yearMonth: YearMonth) {
    _currentMonth.value = yearMonth
    val key = "${userId}_${yearMonth}"
    // L1 hit
    memMonthCache[key]?.let { _diaries.value = it; return }
    // L2 + Firestore on coroutine
    viewModelScope.launch {
        // L2 읽기 → IO dispatcher
        val cached = withContext(Dispatchers.IO) { localCache.getMonth(key) }
        if (cached != null) {
            warmEntryCache(userId, cached)  // 선채움
            memMonthCache[key] = cached
            _diaries.value = cached
            return@launch
        }
        try {
            diaryRepository.getDiariesByMonth(userId, yearMonth).collect { list ->
                warmEntryCache(userId, list)  // 선채움
                memMonthCache[key] = list
                withContext(Dispatchers.IO) { localCache.putMonth(key, list) }  // IO 쓰기
                _diaries.value = list
            }
        } catch (e: Exception) {
            _uiState.value = DiaryUiState.Error(e.message ?: "데이터 로드 실패")
        }
    }
}

// SC-04: 월 내 모든 entry를 memEntryCache에 선채움
private fun warmEntryCache(userId: String, entries: List<DiaryEntry>) {
    entries.forEach { entry ->
        val entryKey = "${userId}_${entry.date}"
        if (!memEntryCache.containsKey(entryKey)) {
            memEntryCache[entryKey] = entry
        }
    }
}
```

### 2.4 loadDiaryByDate() — IO dispatcher (SC-03)

```kotlin
fun loadDiaryByDate(userId: String, date: String) {
    val key = "${userId}_${date}"
    // L1 hit
    if (memEntryCache.containsKey(key)) { _selectedEntry.value = memEntryCache[key]; return }
    // L2 + Firestore
    viewModelScope.launch {
        _isDetailLoading.value = true
        // L2 읽기 → IO dispatcher
        val cached = withContext(Dispatchers.IO) { localCache.getEntry(key) }
        if (cached != null) {
            val (_, entry) = cached
            memEntryCache[key] = entry
            _selectedEntry.value = entry
            _isDetailLoading.value = false
            return@launch
        }
        val result = diaryRepository.getDiaryByDate(userId, date)
        memEntryCache[key] = result
        withContext(Dispatchers.IO) { localCache.putEntry(key, result) }  // IO 쓰기
        _selectedEntry.value = result
        _isDetailLoading.value = false
    }
}
```

---

## 3. DiaryDetailScreen.kt 변경

### 3.1 DiaryDetailSkeleton 컴포저블 추가 (SC-05)

```kotlin
@Composable
private fun DiaryDetailSkeleton(modifier: Modifier = Modifier) {
    val alpha by rememberInfiniteTransition(label = "skeleton")
        .animateFloat(
            initialValue = 0.4f, targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "alpha"
        )
    val shimmer = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        // 이미지 플레이스홀더
        Box(Modifier.fillMaxWidth().height(220.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp)).background(shimmer))
        // 칩 플레이스홀더 2개
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(16.dp)).background(shimmer))
            Box(Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(16.dp)).background(shimmer))
        }
        Spacer(Modifier.height(12.dp))
        // 본문 카드 플레이스홀더
        Box(Modifier.fillMaxWidth().height(160.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)).background(shimmer))
    }
}
```

### 3.2 DiaryPageContent — 분기 변경 (SC-06)

기존:
```kotlin
if (!isCurrentPage || isDetailLoading) {
    Box { CircularProgressIndicator() }
    return@Scaffold
}
```

변경:
```kotlin
when {
    !isCurrentPage -> {
        Box(Modifier.fillMaxSize().padding(padding))
        return@Scaffold
    }
    isDetailLoading -> {
        DiaryDetailSkeleton(Modifier.fillMaxSize().padding(padding))
        return@Scaffold
    }
}
```

(이후 `entry == null` → EmptyDiaryPage, else → DiaryEntryContent 동일)

---

## 4. 필요한 imports (DiaryDetailScreen.kt 추가)

```kotlin
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.clip  // (이미 있음)
```

---

## 5. Success Criteria 매핑

| SC | 파일 | 섹션 |
|----|------|------|
| SC-01 | DiaryViewModel.kt | §2.2 |
| SC-02 | DiaryViewModel.kt | §2.3 |
| SC-03 | DiaryViewModel.kt | §2.4 |
| SC-04 | DiaryViewModel.kt | §2.3 `warmEntryCache` |
| SC-05 | DiaryDetailScreen.kt | §3.1 |
| SC-06 | DiaryDetailScreen.kt | §3.2 |
| SC-07 | 미수정 파일들 | — |
