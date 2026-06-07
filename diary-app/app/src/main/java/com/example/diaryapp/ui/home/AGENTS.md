<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# home

## Purpose
Home screen — combines a monthly calendar with a diary entry list for the selected date.

## Key Files

| File | Description |
|------|-------------|
| `HomeScreen.kt` | Calendar header (month/year nav), day cells colored by entry's background color, diary list below |

## For AI Agents

### Working In This Directory
- Calendar day cells show background color from the diary entry for that date (if exists)
- Tapping a date with an entry navigates to `DiaryDetailScreen`; tapping an empty date opens `DiaryEditorScreen`
- Month swipe uses `HorizontalPager` for performance — pre-load adjacent months
- After returning from the editor, the calendar must refresh to show any new/updated entry color

### Common Patterns
- Calendar state managed in `DiaryViewModel` — selected date, visible month, entry map
- `LaunchedEffect(Unit)` reloads entries when the screen is re-entered (after back navigation)

## Dependencies

### Internal
- `viewmodel/DiaryViewModel.kt`

<!-- MANUAL: -->
