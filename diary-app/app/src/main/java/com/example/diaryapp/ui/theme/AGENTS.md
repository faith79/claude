<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# theme

## Purpose
Material3 theme definition, color tokens, typography, and the custom `LocalThemeColors` composition local for per-entry background colors.

## Key Files

| File | Description |
|------|-------------|
| `Theme.kt` | `DiaryAppTheme` composable — wraps content with Material3 `MaterialTheme` and `LocalThemeColors` |
| `Color.kt` | Color constants for light/dark schemes and diary background palette |
| `Type.kt` | Typography scale using system fonts |
| `LocalThemeColors.kt` | `CompositionLocal` providing the current theme's color set; used for dynamic background colors |
| `AppThemeTemplate.kt` | Template/base for custom theme variants |

## For AI Agents

### Working In This Directory
- Never hardcode colors in screen composables — always reference `MaterialTheme.colorScheme.*` or `LocalThemeColors.current`
- Diary entry background colors are a fixed palette defined in `Color.kt` — do not generate arbitrary colors
- Adding a new color requires updating both light and dark variants

### Common Patterns
- `LocalThemeColors.current.entryBackgrounds` returns the list of selectable background colors
- Theme is read from `ThemePreferences` (DataStore) and applied at the root `DiaryAppTheme` call in `MainActivity`

<!-- MANUAL: -->
