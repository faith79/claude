<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# diary

## Purpose
Diary detail view (read-only) and editor (create/edit) screens.

## Key Files

| File | Description |
|------|-------------|
| `DiaryDetailScreen.kt` | Read-only view of a diary entry — content, emotions, weather, images; swipe to navigate adjacent dates |
| `DiaryEditorScreen.kt` | Create/edit form — text input, emotion/weather chip selectors, image picker, background color picker |

## For AI Agents

### Working In This Directory
- `DiaryEditorScreen` validates: content not empty, emotions ≤ 3, weathers ≤ 3, images ≤ 3
- Background color for each entry is stored in `DiaryEntry` and applied to the editor/detail scaffold
- After save, navigate back to Home AND refresh the calendar cell for the saved date
- `DiaryDetailScreen` supports horizontal swipe to navigate to previous/next date entries

### Common Patterns
- Editor passes `entryId = null` for new entries, non-null for edits
- Image deletion in editor: remove from local list immediately, delete from Storage on save

## Dependencies

### Internal
- `viewmodel/DiaryViewModel.kt`
- `components/MultiImagePicker.kt`, `components/WeatherSelector.kt`, `components/LoadingOverlay.kt`

<!-- MANUAL: -->
