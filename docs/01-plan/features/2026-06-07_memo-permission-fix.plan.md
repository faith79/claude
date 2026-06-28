# Plan: memo-permission-fix

## Executive Summary

| 관점 | 내용 |
|------|------|
| Problem | 메모장 탭 진입 및 저장 시 `PERMISSION_DENIED: Missing or insufficient permissions` 오류 |
| Solution | Firestore rules 배포 + AuthViewModel currentUserId StateFlow 반응형 전환 |
| UX Effect | 메모 로드/저장 정상 동작, 권한 오류 Snackbar 제거 |
| Core Value | 메모 기능 정상 사용 가능 |

## Context Anchor

| 항목 | 내용 |
|------|------|
| WHY | 메모 Firestore rules가 로컬에만 존재하고 Firebase에 배포되지 않아 모든 memos 읽기/쓰기 차단 |
| WHO | 조이어리 앱 사용자 (로그인 후 메모장 탭 이용) |
| RISK | rules 배포 없이는 코드 수정만으로 해결 불가 |
| SUCCESS | 메모장 탭 진입·저장 시 오류 없음 |
| SCOPE | firestore.rules 배포 + AuthViewModel 반응형 userId |

## 1. 문제 분석

### 근본 원인
1. **Firestore rules 미배포**: `fix: memo-firestore-permission` 커밋에서 `firestore.rules`에 `users/{userId}/memos/{memoId}` 규칙이 추가됐으나, `firebase deploy --only firestore:rules`가 실행되지 않아 Firebase 프로젝트에 반영되지 않음
2. **currentUserId 비반응형**: `HomeScreen`의 `val userId = authViewModel.currentUserId`가 plain String이므로, Firebase Auth 상태 변화 시 Compose가 재구성되지 않으면 stale 값을 사용할 수 있음

### 증상
- 메모장 탭 진입 시: `메모 로드 실패: PERMISSION_DENIED`
- 저장 버튼 누름 시: `저장 실패: PERMISSION_DENIED: Missing or insufficient permissions.`

## 2. 요구사항

- [x] Firestore rules를 Firebase 프로젝트에 배포
- [ ] AuthViewModel에 `currentUserIdFlow: StateFlow<String>` 추가
- [ ] HomeScreen에서 userId를 StateFlow로 수집

## 3. 범위

| 파일 | 변경 유형 |
|------|----------|
| `diary-app/firestore.rules` | 배포 완료 (코드 변경 없음) |
| `viewmodel/AuthViewModel.kt` | StateFlow 추가 |
| `ui/home/HomeScreen.kt` | collectAsStateWithLifecycle 적용 |
