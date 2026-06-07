# Design: memo-permission-fix

## Context Anchor

| 항목 | 내용 |
|------|------|
| WHY | memos Firestore rules 미배포로 PERMISSION_DENIED 발생 |
| WHO | 로그인 사용자 (메모장 탭 사용) |
| RISK | 배포 없이 코드만으로 해결 불가 (배포 완료) |
| SUCCESS | 메모 로드·저장 오류 없음 |
| SCOPE | AuthViewModel + HomeScreen (2 파일) |

## 1. 선택 아키텍처: Option C — Pragmatic Balance

### 변경 내용

**AuthViewModel.kt**
- `currentUserIdFlow: StateFlow<String>` 추가
- `FirebaseAuth.AuthStateListener`를 `callbackFlow`로 래핑하여 auth 상태 변화를 반응형으로 노출
- `.stateIn(viewModelScope, SharingStarted.Eagerly, currentUserId)` 로 즉시 시작

**HomeScreen.kt**
- `val userId = authViewModel.currentUserId` →  
  `val userId by authViewModel.currentUserIdFlow.collectAsStateWithLifecycle()`
- 나머지 로직 변경 없음

### 왜 이 접근인가
- `callbackFlow` + `addAuthStateListener`는 Firebase Auth 공식 패턴
- `SharingStarted.Eagerly`로 초기값 즉시 제공 → 첫 컴포지션에서 null 없음
- HomeScreen 변경은 1줄 — 기존 LaunchedEffect/saveMemo 로직 그대로 재사용

## 2. 구현 상세

### AuthViewModel.kt 추가
```kotlin
val currentUserIdFlow: StateFlow<String> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser?.uid ?: "")
    }
    FirebaseAuth.getInstance().addAuthStateListener(listener)
    awaitClose { FirebaseAuth.getInstance().removeAuthStateListener(listener) }
}.stateIn(viewModelScope, SharingStarted.Eagerly, currentUserId)
```

### HomeScreen.kt 변경
```kotlin
// Before
val userId = authViewModel.currentUserId

// After  
val userId by authViewModel.currentUserIdFlow.collectAsStateWithLifecycle()
```
