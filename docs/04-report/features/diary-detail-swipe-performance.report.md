# Report: diary-detail-swipe-performance

**완료일**: 2026-05-31  
**Match Rate**: 100% ✅  
**변경 파일**: 2개 | **변경 라인**: ~60줄

---

## 문제 원인 분석

스와이프 후 콘텐츠가 늦게 나타나는 원인 3가지:

| # | 원인 | 영향 |
|---|------|------|
| 1 | `settledPage` 트리거 — 애니메이션 완료 후에야 로딩 시작 | 항상 딜레이 발생 |
| 2 | 단일 `_selectedEntry` — 프리패치 데이터가 UI에 도달 불가 | 인접 페이지 항상 blank |
| 3 | 인접 날짜 프리패치 없음 — 매번 네트워크/디스크 접근 | 두 번째 스와이프도 느림 |

---

## 수정 내용

### CHANGE-01: LaunchedEffect 트리거 교체 (DiaryDetailScreen.kt)

```kotlin
// Before — 애니메이션 완료 후 로딩 (느림)
LaunchedEffect(pagerState.settledPage, userId) { loadDiaryByDate(...) }

// After — 스와이프 50% 시점에 로딩 (빠름)
LaunchedEffect(pagerState.currentPage, userId) { loadDiaryByDate(...) }

// 추가 — 정착 시 ±1 사일런트 프리패치
LaunchedEffect(pagerState.settledPage, userId) {
    prefetchEntry(userId, 전날)
    prefetchEntry(userId, 다음날)
}
```

### CHANGE-02: Map 기반 entryMap 추가 (DiaryViewModel.kt)

```kotlin
private val _entryMap = MutableStateFlow<Map<String, DiaryEntry?>>(emptyMap())
val entryMap: StateFlow<Map<String, DiaryEntry?>> = _entryMap.asStateFlow()
```

- `loadDiaryByDate()` → `_selectedEntry` + `_entryMap` 동시 업데이트
- `prefetchEntry()` → `_entryMap`만 업데이트 (스켈레톤 없음, isDetailLoading 미조작)
- `warmEntryCache()` → 월 로딩 시 당월 전체를 entryMap에 배치 선채우기
- `invalidateCache()` → `_entryMap`에서도 해당 날짜 제거

### CHANGE-03: 페이지별 독립 조회 (DiaryDetailScreen.kt)

```kotlin
// Before — 단일 entry, 현재 페이지만 표시
val entry by selectedEntry.collectAsStateWithLifecycle()
val pageEntry = if (isCurrentPage) entry else null

// After — 각 페이지가 map에서 독립 조회
val entryMap by diaryViewModel.entryMap.collectAsStateWithLifecycle()
val pageEntry = entryMap[targetDateStr]        // 프리패치 데이터 즉시 표시
val pageHasData = entryMap.containsKey(date)
```

### CHANGE-04: 스켈레톤 조건 개선

```kotlin
// Before — L1 캐시 히트 후에도 잠깐 스켈레톤 가능
isDetailLoading = isCurrentPage && isDetailLoading

// After — 데이터 있으면 스켈레톤 스킵
isDetailLoading = isCurrentPage && isDetailLoading && !pageHasData
```

---

## 개선 후 성능 흐름

```
[홈화면]
loadMonth() → warmEntryCache() → entryMap에 당월 전체 선채움

[상세보기 진입 (날짜 N)]  
entryMap[N] 이미 있음 → 스켈레톤 없이 즉시 표시
settledPage=N → 백그라운드에서 N-1, N+1 프리패치

[첫 스와이프 → N+1 (50% 지점)]
currentPage = N+1 → loadDiaryByDate(N+1)
→ memEntryCache 히트 (프리패치 완료) → 즉시 표시 ✨
settledPage = N+1 → N, N+2 프리패치

[이후 스와이프] 모두 캐시 히트 → 스켈레톤 없음 ✨✨
```

---

## 기존 기능 보존

| 기능 | 상태 |
|------|------|
| `_selectedEntry` (saveDiary에서 사용) | ✅ loadDiaryByDate에서 동시 업데이트 유지 |
| 스켈레톤 애니메이션 | ✅ 최초 진입 / 캐시 없는 경우에만 표시 |
| 삭제 후 캐시 무효화 | ✅ entryMap에서도 해당 날짜 제거 |
| 저장 후 최신 데이터 반영 | ✅ invalidateCache → 재로딩 시 entryMap 갱신 |
