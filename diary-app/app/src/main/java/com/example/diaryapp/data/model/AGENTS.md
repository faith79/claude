<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# model

## Purpose
Domain model data classes. These are the core data structures shared across all layers.

## Key Files

| File | Description |
|------|-------------|
| `DiaryEntry.kt` | Core diary entry — id, userId, content, date (yyyy-MM-dd), emotions, weathers, imageUrls (max 3), timestamps |
| `EmotionTag.kt` | Enum or sealed class for emotion tags (e.g. HAPPY, SAD, ANGRY) |
| `WeatherTag.kt` | Enum or sealed class for weather tags (e.g. SUNNY, CLOUDY, RAINY) |

## For AI Agents

### Working In This Directory
- `DiaryEntry` maps directly to Firestore documents — field names must match Firestore field names exactly
- `emotions` and `weathers` are `List<T>` supporting up to 3 selections each (enforced in UI/ViewModel)
- `imageUrls` holds Firebase Storage download URLs (max 3)
- Do not add Android-specific imports here — models are pure Kotlin data classes

### Common Patterns
- All fields have default values for Firestore deserialization (no-arg constructor requirement)
- `date` is always `"yyyy-MM-dd"` string format — never `Long` timestamp for the calendar date

<!-- MANUAL: -->
