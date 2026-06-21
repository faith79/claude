# Design: diary-emotion-weather-add

## Architecture: Option C — Pragmatic Balance

enum 파일 최소 수정으로 UI 자동 반영. 별도 컴포넌트 변경 없음.

## 변경 파일

### 1. EmotionTag.kt
```kotlin
enum class EmotionTag(val emoji: String, val label: String) {
    HAPPY("😊", "행복"),
    SAD("😢", "슬픔"),
    ANGRY("😠", "분노"),
    CALM("😌", "평온"),
    EXCITED("🥰", "설렘"),
    ANXIOUS("😰", "불안"),
    TIRED("😴", "피곤"),
    ANNOYED("😤", "짜증")   // 신규
}
```

### 2. WeatherTag.kt
```kotlin
enum class WeatherTag(val emoji: String, val label: String) {
    SUNNY("☀️", "맑음"),
    PARTLY_CLOUDY("⛅", "구름조금"),
    CLOUDY("☁️", "흐림"),
    RAINY("🌧️", "비"),
    SNOWY("❄️", "눈"),
    HUMID("💧", "습함"),    // 신규
    HOT("🥵", "더움"),      // 신규
    COLD("🥶", "추움")      // 신규
}
```

## UI 자동 반영 근거
- `DiaryEditorScreen.kt`: `EmotionTag.entries.forEach` → ANNOYED 자동 렌더링
- `WeatherSelector.kt`: `items(WeatherTag.entries)` → 신규 3개 자동 렌더링
- 선택 제한(max 3), 저장 로직 등 모두 기존 코드 재사용
