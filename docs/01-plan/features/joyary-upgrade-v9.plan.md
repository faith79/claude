# joyary-upgrade-v9 Plan

> **Phase**: Plan | **Date**: 2026-05-23 | **Threshold**: 100%

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | (1) 저장/수정 후 상세화면 복귀 시 구버전 데이터 표시 (LaunchedEffect 재실행 안됨); (2) L2 디스크 I/O를 메인 스레드에서 호출 → UI 블로킹; (3) CircularProgressIndicator가 느리다는 느낌 강화 |
| **Solution** | 저장 후 강제 Firestore 재조회; L2 I/O를 Dispatchers.IO로 이동; 월 로드 시 개별 entry 캐시 선채움; CircularProgressIndicator → DiaryDetailSkeleton 교체 |
| **Function/UX Effect** | 수정 후 즉시 최신 데이터 표시; 디스크 읽기 블로킹 제거로 UI 반응성 향상; 로딩 중 콘텐츠 구조 미리 표시로 "느리다" 느낌 제거 |
| **Core Value** | "수정하면 바로, 켜면 바로" |

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 저장 후 구버전 데이터 표시 + 메인 스레드 디스크 I/O = 사용자 체감 속도 저하 |
| **WHO** | 매일 일기를 쓰고 수정하는 조이어리 사용자 |
| **RISK** | 동시성 이슈 (save 중 loadDiaryByDate 경쟁); 스켈레톤 미표시 엣지케이스 |
| **SUCCESS** | 저장 후 최신 데이터 자동 표시; 디스크 I/O IO dispatcher; 스켈레톤 표시 |
| **SCOPE** | DiaryViewModel.kt, DiaryDetailScreen.kt |

## Success Criteria

| ID | 기준 |
|----|------|
| SC-01 | `saveDiary()` 성공 시: L1+L2 무효화 → `_selectedEntry=null` → `_isDetailLoading=true` → Firestore 강제 재조회 후 반영 |
| SC-02 | `loadMonth()` — L2 읽기/쓰기 `withContext(Dispatchers.IO)` 처리 |
| SC-03 | `loadDiaryByDate()` — L2 읽기/쓰기 `withContext(Dispatchers.IO)` 처리 |
| SC-04 | `loadMonth()` Firestore 수신 시 → 월 내 모든 entry를 `memEntryCache`에 선채움 |
| SC-05 | `DiaryDetailSkeleton` 컴포저블 신규 추가 — 이미지/칩/콘텐츠 플레이스홀더 + 알파 애니메이션 |
| SC-06 | `DiaryPageContent` — `isDetailLoading=true` 시 `CircularProgressIndicator` 대신 `DiaryDetailSkeleton` 표시 |
| SC-07 | v7/v8 기능 회귀 없음 (2단 캐시, HorizontalPager, 이미지 30KB) |
