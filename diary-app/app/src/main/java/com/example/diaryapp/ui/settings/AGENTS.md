<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# settings

## Purpose
Settings screen — theme selection, notification configuration, biometric toggle, account management.

## Key Files

| File | Description |
|------|-------------|
| `SettingsScreen.kt` | Theme picker (light/dark/system), daily reminder toggle + time picker, biometric enable/disable, sign-out, account delete |

## For AI Agents

### Working In This Directory
- Theme change takes effect immediately (no app restart needed) via `LocalThemeColors`
- Notification time change reschedules the WorkManager job — call `SettingsViewModel.rescheduleReminder()`
- Account deletion: delete all Firestore data + Storage images first, then delete Firebase Auth user
- Biometric toggle saves to `CredentialStorage` — show toggle only if device supports biometrics

### Common Patterns
- Time picker uses Material3 `TimePickerDialog` composable
- Confirmation dialogs before destructive actions (sign-out, account delete)

## Dependencies

### Internal
- `viewmodel/SettingsViewModel.kt`

<!-- MANUAL: -->
