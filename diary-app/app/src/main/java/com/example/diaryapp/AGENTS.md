<!-- Parent: ../../../../../../../../../../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# diaryapp

## Purpose
Root Kotlin package for the diary Android app. Hosts the Application class, MainActivity, and organizes all code into clean-architecture layers.

## Key Files

| File | Description |
|------|-------------|
| `DiaryApp.kt` | Hilt application class; configures WorkManager with HiltWorkerFactory and Coil image loader (50 MB disk cache, 25% memory) |
| `MainActivity.kt` | Single-activity entry point; hosts the Compose NavHost |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `data/` | Data layer — models, repositories, data sources, utilities (see `data/AGENTS.md`) |
| `di/` | Hilt dependency injection modules (see `di/AGENTS.md`) |
| `navigation/` | Navigation graph and screen route definitions (see `navigation/AGENTS.md`) |
| `notification/` | Daily reminder notifications and theme/notification preferences (see `notification/AGENTS.md`) |
| `security/` | Biometric auth and encrypted credential storage (see `security/AGENTS.md`) |
| `ui/` | All Compose screens and reusable components (see `ui/AGENTS.md`) |
| `viewmodel/` | ViewModels — bridge between data layer and UI (see `viewmodel/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- Never add business logic to `MainActivity` or `DiaryApp` — keep them thin
- All screen navigation is declarative via `NavGraph.kt`
- Hilt must be set up end-to-end: `@HiltAndroidApp` → `@AndroidEntryPoint` → `@HiltViewModel`

### Common Patterns
- Clean architecture: `data/` ← `viewmodel/` ← `ui/`
- Every feature follows the same layering — no shortcuts directly from UI to data sources
- `Design Ref:` comments in source files point to the design document section that drove the change

## Dependencies

### Internal
- All subpackages are internal to this module

### External
- Hilt, Coil, WorkManager, Jetpack Compose, Firebase

<!-- MANUAL: -->
