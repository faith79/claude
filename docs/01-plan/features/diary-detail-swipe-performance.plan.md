# Plan: diary-detail-swipe-performance

## Context Anchor
- **WHY**: 일기 상세보기 스와이프 시 콘텐츠가 늦게 나타남 — 스켈레톤 대기 UX 불량
- **WHO**: 일기를 읽으며 날짜 간 스와이프하는 사용자
- **RISK**: currentPage 트리거 시 스와이프 취소 후 불필요 로드 가능 (L1 캐시로 비용 최소화)
- **SUCCESS**: 스와이프 완료 전에 콘텐츠 표시, 두 번째 스와이프부터 즉시 표시
- **SCOPE**: DiaryViewModel.kt, DiaryDetailScreen.kt

## 근본 원인
1. `LaunchedEffect(settledPage)` → 애니메이션 완료 후 로드 시작 (너무 늦음)
2. `_selectedEntry` 단일 StateFlow → 프리패치된 데이터가 UI에 도달 불가
3. 정착 후 인접 날짜 프리패치 없음 → 다음 스와이프도 항상 대기

## 요구사항

| # | 요구사항 |
|---|---------|
| R-01 | `currentPage` 트리거로 변경 — 스와이프 50% 시점에 로딩 시작 |
| R-02 | 정착(`settledPage`) 시 ±1 인접 날짜 사일런트 프리패치 |
| R-03 | Map 기반 `_entryMap` StateFlow — 페이지별 독립 데이터 보유 |
| R-04 | `warmEntryCache` 시 entryMap도 동시 채우기 (월 로딩 → 즉시 상세보기) |
| R-05 | L1 캐시 히트 → 스켈레톤 미표시 (`!pageHasData` 조건) |
| R-06 | `invalidateCache` 시 entryMap에서도 제거 (저장/삭제 후 stale 방지) |

[CP-1 Auto] 요구사항 확인됨 → 계속 진행
[CP-2 Auto] 명확화 질문 생략 → 합리적 기본값 적용
