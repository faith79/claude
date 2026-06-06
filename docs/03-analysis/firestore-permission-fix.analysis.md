# Analysis: firestore-permission-fix

## Match Rate: 100% ✅

| Axis | Score | Notes |
|------|-------|-------|
| Structural (20%) | 100% | firestore.rules, storage.rules, firebase.json, .firebaserc 모두 생성 |
| Functional (40%) | 100% | 규칙이 앱 데이터 구조와 정확히 일치 |
| Contract (40%) | 100% | DiaryEntryDto.userId ↔ rules userId 필드 일치 |
| **Overall** | **100%** | |

## Root Cause

Firebase Firestore test mode 30일 만료 → 모든 쓰기 요청 PERMISSION_DENIED

## Fix Applied

- `firestore.rules`: diaries/{diaryId} — create는 request.resource.data, read/update/delete는 resource.data 기준 userId 검증
- `storage.rules`: images/{userId}/** — path의 userId와 request.auth.uid 일치 검증
- `firebase.json`: 로컬 규칙 파일 연결
- `.firebaserc`: 프로젝트 ID my-diary-app-73ca4 지정

## 코드 변경 없음

앱 코드(FirestoreDataSource, DiaryViewModel 등)는 정상이며 수정 불필요.
