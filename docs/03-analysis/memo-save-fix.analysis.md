# memo-save-fix Gap Analysis

**Match Rate: 100%** | Iterations: 1

## Root Cause

| # | 원인 | 영향 |
|---|------|------|
| RC-01 | `saveMemo` 내 `loadMemos()` 호출이 fire-and-forget (새 viewModelScope.launch) | 예외 발생 시 reload 미실행 |
| RC-02 | `catch (_: Exception) {}` 전체 무시 | 사용자 피드백 없음, 디버깅 불가 |

## Functional Fixes

| 항목 | 변경 | 상태 |
|------|------|------|
| `saveMemo` 순차 플로우 | `loadMemos()` → `_memos.value = repo.getMemos()` 인라인 | ✅ |
| `_error` StateFlow 추가 | 저장/로드 실패 메시지 노출 | ✅ |
| `loadMemos` 오류 노출 | `catch (e)` → `_error.value = ...` | ✅ |
| `HomeScreen` Snackbar | `snackbarHostState` + `LaunchedEffect(error)` + `SnackbarHost` | ✅ |
| Build | BUILD SUCCESSFUL (22.4MB) | ✅ |

## Overall: 100% ✅
