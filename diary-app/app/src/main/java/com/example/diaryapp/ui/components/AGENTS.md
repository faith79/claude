<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# components

## Purpose
Shared reusable Compose components used across multiple screens.

## Key Files

| File | Description |
|------|-------------|
| `LoadingOverlay.kt` | Full-screen semi-transparent overlay with a `CircularProgressIndicator` |
| `MultiImagePicker.kt` | Image picker component supporting up to 3 images; shows thumbnails with delete buttons |
| `WeatherSelector.kt` | Chip-based weather tag selector; enforces max 3 selections |

## For AI Agents

### Working In This Directory
- `MultiImagePicker` enforces the 3-image limit visually — add button hidden when limit reached
- `WeatherSelector` and emotion selector share the same "max 3" enforcement pattern
- `LoadingOverlay` should be shown above all content (use `Box` with `zIndex`) 

### Common Patterns
- Components accept callbacks (`onImageSelected`, `onWeatherChanged`) — no ViewModel access inside components
- Stateless where possible — state hoisted to screen composable

<!-- MANUAL: -->
