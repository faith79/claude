<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# app

## Purpose
The single Android application module. Contains all source code, resources, and the module-level Gradle build file.

## Key Files

| File | Description |
|------|-------------|
| `build.gradle.kts` | Module build config — compileSdk, minSdk (26), dependencies, Hilt/KSP plugins |
| `google-services.json` | Firebase project config (contains API keys — do not log or expose) |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `src/main/` | Production source code and resources |
| `src/main/java/com/example/diaryapp/` | Kotlin source root (see nested AGENTS.md) |
| `src/main/assets/` | Bundled assets — Lottie animation, Firestore index config |
| `src/main/res/` | Android resources — drawables, mipmaps, values |

## For AI Agents

### Working In This Directory
- `minSdk = 26` (Android 8.0) — do not use APIs newer than this without SDK version checks
- KSP (not KAPT) is used for annotation processing — Hilt, Room if added later
- `google-services.json` must stay in this directory for Firebase to initialize

### Testing Requirements
- `./gradlew :app:assembleDebug` from `diary-app/` root
- `./gradlew :app:lint` for static analysis

### Common Patterns
- All feature code lives under `src/main/java/com/example/diaryapp/`
- Resources follow Material3 naming conventions

## Dependencies

### Internal
- All source packages under `src/main/java/com/example/diaryapp/`

<!-- MANUAL: -->
