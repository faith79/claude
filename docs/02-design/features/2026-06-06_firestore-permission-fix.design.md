# Design: firestore-permission-fix

## 선택 아키텍처: Option C — Pragmatic Balance

### Firestore Rules 설계

컬렉션: `diaries/{diaryId}`

| 작업 | 조건 |
|------|------|
| read | `request.auth != null && resource.data.userId == request.auth.uid` |
| create | `request.auth != null && request.resource.data.userId == request.auth.uid` |
| update | `request.auth != null && resource.data.userId == request.auth.uid` |
| delete | `request.auth != null && resource.data.userId == request.auth.uid` |

> read/update/delete는 기존 문서(resource.data), create는 새 문서(request.resource.data) 기준

### Storage Rules 설계

경로: `images/{userId}/{allPaths=**}`

- `request.auth != null && request.auth.uid == userId`
- 경로의 `{userId}` 변수가 Firebase Auth UID와 일치해야만 접근 허용

### firebase.json 설계

```json
{
  "firestore": {
    "rules": "firestore.rules",
    "indexes": "app/src/main/assets/firestore.indexes.json"
  },
  "storage": {
    "rules": "storage.rules"
  }
}
```

### 배포 방법

```bash
# Firebase CLI 설치 (최초 1회)
npm install -g firebase-tools
firebase login

# diary-app 디렉터리에서 실행
cd diary-app
firebase use my-diary-app-73ca4
firebase deploy --only firestore:rules,storage
```

또는 Firebase Console에서 수동 적용:
- Firestore: https://console.firebase.google.com/project/my-diary-app-73ca4/firestore/rules
- Storage: https://console.firebase.google.com/project/my-diary-app-73ca4/storage/rules
