<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# source

## Purpose
Firebase data sources — the only layer that talks directly to Firebase SDKs. Each source wraps one Firebase service.

## Key Files

| File | Description |
|------|-------------|
| `AuthDataSource.kt` | Wraps Firebase Auth — signIn, createUser, signOut, currentUser flow |
| `FirestoreDataSource.kt` | Wraps Firestore — diary entry CRUD, real-time snapshots |
| `StorageDataSource.kt` | Wraps Firebase Storage — upload, download URL retrieval, delete |

## For AI Agents

### Working In This Directory
- These are the only files that import `com.google.firebase.*` directly
- All methods are `suspend fun` or return `Flow` — no callbacks
- Firestore collection path: `users/{userId}/entries/{entryId}`
- Storage path: `users/{userId}/images/{filename}`

### Common Patterns
- `FirestoreDataSource` uses `.snapshotFlow()` for real-time diary list updates
- `StorageDataSource.uploadImage()` returns the download URL after upload completes
- Always check `currentUser != null` before Firestore operations — throw `IllegalStateException` if null

## Dependencies

### External
- Firebase Firestore KTX, Firebase Auth KTX, Firebase Storage KTX

<!-- MANUAL: -->
