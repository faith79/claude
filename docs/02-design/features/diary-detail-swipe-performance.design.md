# Design: diary-detail-swipe-performance

## Architecture: Option C — Pragmatic Balance

### 변경 1: LaunchedEffect 트리거 교체

```kotlin
// BEFORE — 애니메이션 완료 후 로드 (느림)
LaunchedEffect(pagerState.settledPage, userId) {
    diaryViewModel.loadDiaryByDate(userId, pageToDate(pagerState.settledPage).format(...))
}

// AFTER — 스와이프 50% 시점에 로드 (빠름)
LaunchedEffect(pagerState.currentPage, userId) {
    diaryViewModel.loadDiaryByDate(userId, pageToDate(pagerState.currentPage).format(...))
}

// NEW — 정착 시 ±1 선로딩
LaunchedEffect(pagerState.settledPage, userId) {
    for (offset in listOf(-1, 1)) {
        diaryViewModel.prefetchEntry(userId, pageToDate(pagerState.settledPage + offset).format(...))
    }
}
```

### 변경 2: ViewModel — _entryMap 추가

```kotlin
private val _entryMap = MutableStateFlow<Map<String, DiaryEntry?>>(emptyMap())
val entryMap: StateFlow<Map<String, DiaryEntry?>> = _entryMap.asStateFlow()
```

- `loadDiaryByDate()` → `_selectedEntry` + `_entryMap` 동시 업데이트
- `prefetchEntry()` → `_entryMap`만 업데이트 (스켈레톤 없음)
- `warmEntryCache()` → `_entryMap`에 월 전체 선채움
- `invalidateCache()` → `_entryMap`에서 해당 날짜 제거

### 변경 3: 페이지별 독립 entry 조회

```kotlin
// BEFORE
val entry by diaryViewModel.selectedEntry.collectAsStateWithLifecycle()
val pageEntry = if (isCurrentPage) entry else null

// AFTER
val entryMap by diaryViewModel.entryMap.collectAsStateWithLifecycle()
val pageEntry = entryMap[targetDateStr]         // 프리패치된 데이터 즉시 표시
val pageHasData = entryMap.containsKey(targetDateStr)
```

### 변경 4: 스켈레톤 조건 개선

```kotlin
// BEFORE — L1 캐시 히트 후에도 isDetailLoading 구간 동안 스켈레톤 표시 가능
isDetailLoading = isCurrentPage && isDetailLoading

// AFTER — 데이터가 이미 있으면 스켈레톤 스킵
isDetailLoading = isCurrentPage && isDetailLoading && !pageHasData
```

### 변경 5: 비현재 페이지 표시 조건

```kotlin
// BEFORE — 비현재 페이지 항상 blank
!isCurrentPage → blank

// AFTER — 비현재 페이지도 데이터 있으면 표시 (스와이프 중 미리 보임)
!isCurrentPage && !hasData → blank
```

### 성능 흐름 (개선 후)

```
[앱 시작 → 홈화면]
loadMonth() → warmEntryCache() → entryMap에 당월 전체 선채움

[상세보기 진입 (날짜 N)]
entryMap[N] 이미 있음 → 즉시 표시 (스켈레톤 없음)
settledPage=N → prefetch N-1, N+1

[스와이프 → N+1 (50% 지점)]
currentPage = N+1 → loadDiaryByDate(N+1)
→ memEntryCache 히트 (prefetch로 이미 있음) → 즉시 표시
settledPage = N+1 → prefetch N, N+2
```

[CP-3 Auto] Option C — Pragmatic Balance 선택됨
