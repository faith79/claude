# Plan: diary-detail-swipe-performance

## Context Anchor
- **WHY**: 일기 상세보기 스와이프 시 ±1 프리패치만으로는 빠른 연속 스와이프 시 콘텐츠 지연 발생
- **WHO**: 일기를 읽으며 날짜 간 빠르게 스와이프하는 사용자
- **RISK**: 불필요 네트워크 호출 증가 — L1/L2 캐시 가드로 중복 방지
- **SUCCESS**: 빠른 연속 스와이프 시 스켈레톤 없이 즉시 콘텐츠 표시
- **SCOPE**: DiaryDetailScreen.kt (LaunchedEffect 2개), DiaryViewModel.kt (변경 없음)

## 근본 원인 (v2 분석)
1. `settledPage` 트리거에서만 ±1 프리패치 → 빠른 스와이프(2페이지 이상)면 버퍼 부족
2. `currentPage` 트리거는 현재 페이지만 로드, 인접 페이지 선로딩 없음
3. **새 요구**: ±2 버퍼로 확장하면 빠른 스와이프에도 콘텐츠 즉시 표시 가능

## 요구사항

| # | 요구사항 |
|---|---------|
| R-01 | `currentPage` 트리거로 변경 — 스와이프 50% 시점에 로딩 시작 (기존 유지) |
| R-02 | `settledPage` 트리거: ±2 인접 날짜 사일런트 프리패치 (±1 → ±2 확장) |
| R-03 | `currentPage` 트리거: 현재 로드와 동시에 ±1 즉시 프리패치 (신규) |
| R-04 | Map 기반 `_entryMap` StateFlow — 페이지별 독립 데이터 보유 (기존 유지) |
| R-05 | `warmEntryCache` 시 entryMap도 동시 채우기 (기존 유지) |
| R-06 | L1 캐시 히트 → 스켈레톤 미표시 (기존 유지) |
| R-07 | `invalidateCache` 시 entryMap에서도 제거 (기존 유지) |

## 변경 파일

| 파일 | 변경 내용 |
|------|---------|
| DiaryDetailScreen.kt | LaunchedEffect(currentPage): ±1 prefetch 추가 |
| DiaryDetailScreen.kt | LaunchedEffect(settledPage): ±1 → ±2 확장 |

[CP-1 Auto] 요구사항 확인됨 → 계속 진행
[CP-2 Auto] 명확화 질문 생략 → 합리적 기본값 적용
