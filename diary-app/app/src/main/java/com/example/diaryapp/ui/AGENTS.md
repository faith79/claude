<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# ui

## Purpose
All Jetpack Compose screens and reusable UI components. Organized by feature area. Screens observe ViewModel state and emit UI events — no business logic here.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `auth/` | Login and signup screens (see `auth/AGENTS.md`) |
| `components/` | Shared reusable composables (see `components/AGENTS.md`) |
| `diary/` | Diary detail view and editor screens (see `diary/AGENTS.md`) |
| `home/` | Home screen — calendar + diary list (see `home/AGENTS.md`) |
| `settings/` | Settings screen (see `settings/AGENTS.md`) |
| `theme/` | Material3 theme, colors, typography (see `theme/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- Screens receive a `NavController` and a `ViewModel` (injected via `hiltViewModel()`)
- No direct Firebase/repository calls in UI — all through ViewModel
- Use `LaunchedEffect` for one-shot side effects (navigation after save, snackbar on error)
- Screen orientation is locked to portrait — no landscape layout needed

### Common Patterns
- Screens collect ViewModel state with `collectAsStateWithLifecycle()`
- Loading states use `LoadingOverlay` composable from `components/`
- Error states shown via `Snackbar` through `SnackbarHostState`

## Dependencies

### Internal
- `viewmodel/` — all ViewModels
- `navigation/Screen.kt` — route constants

### External
- Jetpack Compose, Material3, Coil (AsyncImage)

<!-- MANUAL: -->
