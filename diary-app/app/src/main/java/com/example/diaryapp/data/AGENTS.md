<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# data

## Purpose
Data layer — defines domain models, repository interfaces/implementations, remote data sources, and local utilities. ViewModels depend only on repository interfaces, never on data sources directly.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `model/` | Domain model data classes (see `model/AGENTS.md`) |
| `repository/` | Repository interfaces and Hilt-injected implementations (see `repository/AGENTS.md`) |
| `source/` | Firebase data sources — Firestore, Storage, Auth (see `source/AGENTS.md`) |
| `util/` | Local cache and image compression utilities (see `util/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- All repository implementations use constructor-injected data sources (no static calls)
- Data sources are the only classes that import Firebase SDK directly
- Repositories return `Flow<T>` or `suspend fun` — never blocking calls

### Common Patterns
- Interface in `repository/DiaryRepository.kt` → implementation in `repository/DiaryRepositoryImpl.kt`
- Same pattern for Auth: `AuthRepository` / `AuthRepositoryImpl`

## Dependencies

### External
- Firebase Firestore, Firebase Storage, Firebase Auth

<!-- MANUAL: -->
