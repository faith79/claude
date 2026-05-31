# Report: joyary-login-biometric

**완료일**: 2026-05-31  
**Match Rate**: 100% (target: 100%) ✅  
**Status**: PASSED  

---

## 구현 요약

### FR-01: 앱 실행 시 무조건 로그인

`MainActivity.onCreate()` 에서 `@Inject AuthRepository`를 사용해 `signOutImmediate()` 를 한 번 호출, Firebase 세션을 동기적으로 종료한다. `NavGraph` 의 `startDestination` 은 항상 `Screen.Login.route` — 기존 `isLoggedIn` 조건 완전 제거.

**핵심 결정**: Composable 안이 아닌 `onCreate()` 에서 호출 → recompose 시 반복 signOut 방지.

### FR-02: 지문 생체인증 로그인

**자격증명 저장**: 이메일/비밀번호 로그인 성공 직후 `CredentialStorage` (EncryptedSharedPreferences + AES256_GCM MasterKey) 에 암호화 저장.

**지문 버튼 조건**: `canAuthenticate(BIOMETRIC_STRONG) == SUCCESS` AND `hasBiometricCredentials == true` → 첫 로그인 전에는 버튼 미표시.

**인증 흐름**: BiometricPrompt → `onAuthenticationSucceeded` → `signInWithBiometric()` → 저장된 자격증명으로 Firebase signIn.

---

## 변경 파일

| 파일 | 변경 내용 |
|------|---------|
| `gradle/libs.versions.toml` | biometric 1.1.0, security-crypto 1.0.0 추가 |
| `app/build.gradle.kts` | 2개 dependency 추가 |
| `AndroidManifest.xml` | USE_BIOMETRIC permission 추가 |
| `security/CredentialStorage.kt` | NEW — EncryptedSharedPreferences 래퍼 |
| `data/repository/AuthRepository.kt` | 4개 메서드 추가 |
| `data/repository/AuthRepositoryImpl.kt` | CredentialStorage 주입, 4개 메서드 구현 |
| `viewmodel/AuthViewModel.kt` | signOutImmediate, signInWithBiometric, hasBiometricCredentials |
| `ui/auth/LoginScreen.kt` | BiometricPrompt + 지문 OutlinedButton |
| `MainActivity.kt` | @Inject AuthRepository, signOutImmediate in onCreate |

---

## 사용 흐름

```
[앱 시작]
  └─ MainActivity.onCreate()
      ├─ authRepository.signOutImmediate()  ← Firebase 세션 종료
      └─ NavGraph(startDestination = Login)

[로그인 화면]
  ├─ 이메일/비밀번호 로그인 → 성공 → credentials 암호화 저장 → Home
  └─ (두 번째 실행부터) 지문 버튼 표시
      └─ BiometricPrompt 인증 → 저장된 credentials 로 Firebase signIn → Home
```

---

## 아키텍처 레이어 매핑

```
UI Layer         : LoginScreen (BiometricPrompt, showBiometric UI)
ViewModel Layer  : AuthViewModel (signInWithBiometric, hasBiometricCredentials, signOutImmediate)
Repository Layer : AuthRepository (interface) / AuthRepositoryImpl (impl + CredentialStorage)
Data Layer       : AuthDataSource (Firebase) / CredentialStorage (EncryptedSharedPreferences)
```
