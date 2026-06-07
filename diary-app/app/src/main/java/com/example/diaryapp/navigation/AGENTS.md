<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# navigation

## Purpose
Defines all navigation routes and the single Compose NavHost. All screen-to-screen navigation goes through here.

## Key Files

| File | Description |
|------|-------------|
| `Screen.kt` | Sealed class / object with route string constants for every screen |
| `NavGraph.kt` | Compose `NavHost` wiring screens to routes; handles auth state redirect |

## For AI Agents

### Working In This Directory
- Add new screens to `Screen.kt` first (route definition), then wire in `NavGraph.kt`
- Auth guard logic lives in `NavGraph.kt` — unauthenticated users are redirected to `LoginScreen`
- Pass only primitive/serializable arguments via nav routes; use ViewModel shared state for complex objects

### Common Patterns
- `Screen.DiaryDetail.createRoute(entryId)` style helper functions for parameterized routes

## Dependencies

### Internal
- `ui/` — all screen composables

<!-- MANUAL: -->
