<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# security

## Purpose
Biometric authentication and encrypted credential storage. Allows users to unlock the app with fingerprint/face instead of re-entering their password.

## Key Files

| File | Description |
|------|-------------|
| `CredentialStorage.kt` | Stores and retrieves encrypted email/password using AndroidX Security EncryptedSharedPreferences |

## For AI Agents

### Working In This Directory
- Credentials are encrypted at rest via `EncryptedSharedPreferences` — never store raw passwords
- Biometric prompt is shown only if the user has enrolled biometrics and enabled the feature in Settings
- Do not log or display credential values

### Common Patterns
- `CredentialStorage` is singleton-scoped via Hilt
- Biometric authentication flow: check availability → show `BiometricPrompt` → on success, read from `CredentialStorage` → call `AuthViewModel.login()`

## Dependencies

### External
- AndroidX Security Crypto (EncryptedSharedPreferences)
- AndroidX Biometric

<!-- MANUAL: -->
