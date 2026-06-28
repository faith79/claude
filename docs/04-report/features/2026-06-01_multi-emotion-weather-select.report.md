# Report: multi-emotion-weather-select

**완료일**: 2026-06-01
**Match Rate**: 100% ✅
**변경 파일**: 8개 | **변경 라인**: ~80줄

---

## 수정 내용

### 1. 데이터 모델 변경 (DiaryEntry.kt)

| 필드 | 기존 | 변경 |
|------|------|------|
| `emotion` | `EmotionTag?` | `emotions: List<EmotionTag>` |
| `weather` | `WeatherTag?` | `weathers: List<WeatherTag>` |

### 2. Firestore DTO 확장 (FirestoreDataSource.kt)

```kotlin
// 신규 다중 필드 추가
val emotions: List<String> = emptyList()
val weathers: List<String> = emptyList()
// legacy 단일 필드 읽기 전용 유지 (기존 데이터 호환)
val emotion: String? = null
val weather: String? = null

// toDomain: emotions 우선, 비면 legacy emotion fallback
emotions = emotions.mapNotNull { ... }.ifEmpty { emotion?.let{...}?.let{listOf(it)} ?: emptyList() }
```

### 3. 로컬 캐시 직렬화 (DiaryLocalCache.kt)

```kotlin
// 저장: JSONArray
val emotionArr = JSONArray(); entry.emotions.forEach { emotionArr.put(it.name) }

// 읽기: optJSONArray 우선, null이면 구버전 optString fallback
val emotionArr = obj.optJSONArray("emotions")
val emotions = if (emotionArr != null) { ... }
else { obj.optString("emotion","")?.let{...}?.let{listOf(it)} ?: emptyList() }
```

### 4. ViewModel 파라미터 변경 (DiaryViewModel.kt)

```kotlin
// Before
fun saveDiary(... emotion: EmotionTag?, weather: WeatherTag?, ...)

// After
fun saveDiary(... emotions: List<EmotionTag>, weathers: List<WeatherTag>, ...)
```

### 5. 편집 화면 다중 선택 (DiaryEditorScreen.kt)

```kotlin
// Set 기반 상태
var selectedEmotions by remember { mutableStateOf<Set<EmotionTag>>(emptySet()) }
var selectedWeathers by remember { mutableStateOf<Set<WeatherTag>>(emptySet()) }

// 토글 로직
selectedEmotions = if (em in selectedEmotions) selectedEmotions - em else selectedEmotions + em
```

### 6. WeatherSelector 다중 선택 (WeatherSelector.kt)

```kotlin
fun WeatherSelector(selected: Set<WeatherTag>, onSelect: (WeatherTag) -> Unit)
// isSelected = weather in selected
```

### 7. 상세보기 복수 칩 (DiaryDetailScreen.kt)

```kotlin
LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    items(entry.emotions) { emotion -> AssistChip(...) }
    items(entry.weathers) { weather -> AssistChip(...) }
}
```

### 8. 달력 셀 첫 번째 감정 표시 (HomeScreen.kt)

```kotlin
val emotion = entry?.emotions?.firstOrNull()
```

---

## 하위 호환성

| 시나리오 | 처리 방식 |
|---------|---------|
| 기존 Firestore 단일 `emotion` 필드 | `emotions` 비면 `emotion` fallback → `[HAPPY]` |
| 기존 로컬 캐시 단일 `emotion` 문자열 | `optJSONArray` null → `optString` fallback |
| 기존 사용자 기존 일기 | 불러오면 1개 선택 상태로 복원, 추가 선택 가능 |

## 동작 방식

- **편집**: 감정/날씨 아이템 클릭 시 Set에 추가, 재클릭 시 제거 (토글)
- **상세보기**: 선택된 모든 감정+날씨 칩을 가로 스크롤 LazyRow로 표시
- **달력**: 여러 감정 중 첫 번째 이모지를 셀에 표시
