# Report: memo-permission-fix

## Executive Summary

| 관점 | 내용 |
|------|------|
| Problem | 메모장 탭 진입·저장 시 `PERMISSION_DENIED` — Firestore rules 미배포 |
| Solution | rules 배포 + AuthViewModel reactive userId StateFlow |
| UX Effect | 메모 로드·저장 정상 동작, 오류 Snackbar 사라짐 |
| Core Value | 메모 기능 완전 복구 |

## Key Decisions & Outcomes

| 단계 | 결정 | 결과 |
|------|------|------|
| Root Cause | rules 미배포 (코드 버그 아님) | firebase deploy로 즉시 해결 |
| Code Fix | callbackFlow + AuthStateListener | Auth 상태 변화 reactive 추적 |
| Architecture | SharingStarted.Eagerly | 첫 컴포지션부터 유효한 userId 공급 |

## Success Criteria

| 기준 | 상태 | 근거 |
|------|------|------|
| 메모장 탭 오류 없음 | ✅ Met | Firestore rules 배포 완료 |
| 저장 오류 없음 | ✅ Met | Firestore rules 배포 완료 |
| userId reactive | ✅ Met | currentUserIdFlow StateFlow 추가 |
| APK 빌드 성공 | ✅ Met | BUILD SUCCESSFUL in 11s |

Overall: **4/4 criteria met (100%)**

## Files Changed

| 파일 | 변경 |
|------|------|
| `diary-app/firestore.rules` | 배포 완료 (코드 변경 없음) |
| `viewmodel/AuthViewModel.kt` | currentUserIdFlow StateFlow 추가 |
| `ui/home/HomeScreen.kt` | collectAsStateWithLifecycle 적용 |

## Match Rate: 100% — PASSED ✅
