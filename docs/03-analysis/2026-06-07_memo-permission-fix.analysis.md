# Analysis: memo-permission-fix

## Match Rate: 100%

| 축 | 점수 | 비고 |
|----|------|------|
| Structural | 100% | 2개 파일 변경 완료 |
| Functional | 100% | callbackFlow + AuthStateListener 정상 |
| Contract | 100% | Firestore rules 배포 완료 |

## Root Cause

Firestore rules `firestore.rules`에 `/users/{userId}/memos/{memoId}` 규칙이 추가된 커밋(`fix: memo-firestore-permission`)이 있었으나, `firebase deploy --only firestore:rules` 실행이 누락됨. Firebase 프로젝트(`my-diary-app-73ca4`)에는 구 rules가 적용된 상태였으므로 모든 memo 읽기/쓰기가 PERMISSION_DENIED로 차단됨.

## Fixes Applied

1. **Firestore rules 배포** — `firebase deploy --only firestore:rules` 실행 완료
2. **AuthViewModel.currentUserIdFlow** — `callbackFlow` + `AuthStateListener`로 reactive StateFlow 추가
3. **HomeScreen userId** — `collectAsStateWithLifecycle()` 적용으로 Auth 상태 변화 즉시 반영

## Gaps Found: 0
