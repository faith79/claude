<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# notification

## Purpose
Daily reminder notification system and user preferences storage (notification time, dark/light theme).

## Key Files

| File | Description |
|------|-------------|
| `DailyReminderWorker.kt` | WorkManager `CoroutineWorker` that fires a daily reminder notification |
| `NotificationPreferences.kt` | DataStore-backed storage for reminder time and enabled/disabled toggle |
| `ThemePreferences.kt` | DataStore-backed storage for app theme selection (light/dark/system) |

## For AI Agents

### Working In This Directory
- `DailyReminderWorker` is a `@HiltWorker` — inject dependencies via `@AssistedInject`
- Notification channel must be created before posting (handled in `DiaryApp.onCreate`)
- Reschedule the WorkManager job after preference changes — don't rely on the old schedule

### Common Patterns
- Preferences are read as `Flow<T>` via DataStore
- WorkManager periodic work uses `ExistingPeriodicWorkPolicy.UPDATE` to replace on reschedule

## Dependencies

### External
- WorkManager (Hilt integration)
- AndroidX DataStore

<!-- MANUAL: -->
