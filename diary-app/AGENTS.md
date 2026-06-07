<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# diary-app

## Purpose
Android diary app built with Kotlin, Jetpack Compose, and Firebase. Users write daily entries with emotion/weather tags and photos, stored in Firestore. Supports biometric auth, daily notifications, and customizable themes.

## Key Files

| File | Description |
|------|-------------|
| `build.gradle.kts` | Root Gradle build — plugin versions |
| `settings.gradle.kts` | Module declarations |
| `gradle/libs.versions.toml` | Version catalog — all dependency versions in one place |
| `gradle.properties` | JVM args, AndroidX flags |
| `firestore.rules` | Firestore security rules — users can only access their own data |
| `storage.rules` | Firebase Storage security rules |
| `firebase.json` | Firebase project configuration |
| `.firebaserc` | Firebase project alias |
| `INSTALL_GUIDE.md` | Setup instructions for new developers |
| `local.properties` | SDK path (gitignored) |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `app/` | The single Android module (see `app/AGENTS.md`) |
| `gradle/` | Gradle wrapper and version catalog |

## For AI Agents

### Working In This Directory
- Always use `libs.versions.toml` when adding dependencies — never hardcode versions in `build.gradle.kts`
- Firebase rules in `firestore.rules` and `storage.rules` must be deployed with `firebase deploy --only firestore:rules,storage` after changes
- `local.properties` is gitignored — never commit it
- `my-diary-key` is the release keystore — never commit or log its password

### Testing Requirements
- `./gradlew assembleDebug` to build debug APK
- `./gradlew connectedAndroidTest` for instrumented tests (requires connected device/emulator)
- Deploy Firestore rules before testing auth-gated data operations

### Common Patterns
- Clean architecture: data → repository → viewmodel → UI
- Hilt for DI throughout
- All Firebase operations are async (Kotlin coroutines / Flow)

## Dependencies

### External
- Firebase BOM — Firestore, Auth, Storage, Analytics
- Jetpack Compose BOM — UI toolkit
- Hilt — dependency injection
- Coil — image loading (50 MB disk cache, 25% memory cache)
- WorkManager — background tasks (daily reminder notifications)
- AndroidX Biometric — fingerprint/face auth

<!-- MANUAL: -->
