# Report: diary-detail-swipe-performance (v2 — ±2 확장)

**완료일**: 2026-06-01
**Match Rate**: 100% ✅
**변경 파일**: 1개 | **변경 라인**: ~6줄

---

## 수정 내용

### 1. currentPage 트리거 — ±1 즉시 프리패치 추가 (DiaryDetailScreen.kt)

```kotlin
// Before: 현재 페이지만 로드
LaunchedEffect(pagerState.currentPage, userId) {
    val targetDate = pageToDate(pagerState.currentPage).format(DateTimeFormatter.ISO_LOCAL_DATE)
    diaryViewModel.loadDiaryByDate(userId, targetDate)
}

// After: 현재 로드 + ±1 즉시 프리패치
LaunchedEffect(pagerState.currentPage, userId) {
    val currentPage = pagerState.currentPage
    val targetDate = pageToDate(currentPage).format(DateTimeFormatter.ISO_LOCAL_DATE)
    diaryViewModel.loadDiaryByDate(userId, targetDate)
    for (offset in listOf(-1, 1)) {
        val neighborDate = pageToDate(currentPage + offset).format(DateTimeFormatter.ISO_LOCAL_DATE)
        diaryViewModel.prefetchEntry(userId, neighborDate)
    }
}
```

### 2. settledPage 트리거 — ±1 → ±2 확장 (DiaryDetailScreen.kt)

```kotlin
// Before: ±1만 프리패치
LaunchedEffect(pagerState.settledPage, userId) {
    for (offset in listOf(-1, 1)) { ... }
}

// After: ±2로 확장
LaunchedEffect(pagerState.settledPage, userId) {
    for (offset in listOf(-2, -1, 1, 2)) { ... }
}
```

---

## 버퍼 커버리지 개선

| 시나리오 | 기존 | 변경 후 |
|---------|------|--------|
| 정착 후 | ±1 로드됨 | ±2 로드됨 |
| 스와이프 50% | 현재만 로드 | 현재 + ±1 즉시 시작 |
| 연속 2페이지 빠른 스와이프 | 2번째 로딩 중 (지연) | 2번째 이미 프리패치됨 |
| 연속 3페이지 빠른 스와이프 | 3번째 지연 | 3번째 settledPage ±2로 커버 |

## 중복 호출 안전성

- `prefetchEntry`에 이중 guard: `memEntryCache.containsKey` + `_entryMap.value.containsKey`
- currentPage에서 ±1, settledPage에서 ±2 중복 호출 시 두 번째는 즉시 반환 (네트워크 낭비 없음)

## 적용 범위

- **신규 진입**: 초기 페이지에서 ±2 즉시 버퍼링
- **빠른 스와이프**: 2페이지 이상 연속 스와이프 시 대부분 프리패치됨
- **기존 기능**: entryMap, warmEntryCache, invalidateCache 모두 미변경
