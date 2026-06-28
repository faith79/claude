# joyary-upgrade-v8 Plan

> **Phase**: Plan | **Date**: 2026-05-23 | **Threshold**: 98%

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 인메모리 캐시만 존재해 앱 재시작 시 Firestore 재호출; 이미지 100KB로 로딩 느림; Coil 이미지 캐시 미설정 |
| **Solution** | `DiaryLocalCache` 파일 기반 24h 디스크 캐시(L2) + 기존 메모리(L1) 2단 캐시; 이미지 30KB 압축; Coil 50MB 디스크캐시 명시 설정 |
| **Function/UX Effect** | 앱 재시작 후에도 달력·상세 즉시 표시; 이미지 다운로드 67% 단축; Coil 반복 로딩 제거 |
| **Core Value** | "앱 켜자마자 즉시 보이는 조이어리" |

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 앱 재시작마다 Firestore 재조회 → 달력 로딩 느림; 100KB 이미지 → 느린 표시 |
| **WHO** | 매일 사용하는 조이어리 사용자 |
| **RISK** | 파일 I/O 예외 처리; 직렬화 오류; Coil API 변경 |
| **SUCCESS** | 앱 재시작 후 달력 즉시 표시; 이미지 ≤30KB; Coil 50MB 디스크캐시 |
| **SCOPE** | DiaryLocalCache.kt(신규), DiaryViewModel.kt, ImageCompressor.kt, DiaryApp.kt |

## Success Criteria

| ID | 기준 |
|----|------|
| SC-01 | `DiaryLocalCache.kt` 생성 — 파일 기반 24h 디스크 캐시 |
| SC-02 | `loadMonth`: L1(메모리) → L2(디스크) → Firestore 순서 |
| SC-03 | `loadDiaryByDate`: L1 → L2 → Firestore 순서 |
| SC-04 | `invalidateCache`: 메모리 + 디스크 동시 무효화 |
| SC-05 | `init { localCache.cleanupExpired() }` 앱 시작 시 만료 파일 삭제 |
| SC-06 | `ImageCompressor.maxSizeBytes = 30_720L` (30KB) |
| SC-07 | `ImageCompressor.maxDimensions = [640, 480, 320, 160]` + startQuality=75 |
| SC-08 | `DiaryApp` — Coil 디스크캐시 50MB 명시 설정 |
| SC-09 | `DiaryApp` — Coil 메모리캐시 25% 명시 설정 |
| SC-10 | v7 기능 회귀 없음 (이미지 스와이프·TTL 구조 유지) |
