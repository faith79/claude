# Plan: calendar-swipe-performance

## Context Anchor
- **WHY**: 달력 월 스와이프 시 감정 아이콘이 느리게 나타남. settledPage 트리거 + 단일 diaryMap 구조로 인접 달 데이터가 페이지에 전달되지 않음.
- **WHO**: 모든 조이어리 사용자 (달력 탐색 핵심 UX)
- **RISK**: _monthlyDiaryMap 추가 시 메모리 사용량 증가 (달당 최대 31 entry, 허용 범위)
- **SUCCESS**: 인접 달 스와이프 시 즉시 아이콘 표시. loadMonth latency 제거.
- **SCOPE**: DiaryViewModel.kt + HomeScreen.kt 2개 파일만 수정

[CP-1 Auto] 요구사항 확인됨 → 계속 진행
[CP-2 Auto] 명확화 질문 생략 → 합리적 기본값 적용

## 요구사항

| ID | 요구사항 | 우선순위 |
|----|---------|---------|
| R-01 | pagerState.currentPage 트리거로 loadMonth 호출 시점 앞당기기 | High |
| R-02 | _monthlyDiaryMap StateFlow 추가 (key: "userId_YYYY-MM", value: List<DiaryEntry>) | High |
| R-03 | warmEntryCache(userId, yearMonth, entries) 시그니처 변경 — 빈 달도 monthlyDiaryMap 등록 | High |
| R-04 | HomeScreen: 각 HorizontalPager 페이지가 monthlyDiaryMap 슬라이스를 독립 소비 | High |
| R-05 | LaunchedEffect 분리: currentPage → loadMonth, settledPage → prefetchMonth(±1) | High |
| R-06 | invalidateCache: _monthlyDiaryMap에서 해당 달 제거 | Medium |

## 변경 파일

| 파일 | 변경 내용 |
|------|---------|
| DiaryViewModel.kt | _monthlyDiaryMap 추가, warmEntryCache 시그니처 변경, invalidateCache 업데이트 |
| HomeScreen.kt | monthlyDiaryMap 수집, LaunchedEffect 분리, 페이지별 diaryMap 슬라이스 |
