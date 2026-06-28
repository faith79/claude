# Report: calendar-swipe-performance

**완료일**: 2026-06-01  
**Match Rate**: 100% ✅  
**변경 파일**: 2개 | **변경 라인**: ~40줄

---

## 문제 원인 분석

달력 스와이프 시 아이콘이 느리게 나타나는 원인 2가지:

| # | 원인 | 영향 |
|---|------|------|
| 1 | `settledPage` 트리거 — 애니메이션 완료 후에야 loadMonth 시작 | 항상 딜레이 발생 |
| 2 | 단일 `_diaries` → `diaryMap` — 모든 페이지가 같은 map 공유 | 인접 달 페이지는 항상 공백 |

기존 `prefetchMonth(±1)` 이미 동작 중이었으나, 그 결과를 UI가 소비할 경로가 없었음.

---

## 수정 내용

### CHANGE-01: _monthlyDiaryMap StateFlow 추가 (DiaryViewModel.kt)

```kotlin
private val _monthlyDiaryMap = MutableStateFlow<Map<String, List<DiaryEntry>>>(emptyMap())
val monthlyDiaryMap: StateFlow<Map<String, List<DiaryEntry>>> = _monthlyDiaryMap.asStateFlow()
```

### CHANGE-02: warmEntryCache 시그니처 변경

```kotlin
// Before
private fun warmEntryCache(userId: String, entries: List<DiaryEntry>)

// After
private fun warmEntryCache(userId: String, yearMonth: YearMonth, entries: List<DiaryEntry>)
// 추가: _monthlyDiaryMap.value = _monthlyDiaryMap.value + (monthKey to entries)
```

- 빈 달도 monthKey 등록 → "데이터 없음" 확인됨 상태 표시 가능
- 새 데이터 로드 시 항상 덮어씀 (stale 방지)
- 7개 호출부 모두 업데이트

### CHANGE-03: invalidateCache 동기화

```kotlin
_monthlyDiaryMap.value = _monthlyDiaryMap.value - monthKey
```

저장/삭제 후 해당 달 재로딩 시 최신 데이터로 교체.

### CHANGE-04: LaunchedEffect 분리 (HomeScreen.kt)

```kotlin
// Before — settledPage (애니메이션 완료 후)
LaunchedEffect(pagerState.settledPage, userId) {
    diaryViewModel.loadMonth(...)
    diaryViewModel.prefetchMonth(±1)
}

// After — 분리
LaunchedEffect(pagerState.currentPage, userId) {  // 스와이프 50% 시점
    diaryViewModel.loadMonth(userId, pageToYearMonth(pagerState.currentPage))
}
LaunchedEffect(pagerState.settledPage, userId) {  // 정착 후
    diaryViewModel.prefetchMonth(userId, current.minusMonths(1))
    diaryViewModel.prefetchMonth(userId, current.plusMonths(1))
}
```

### CHANGE-05: 페이지별 독립 diaryMap

```kotlin
// Before — 모든 페이지 공유 단일 map
val diaries by diaryViewModel.diaries.collectAsStateWithLifecycle()
val diaryMap = remember(diaries) { diaries.associateBy { it.date } }

// After — 각 페이지 monthlyDiaryMap 슬라이스
val monthlyDiaryMap by diaryViewModel.monthlyDiaryMap.collectAsStateWithLifecycle()
// HorizontalPager page 람다 안:
val monthKey = "${userId}_${pageMonth}"
val diaryMap = remember(monthlyDiaryMap, monthKey) {
    monthlyDiaryMap[monthKey]?.associateBy { it.date } ?: emptyMap()
}
```

---

## 개선 후 성능 흐름

```
[홈 진입 — 현재달 M]
loadMonth(M) → warmEntryCache(userId, M, list) → _monthlyDiaryMap[M] 채움 → 아이콘 즉시 표시
settledPage=M → prefetchMonth(M-1), prefetchMonth(M+1) → _monthlyDiaryMap[M±1] 채움

[스와이프 → M+1 (50% 시점)]
currentPage=M+1 → loadMonth(M+1)
  → L1 캐시 히트 (prefetch 완료) → warmEntryCache → monthlyDiaryMap[M+1] 갱신
  → 각 페이지 독립 소비 → 스와이프 중 아이콘 즉시 표시 ✨

[이후 스와이프] 모두 캐시 히트 → 아이콘 즉시 표시 ✨✨
```

---

## 기존 기능 보존

| 기능 | 상태 |
|------|------|
| `_diaries` StateFlow (기존 소비자) | ✅ loadMonth에서 계속 업데이트 |
| `prefetchEntry` / `entryMap` (상세보기) | ✅ 미변경 |
| 저장/삭제 후 캐시 무효화 | ✅ invalidateCache에서 monthlyDiaryMap도 제거 |
| FAB Upsert 동작 | ✅ getEntryByDate 미변경 |
