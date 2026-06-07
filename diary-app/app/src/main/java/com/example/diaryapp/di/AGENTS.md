<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# di

## Purpose
Hilt dependency injection modules. Binds interfaces to their implementations and provides singleton Firebase/system objects.

## Key Files

| File | Description |
|------|-------------|
| `DataSourceModule.kt` | Provides `FirestoreDataSource`, `StorageDataSource`, `AuthDataSource` |
| `RepositoryModule.kt` | Binds `DiaryRepository` → `DiaryRepositoryImpl`, `AuthRepository` → `AuthRepositoryImpl` |
| `NotificationModule.kt` | Provides `NotificationManager`, `WorkManager` for injection |

## For AI Agents

### Working In This Directory
- Add a new `@Provides` or `@Binds` here when introducing a new injectable dependency
- All modules are `@InstallIn(SingletonComponent::class)` unless a narrower scope is justified
- Never instantiate dependencies manually in constructors — always inject via Hilt

### Common Patterns
- `@Binds` for interface → implementation pairs
- `@Provides` for third-party or system objects (Firebase, WorkManager)

## Dependencies

### Internal
- `data/repository/`, `data/source/`, `notification/`

<!-- MANUAL: -->
