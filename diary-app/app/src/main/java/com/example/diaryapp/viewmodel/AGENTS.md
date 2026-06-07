<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# viewmodel

## Purpose
Hilt ViewModels that mediate between the data layer and the UI. Expose `StateFlow`/`SharedFlow` for UI state and handle user actions as suspend functions or event methods.

## Key Files

| File | Description |
|------|-------------|
| `AuthViewModel.kt` | Login, signup, sign-out, biometric auth, password reset |
| `DiaryViewModel.kt` | CRUD for diary entries, image upload/delete, calendar state, local cache management |
| `SettingsViewModel.kt` | Theme preference, notification toggle/time, account deletion |

## For AI Agents

### Working In This Directory
- All ViewModels are `@HiltViewModel` with `@Inject constructor`
- Expose UI state as `StateFlow<UiState>` — never expose mutable state directly
- Long-running operations launched with `viewModelScope.launch` — handle exceptions and update error state
- `DiaryViewModel` owns the local cache invalidation logic after write operations

### Common Patterns
- `UiState` sealed classes defined in `ui/<feature>/` (e.g. `AuthUiState.kt`)
- Navigation events emitted as `SharedFlow<NavigationEvent>` so they fire exactly once
- Image uploads go through `StorageDataSource` then Firestore update in sequence

## Dependencies

### Internal
- `data/repository/` — injected repository interfaces
- `data/util/DiaryLocalCache.kt` — for cache reads/writes

### External
- Hilt, Kotlin Coroutines, Firebase Auth

<!-- MANUAL: -->
