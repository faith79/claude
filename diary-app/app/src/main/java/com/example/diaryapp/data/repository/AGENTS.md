<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# repository

## Purpose
Repository interfaces (contracts) and their Hilt-injected implementations. ViewModels depend only on the interfaces — implementations are swappable via DI.

## Key Files

| File | Description |
|------|-------------|
| `DiaryRepository.kt` | Interface — CRUD for diary entries, image operations |
| `DiaryRepositoryImpl.kt` | Implementation using `FirestoreDataSource` and `StorageDataSource` |
| `AuthRepository.kt` | Interface — login, signup, signOut, current user |
| `AuthRepositoryImpl.kt` | Implementation using `AuthDataSource` (Firebase Auth) |

## For AI Agents

### Working In This Directory
- New operations go in the interface first, then implement in `*Impl`
- Implementations coordinate multiple data sources when needed (e.g. delete image from Storage then update Firestore)
- Return `Flow<T>` for read operations, `suspend fun` for writes

### Common Patterns
- `DiaryRepositoryImpl` injects both `FirestoreDataSource` and `StorageDataSource`
- Error propagation: let exceptions bubble up to ViewModel which catches and maps to UI state

## Dependencies

### Internal
- `data/source/` — injected data sources

<!-- MANUAL: -->
