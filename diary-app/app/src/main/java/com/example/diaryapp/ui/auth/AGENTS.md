<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# auth

## Purpose
Login and sign-up Compose screens. Handles email/password auth, biometric unlock, and input validation UI.

## Key Files

| File | Description |
|------|-------------|
| `LoginScreen.kt` | Email/password login form, biometric button (if enrolled), forgot password link |
| `SignUpScreen.kt` | Email/password registration form with validation |
| `AuthUiState.kt` | Sealed class for auth states: Idle, Loading, Success, Error |

## For AI Agents

### Working In This Directory
- Biometric button is shown only when `CredentialStorage` has saved credentials and device supports biometrics
- Screen title scrolls out of view on small screens (login screen has scrollable layout)
- Both screens navigate to `HomeScreen` on `AuthUiState.Success`
- Validation errors shown as field-level text, not Snackbar

### Common Patterns
- `AuthViewModel` is shared between `LoginScreen` and `SignUpScreen` via `hiltViewModel()`
- Keyboard done action triggers form submission

## Dependencies

### Internal
- `viewmodel/AuthViewModel.kt`
- `security/CredentialStorage.kt` (accessed via ViewModel)

<!-- MANUAL: -->
