# Design: joyary-login-biometric

## Architecture: Option C — Pragmatic Balance

기존 클린 아키텍처 레이어를 유지하면서 최소 변경으로 두 기능을 구현한다.

---

## §1. 강제 로그인 (FR-01)

### §1.1 변경점
`MainActivity.kt` 에서 `isLoggedIn` 조건 제거, `signOutImmediate()` 호출:

```
Before: val start = if (authViewModel.isLoggedIn) Screen.Home.route else Screen.Login.route
After:  authViewModel.signOutImmediate()
        val start = Screen.Login.route
```

### §1.2 signOutImmediate 체인
Firebase `auth.signOut()` 은 동기 함수이므로 suspend 불필요:

```
AuthDataSource.signOut()  // = auth.signOut() (sync)
    ↑
AuthRepository.signOutImmediate()  // non-suspend
    ↑
AuthViewModel.signOutImmediate()   // sets _uiState = Idle
    ↑
MainActivity.setContent { authViewModel.signOutImmediate() }
```

---

## §2. 생체인증 로그인 (FR-02)

### §2.1 의존성
```toml
biometric = "1.1.0"
security-crypto = "1.0.0"
```
```kotlin
implementation("androidx.biometric:biometric:1.1.0")
implementation("androidx.security:security-crypto:1.0.0")
```

### §2.2 CredentialStorage
`security/CredentialStorage.kt` — `@Singleton`, `@ApplicationContext` 주입:
- `MasterKey.KeyScheme.AES256_GCM` 으로 마스터 키 생성
- `EncryptedSharedPreferences` 로 email/password 저장
- `hasCredentials()`, `getEmail()`, `getPassword()`, `clearCredentials()`

### §2.3 AuthRepository 확장
```kotlin
interface AuthRepository {
    // 기존
    fun signOutImmediate()           // NEW: 동기 signOut
    fun saveCredentials(email, pwd)  // NEW: 자격증명 저장
    fun hasBiometricCredentials()    // NEW: 저장 여부 확인
    fun getBiometricCredentials()    // NEW: Pair<email,pwd>? 반환
}
```

### §2.4 AuthViewModel 확장
```kotlin
val hasBiometricCredentials: Boolean get() = authRepository.hasBiometricCredentials()

fun signIn(email, password) {
    // 로그인 성공 시 saveCredentials 추가 호출
}

fun signInWithBiometric() {
    val (email, pwd) = authRepository.getBiometricCredentials() ?: return Error
    viewModelScope.launch { authRepository.signIn(email, pwd) ... }
}

fun signOutImmediate() {
    authRepository.signOutImmediate()
    _uiState.value = AuthUiState.Idle
}
```

### §2.5 LoginScreen BiometricPrompt

```
showBiometric = canAuthenticate(BIOMETRIC_STRONG) == SUCCESS
             && viewModel.hasBiometricCredentials

BiometricPrompt(FragmentActivity, executor, callback) {
    onAuthenticationSucceeded → viewModel.signInWithBiometric()
    onAuthenticationError     → viewModel.resetState() (user cancel 제외)
}

UI:
┌─────────────────────────────┐
│  이메일         [          ]│
│  비밀번호       [          ]│
│                             │
│  [       로그인       ]     │
│  [🔍 지문으로 로그인  ]  ← showBiometric 시만 표시
│                             │
│  계정이 없으신가요? 회원가입  │
└─────────────────────────────┘
```

### §2.6 AndroidManifest
```xml
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
```

---

## §3. 파일 변경 요약

| 파일 | 변경 유형 | 핵심 변경 |
|------|----------|----------|
| `libs.versions.toml` | 수정 | biometric/security-crypto 버전 추가 |
| `build.gradle.kts` | 수정 | 2개 dependency 추가 |
| `AndroidManifest.xml` | 수정 | USE_BIOMETRIC permission |
| `security/CredentialStorage.kt` | 신규 | EncryptedSharedPreferences 래퍼 |
| `AuthRepository.kt` | 수정 | 4개 메서드 추가 |
| `AuthRepositoryImpl.kt` | 수정 | CredentialStorage 주입 + 구현 |
| `AuthViewModel.kt` | 수정 | signOutImmediate, signInWithBiometric |
| `LoginScreen.kt` | 수정 | BiometricPrompt + 지문 버튼 |
| `MainActivity.kt` | 수정 | 강제 Login 시작 |
