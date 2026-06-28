# joyary-upgrade-v10 Plan

> **Feature**: joyary-upgrade-v10
> **Type**: 버그 수정
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-23
> **Threshold**: 100%

---

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 일기 수정 후 상세화면 복귀 시 구버전(캐시) 데이터가 표시됨. DiaryDetailScreen과 DiaryEditorScreen이 각각 별도 DiaryViewModel 인스턴스를 가지므로 EditorScreen의 캐시 무효화·강제 재조회가 DetailScreen에 전달되지 않음 |
| **Solution** | NavGraph.kt에서 DiaryDetail·DiaryEditor composable이 Activity 스코프 DiaryViewModel을 공유하도록 변경 |
| **Function/UX Effect** | 수정 저장 후 상세화면 복귀 시 즉시 최신 데이터(스켈레톤→실제값) 표시 |
| **Core Value** | "수정하면 바로 반영" |

---

## Context Anchor

| 항목 | 내용 |
|------|------|
| **WHY** | 일기 수정 후 구버전이 보여 신뢰성 저하 |
| **WHO** | 일기 수정 기능 사용자 |
| **RISK** | Activity 스코프 ViewModel 공유 시 HomeScreen 등 다른 화면과의 상태 격리 필요 |
| **SUCCESS** | 수정 저장 후 DetailScreen에 최신 데이터 표시 (SC-01) |
| **SCOPE** | NavGraph.kt 수정 1개 파일. DiaryViewModel/DiaryDetailScreen/DiaryEditorScreen 로직 변경 없음 |

---

## 1. Problem Analysis

### 1.1 Root Cause

```
현재 구조:
  NavGraph → DiaryDetailScreen → hiltViewModel() → vm_detail (BackStackEntry 스코프)
  NavGraph → DiaryEditorScreen → hiltViewModel() → vm_editor (BackStackEntry 스코프)

vm_editor.saveDiary().onSuccess:
  ✅ vm_editor.invalidateCache()   — vm_editor의 memEntryCache 제거
  ✅ vm_editor._selectedEntry = null
  ✅ vm_editor._isDetailLoading = true
  ✅ vm_editor: Firestore 강제 재조회

popBackStack() → DiaryDetailScreen 복귀:
  ❌ vm_detail._selectedEntry 변경 없음  ← 다른 인스턴스!
  ❌ vm_detail.memEntryCache[key] = 구버전 Entry 유지
  ❌ LaunchedEffect(pagerState.settledPage, userId) 재실행 안됨 (key 동일)
  ❌ 구버전 데이터 표시
```

### 1.2 Fix

```
수정 후 구조:
  NavGraph → DiaryDetailScreen → hiltViewModel(activity) → vm_shared (Activity 스코프)
  NavGraph → DiaryEditorScreen → hiltViewModel(activity) → vm_shared (동일 인스턴스)

vm_shared.saveDiary().onSuccess:
  ✅ vm_shared.invalidateCache()   — 공유 memEntryCache 제거
  ✅ vm_shared._selectedEntry = null  ← DiaryDetailScreen이 관찰하는 동일 Flow!
  ✅ vm_shared._isDetailLoading = true → 스켈레톤 표시
  ✅ vm_shared: Firestore 강제 재조회 → _selectedEntry = 최신 Entry
  ✅ DiaryDetailScreen 즉시 최신 데이터 표시
```

---

## 2. Success Criteria

| # | 기준 | 검증 방법 |
|---|------|---------|
| SC-01 | 수정 저장 후 DetailScreen 복귀 시 최신 데이터 표시 | 코드 리뷰 (ViewModel 스코프 확인) |
| SC-02 | 스켈레톤 → 실제 데이터 순서로 표시 (isDetailLoading 플로우) | 코드 리뷰 |
| SC-03 | DiaryEditorScreen 초기 데이터 로드 정상 작동 | 코드 리뷰 |
| SC-04 | v7/v8/v9 기능 회귀 없음 | 코드 리뷰 |

---

## 3. Scope

**수정 파일**: `navigation/NavGraph.kt` (1개 파일)

**변경 내용**:
- `DiaryDetail` composable: `hiltViewModel()` → `hiltViewModel(activity)`
- `DiaryEditor` composable: `hiltViewModel()` → `hiltViewModel(activity)`
- `DiaryViewModel` 인스턴스를 composable 파라미터로 전달

**변경 없음**: DiaryViewModel.kt, DiaryDetailScreen.kt, DiaryEditorScreen.kt

---

## 4. Risk

| 리스크 | 영향 | 대응 |
|--------|------|------|
| HomeScreen이 별도 ViewModel 사용 | HomeScreen 캘린더 캐시는 별도 인스턴스 유지 — 이번 범위 밖 | 해당 없음 (별도 이슈) |
| Activity 스코프 ViewModel 생명주기 | Activity 재생성(회전 등) 시 ViewModel 유지됨 — 정상 동작 | 이미 Hilt ViewModel이 처리 |
