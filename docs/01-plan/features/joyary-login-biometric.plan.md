# Plan: joyary-login-biometric

## Context Anchor
- **WHY**: 보안 강화 — 앱 실행마다 인증을 요구하고, 생체인증으로 편리성도 제공
- **WHO**: Joyary 앱 사용자 (기존 계정 보유자)
- **RISK**: EncryptedSharedPreferences 초기화 실패 시 생체 로그인 불가 (graceful degradation)
- **SUCCESS**: 앱 실행 시 항상 로그인 화면 표시 + 지문 버튼으로 빠른 로그인 가능
- **SCOPE**: 인증 흐름만 수정, 일기 데이터/UI 변경 없음

## Requirements

### FR-01: 강제 로그인
- 앱 실행(MainActivity.onCreate) 시 Firebase 세션 종료
- 항상 LoginScreen 으로 시작 (isLoggedIn 조건 제거)

### FR-02: 생체인증 로그인
- 이메일/비밀번호 로그인 성공 시 자격증명을 EncryptedSharedPreferences 에 저장
- LoginScreen: 생체 하드웨어 지원 + 저장된 자격증명 있을 때 지문 버튼 표시
- BiometricPrompt 인증 성공 → 저장된 자격증명으로 Firebase signIn

## Files to Change

| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | biometric, security-crypto 버전 추가 |
| `app/build.gradle.kts` | 두 dependency 추가 |
| `AndroidManifest.xml` | USE_BIOMETRIC permission 추가 |
| NEW `security/CredentialStorage.kt` | EncryptedSharedPreferences 래퍼 |
| `data/repository/AuthRepository.kt` | signOutImmediate, hasBiometricCredentials, getBiometricCredentials, saveCredentials 추가 |
| `data/repository/AuthRepositoryImpl.kt` | 새 메서드 구현 |
| `viewmodel/AuthViewModel.kt` | signOutImmediate, signInWithBiometric 추가 |
| `ui/auth/LoginScreen.kt` | BiometricPrompt + 지문 버튼 추가 |
| `MainActivity.kt` | 항상 Login route, signOutImmediate 호출 |

## [CP-1 Auto] 요구사항 확인됨
## [CP-2 Auto] 합리적 기본값 적용 — 생체 없을 때 버튼 숨김, 자격증명 없을 때 버튼 숨김
