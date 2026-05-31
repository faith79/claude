# Design: emotion-weather-limit-required

## Architecture: Option C — Pragmatic Balance ✅

---

## CHANGE-01: DiaryEditorScreen.kt — 저장 버튼 필수 조건 추가

```kotlin
// Before
enabled = content.isNotBlank() && !isLoading

// After
enabled = content.isNotBlank() && selectedEmotions.isNotEmpty()
       && selectedWeathers.isNotEmpty() && !isLoading
```

---

## CHANGE-02: DiaryEditorScreen.kt — EmotionSelector 호출부 maxReached 전달

```kotlin
EmotionSelector(
    selected = selectedEmotions,
    onSelect = { em ->
        selectedEmotions = if (em in selectedEmotions) selectedEmotions - em else selectedEmotions + em
    },
    maxReached = selectedEmotions.size >= 3   // 신규 파라미터
)
```

---

## CHANGE-03: DiaryEditorScreen.kt — WeatherSelector 호출부 maxReached 전달

```kotlin
WeatherSelector(
    selected = selectedWeathers,
    onSelect = { w ->
        selectedWeathers = if (w in selectedWeathers) selectedWeathers - w else selectedWeathers + w
    },
    maxReached = selectedWeathers.size >= 3   // 신규 파라미터
)
```

---

## CHANGE-04: DiaryEditorScreen.kt — EmotionSelector 컴포저블 업데이트

```kotlin
@Composable
private fun EmotionSelector(
    selected: Set<EmotionTag>,
    onSelect: (EmotionTag) -> Unit,
    maxReached: Boolean = false    // 신규
) {
    Column {
        // 헤더: "오늘의 감정 (N/3)"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("오늘의 감정", style = labelMedium, color = onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            Text("(${selected.size}/3)", style = labelSmall,
                color = if (selected.isEmpty()) colorScheme.error else onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Row(...) {
            EmotionTag.entries.forEach { emotion ->
                val isSelected = emotion in selected
                val isClickable = isSelected || !maxReached
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(if (!isSelected && maxReached) 0.38f else 1f)   // 신규
                        .clip(...)
                        .background(...)
                        .border(...)
                        .clickable(enabled = isClickable) { onSelect(emotion) }  // enabled 조건
                        .padding(vertical = 8.dp)
                ) { ... }
            }
        }
    }
}
```

---

## CHANGE-05: WeatherSelector.kt — maxReached 파라미터 + 카운트 헤더 + disabled

```kotlin
@Composable
fun WeatherSelector(
    selected: Set<WeatherTag>,
    onSelect: (WeatherTag) -> Unit,
    modifier: Modifier = Modifier,
    maxReached: Boolean = false    // 신규
) {
    Column(modifier = modifier) {
        // 헤더: "날씨 (N/3)"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("날씨", style = labelMedium, color = onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            Text("(${selected.size}/3)", style = labelSmall,
                color = if (selected.isEmpty()) colorScheme.error else onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(...) {
            items(WeatherTag.entries) { weather ->
                val isSelected = weather in selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(weather) },
                    enabled = isSelected || !maxReached,   // 신규: 미선택+maxReached → disabled
                    label = { Text("${weather.emoji} ${weather.label}") },
                    ...
                )
            }
        }
    }
}
```

## 필요 추가 import

- `DiaryEditorScreen.kt`: `import androidx.compose.ui.draw.alpha` (Modifier.alpha() 용)
- `WeatherSelector.kt`: `import androidx.compose.foundation.layout.Row`, `import androidx.compose.ui.unit.dp` (already), `import androidx.compose.ui.Alignment`
