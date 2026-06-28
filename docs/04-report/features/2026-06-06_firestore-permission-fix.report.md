# Report: firestore-permission-fix

## 결과: PASSED ✅ (100%)

## 원인

Firebase Firestore의 **test mode 기간(30일) 만료**로 인해 모든 쓰기 요청이 PERMISSION_DENIED로 차단됨.
앱 코드에는 문제 없음.

## 생성된 파일

| 파일 | 역할 |
|------|------|
| `diary-app/firestore.rules` | Firestore 보안 규칙 — 인증된 사용자만 자신의 일기 CRUD |
| `diary-app/storage.rules` | Storage 보안 규칙 — 인증된 사용자만 자신의 이미지 접근 |
| `diary-app/firebase.json` | Firebase CLI 배포 설정 |
| `diary-app/.firebaserc` | Firebase 프로젝트 ID 지정 |

## 규칙 요약

**Firestore** (`diaries/{diaryId}`):
- create: `request.resource.data.userId == request.auth.uid`
- read/update/delete: `resource.data.userId == request.auth.uid`

**Storage** (`images/{userId}/**`):
- read/write: `request.auth.uid == userId`

## 배포 방법 (필수)

### 방법 A — Firebase CLI (권장)
```bash
npm install -g firebase-tools   # 최초 1회
firebase login                   # 최초 1회
cd diary-app
firebase deploy --only firestore:rules,storage
```

### 방법 B — Firebase Console 수동 적용 (즉시)

1. **Firestore 규칙**:
   - https://console.firebase.google.com/project/my-diary-app-73ca4/firestore/rules
   - 아래 내용으로 교체 후 "게시" 클릭

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /diaries/{diaryId} {
      allow read: if request.auth != null
                  && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null
                    && request.resource.data.userId == request.auth.uid;
      allow update: if request.auth != null
                    && resource.data.userId == request.auth.uid;
      allow delete: if request.auth != null
                    && resource.data.userId == request.auth.uid;
    }
  }
}
```

2. **Storage 규칙**:
   - https://console.firebase.google.com/project/my-diary-app-73ca4/storage/rules
   - 아래 내용으로 교체 후 "게시" 클릭

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /images/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null
                         && request.auth.uid == userId;
    }
  }
}
```
