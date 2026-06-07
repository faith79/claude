# memo-save-fix Plan

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 메모 저장 버튼 클릭 시 시트는 닫히나 목록에 메모가 나타나지 않음 |
| **Solution** | ViewModel saveMemo 플로우 수정 + Snackbar 에러 피드백 추가 |
| **Function/UX Effect** | 저장 성공 시 즉시 목록 갱신, 실패 시 원인을 Snackbar로 표시 |
| **Core Value** | 메모 저장 신뢰성 확보 |

## Root Cause

1. `MemoViewModel.saveMemo` → `loadMemos(userId)` 호출이 fire-and-forget 새 코루틴
   - `saveMemo` 예외 시 `loadMemos` 미실행 → 목록 stale
2. `catch (_: Exception) {}` 전체 무시 → 사용자 피드백 없음

## Fix Scope

| 파일 | 변경 |
|------|------|
| `MemoViewModel.kt` | `saveMemo` 플로우 inline 재작성, `_error` StateFlow 추가 |
| `HomeScreen.kt` | `snackbarHostState` 추가, error 수집 + Snackbar 표시 |

## Success Criteria

- SC-01: 저장 성공 시 목록에 즉시 신규 메모 표시
- SC-02: 저장 실패(Firestore 오류 등) 시 Snackbar 에러 메시지 표시
- SC-03: 편집 후 저장 시 기존 항목 갱신
- SC-04: 빌드 성공
