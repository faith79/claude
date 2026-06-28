# Design: multi-emotion-weather-select

## Architecture: Option C — Pragmatic Balance ✅

기존 코드 패턴 최대 유지 + 최소 변경으로 다중 선택 구현.

---

## CHANGE-01: DiaryEntry.kt — 모델 필드 교체

```kotlin
// Before
val emotion: EmotionTag? = null
val weather: WeatherTag? = null

// After
val emotions: List<EmotionTag> = emptyList()
val weathers: List<WeatherTag> = emptyList()
```

---

## CHANGE-02: FirestoreDataSource.kt — DTO 확장 + legacy fallback

```kotlin
data class DiaryEntryDto(
    ...
    val emotions: List<String> = emptyList(),  // 신규 다중 필드
    val emotion: String? = null,               // 레거시 단일 필드 — 읽기 전용
    val weathers: List<String> = emptyList(),  // 신규 다중 필드
    val weather: String? = null,               // 레거시 단일 필드 — 읽기 전용
    ...
)

// toDomain: emotions 우선, 비어있으면 emotion 단일 필드 fallback
emotions = emotions.mapNotNull { ... }
    .ifEmpty { emotion?.let { ... }?.let { listOf(it) } ?: emptyList() }

// fromDomain: emotions/weathers만 저장 (단일 필드 더 이상 쓰지 않음)
emotions = e.emotions.map { it.name },
weathers = e.weathers.map { it.name },
```

---

## CHANGE-03: DiaryLocalCache.kt — JSONArray 직렬화

```kotlin
// entryToJson: JSONArray로 저장
val emotionArr = JSONArray()
entry.emotions.forEach { emotionArr.put(it.name) }

// entryFromJson: optJSONArray로 읽기, 없으면 old "emotion" 단일 필드 fallback
val emotionArr = obj.optJSONArray("emotions")
val emotions = if (emotionArr != null) { ... }
else { obj.optString("emotion","").takeIf{...}?.let{...}?.let{listOf(it)} ?: emptyList() }
```

---

## CHANGE-04: DiaryViewModel.kt — saveDiary 파라미터 변경

```kotlin
fun saveDiary(
    ...
    emotions: List<EmotionTag> = emptyList(),   // EmotionTag? → List
    weathers: List<WeatherTag> = emptyList(),   // WeatherTag? → List
    ...
)
// DiaryEntry 생성 시: emotions = emotions, weathers = weathers
```

---

## CHANGE-05: DiaryEditorScreen.kt — Set 상태 + 다중 토글

```kotlin
var selectedEmotions by remember { mutableStateOf<Set<EmotionTag>>(emptySet()) }
var selectedWeathers by remember { mutableStateOf<Set<WeatherTag>>(emptySet()) }

// 기존 일기 로드 시
selectedEmotions = e.emotions.toSet()
selectedWeathers = e.weathers.toSet()

// EmotionSelector 호출
EmotionSelector(
    selected = selectedEmotions,
    onSelect = { em -> selectedEmotions = if (em in selectedEmotions) selectedEmotions - em else selectedEmotions + em }
)

// WeatherSelector 호출
WeatherSelector(
    selected = selectedWeathers,
    onSelect = { w -> selectedWeathers = if (w in selectedWeathers) selectedWeathers - w else selectedWeathers + w }
)

// saveDiary 호출
emotions = selectedEmotions.toList(),
weathers = selectedWeathers.toList(),

// EmotionSelector 컴포저블 시그니처
@Composable private fun EmotionSelector(
    selected: Set<EmotionTag>,
    onSelect: (EmotionTag) -> Unit
)
// isSelected = emotion in selected (Set 포함 여부)
```

---

## CHANGE-06: WeatherSelector.kt — Set 기반 다중 선택

```kotlin
@Composable
fun WeatherSelector(
    selected: Set<WeatherTag>,
    onSelect: (WeatherTag) -> Unit,
    modifier: Modifier = Modifier
)
// isSelected = weather in selected
```

---

## CHANGE-07: DiaryDetailScreen.kt — 복수 칩 LazyRow

```kotlin
// 조건 변경
if (entry.emotions.isNotEmpty() || entry.weathers.isNotEmpty()) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(entry.emotions) { emotion ->
            AssistChip(onClick = {}, label = { Text("${emotion.emoji} ${emotion.label}") })
        }
        items(entry.weathers) { weather ->
            AssistChip(onClick = {}, label = { Text("${weather.emoji} ${weather.label}") })
        }
    }
    Spacer(Modifier.height(12.dp))
}
```

---

## CHANGE-08: HomeScreen.kt — DayCell emotions.firstOrNull()

```kotlin
// Before
val emotion = entry?.emotion

// After
val emotion = entry?.emotions?.firstOrNull()
```

---

## 하위 호환성 보장

| 시나리오 | 처리 |
|---------|------|
| 기존 Firestore 문서 (`emotion: "HAPPY"`) | toDomain에서 emotions fallback → `[HAPPY]` |
| 기존 로컬 캐시 (`"emotion": "HAPPY"`) | entryFromJson fallback → `[HAPPY]` |
| 신규 저장 (`emotions: ["HAPPY","CALM"]`) | 그대로 읽기 |
| 기존 단일 선택 앱 사용자 | 불러오면 1개 선택 상태로 표시, 추가 선택 가능 |
