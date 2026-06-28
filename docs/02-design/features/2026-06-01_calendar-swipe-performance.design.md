# Design: calendar-swipe-performance

## Architecture: Option C — Pragmatic Balance

기존 prefetchMonth 로직은 이미 동작 중. 
문제: prefetch 결과를 UI가 소비할 경로(observable StateFlow)가 없음.
해결: _monthlyDiaryMap 추가 + 페이지별 독립 슬라이스 소비.

---

## CHANGE-01: _monthlyDiaryMap StateFlow 추가 (DiaryViewModel.kt)

```kotlin
private val _monthlyDiaryMap = MutableStateFlow<Map<String, List<DiaryEntry>>>(emptyMap())
val monthlyDiaryMap: StateFlow<Map<String, List<DiaryEntry>>> = _monthlyDiaryMap.asStateFlow()
```

위치: `_entryMap` 선언 바로 아래.

---

## CHANGE-02: warmEntryCache 시그니처 변경 (DiaryViewModel.kt)

```kotlin
// Before
private fun warmEntryCache(userId: String, entries: List<DiaryEntry>)

// After
private fun warmEntryCache(userId: String, yearMonth: YearMonth, entries: List<DiaryEntry>)
```

추가 로직:
```kotlin
val monthKey = "${userId}_${yearMonth}"
_monthlyDiaryMap.value = _monthlyDiaryMap.value + (monthKey to entries)
```

- 기존 _entryMap / memEntryCache 업데이트 로직은 유지
- 빈 달(entries empty)도 monthKey 등록 → CalendarGrid 공백 달 정상 표시
- 새 데이터 로드 시 항상 덮어씀 (stale 방지)

---

## CHANGE-03: warmEntryCache 호출부 업데이트 (DiaryViewModel.kt)

| 함수 | 기존 | 변경 |
|------|------|------|
| loadMonth | warmEntryCache(userId, list) | warmEntryCache(userId, yearMonth, list) |
| prefetchMonth (disk cache hit) | warmEntryCache(userId, cached) | warmEntryCache(userId, yearMonth, cached) |
| prefetchMonth (Firestore) | warmEntryCache(userId, list) | warmEntryCache(userId, yearMonth, list) |

---

## CHANGE-04: invalidateCache — _monthlyDiaryMap 제거 (DiaryViewModel.kt)

```kotlin
// 기존 코드에 추가
val yearMonth = date.substring(0, 7)  // 이미 있음
val monthKey = "${userId}_${yearMonth}"  // 이미 있음
_monthlyDiaryMap.value = _monthlyDiaryMap.value - monthKey
```

저장/삭제 후 해당 달 재로딩 시 최신 데이터로 교체됨.

---

## CHANGE-05: HomeScreen LaunchedEffect 분리

```kotlin
// Before — 하나의 LaunchedEffect, settledPage 트리거
LaunchedEffect(pagerState.settledPage, userId) {
    val current = pageToYearMonth(pagerState.settledPage)
    diaryViewModel.loadMonth(userId, current)
    diaryViewModel.prefetchMonth(userId, current.minusMonths(1))
    diaryViewModel.prefetchMonth(userId, current.plusMonths(1))
}

// After — 두 개로 분리
// 스와이프 50% 시점에 현재 목표 달 로딩 시작
LaunchedEffect(pagerState.currentPage, userId) {
    if (userId.isNotEmpty()) {
        diaryViewModel.loadMonth(userId, pageToYearMonth(pagerState.currentPage))
    }
}
// 정착 후 인접 달 선로딩
LaunchedEffect(pagerState.settledPage, userId) {
    if (userId.isNotEmpty()) {
        val current = pageToYearMonth(pagerState.settledPage)
        diaryViewModel.prefetchMonth(userId, current.minusMonths(1))
        diaryViewModel.prefetchMonth(userId, current.plusMonths(1))
    }
}
```

---

## CHANGE-06: HomeScreen — 페이지별 독립 diaryMap

```kotlin
// Before — 단일 diaryMap, 모든 페이지 공유
val diaries by diaryViewModel.diaries.collectAsStateWithLifecycle()
val diaryMap = remember(diaries) { diaries.associateBy { it.date } }

// After — monthlyDiaryMap 수집, 각 페이지 독립 슬라이스
val monthlyDiaryMap by diaryViewModel.monthlyDiaryMap.collectAsStateWithLifecycle()

// HorizontalPager 내부 (page 람다 안)
val monthKey = "${userId}_${pageMonth}"
val diaryMap = remember(monthlyDiaryMap, monthKey) {
    monthlyDiaryMap[monthKey]?.associateBy { it.date } ?: emptyMap()
}
```

---

## 성능 흐름 (수정 후)

```
[홈 진입 — 현재달 M]
loadMonth(M) → warmEntryCache(userId, M, list) → _monthlyDiaryMap[M] 채움
settledPage=M → prefetchMonth(M-1), prefetchMonth(M+1) → _monthlyDiaryMap[M-1], [M+1] 채움

[스와이프 50% → M+1]
currentPage=M+1 → loadMonth(M+1)
  → L1 캐시 히트 (prefetch 완료) → warmEntryCache → _monthlyDiaryMap[M+1] 이미 있음 → 즉시 표시 ✨

[빠른 다음 스와이프 → M+2]
prefetch 미완료 시 → loadMonth(M+2) currentPage 트리거로 즉시 시작
settledPage=M+1 → prefetchMonth(M+2) 병렬 진행 → 빠른 표시 ✨
```
