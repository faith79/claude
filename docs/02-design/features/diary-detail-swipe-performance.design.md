# Design: diary-detail-swipe-performance (v2 — ±2 확장)

## Architecture: Option C — Pragmatic Balance (선택) ✅

### 구현 스펙

#### CHANGE-01: currentPage 트리거에 ±1 프리패치 추가 (R-03)

```kotlin
// Before
LaunchedEffect(pagerState.currentPage, userId) {
    val targetDate = pageToDate(pagerState.currentPage).format(DateTimeFormatter.ISO_LOCAL_DATE)
    diaryViewModel.loadDiaryByDate(userId, targetDate)
}

// After
LaunchedEffect(pagerState.currentPage, userId) {
    val currentPage = pagerState.currentPage
    val targetDate = pageToDate(currentPage).format(DateTimeFormatter.ISO_LOCAL_DATE)
    diaryViewModel.loadDiaryByDate(userId, targetDate)
    // 스와이프 50% 시점에 ±1 즉시 프리패치 (R-03)
    for (offset in listOf(-1, 1)) {
        val neighborDate = pageToDate(currentPage + offset)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
        diaryViewModel.prefetchEntry(userId, neighborDate)
    }
}
```

#### CHANGE-02: settledPage 트리거 ±1 → ±2 확장 (R-02)

```kotlin
// Before
LaunchedEffect(pagerState.settledPage, userId) {
    for (offset in listOf(-1, 1)) {
        val neighborDate = pageToDate(pagerState.settledPage + offset)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
        diaryViewModel.prefetchEntry(userId, neighborDate)
    }
}

// After
LaunchedEffect(pagerState.settledPage, userId) {
    for (offset in listOf(-2, -1, 1, 2)) {
        val neighborDate = pageToDate(pagerState.settledPage + offset)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
        diaryViewModel.prefetchEntry(userId, neighborDate)
    }
}
```

### DiaryViewModel.kt — 변경 없음
- `prefetchEntry`에 이미 중복 방지 guard 존재 (`memEntryCache.containsKey`, `_entryMap.value.containsKey`)
- 동일 날짜 중복 호출 시 두 번째는 즉시 반환 (네트워크 낭비 없음)

## 버퍼 커버리지 비교

| 시나리오 | 기존 (±1) | 변경 후 (±2) |
|---------|-----------|-------------|
| 정착 후 | ±1 로드됨 | ±2 로드됨 |
| 스와이프 50% | 현재만 | 현재 + ±1 즉시 시작 |
| 연속 2페이지 스와이프 | 2번째 로딩 중 | 2번째 이미 프리패치됨 |
| 연속 3페이지 스와이프 | 3번째 지연 | 3번째 settledPage ±2로 커버 |

## 변경 파일

| 파일 | 변경 | 예상 라인 |
|------|------|---------|
| `DiaryDetailScreen.kt` | LaunchedEffect 2개 수정 | +4줄 |
