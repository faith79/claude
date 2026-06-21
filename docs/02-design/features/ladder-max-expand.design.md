# Design: ladder-max-expand

## Architecture: Option C — Pragmatic Balance

LadderGameScreen.kt 단일 파일, 최소 변경.

## 변경 상세

### 1. LADDER_PATH_COLORS (line 38-42)
10색 → 12색으로 확장. 추가 색상: 청록(Teal) + 딥오렌지(DeepOrange)
```kotlin
private val LADDER_PATH_COLORS = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
    Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1),
    Color(0xFFE91E63), Color(0xFF6D4C41), Color(0xFF3949AB), Color(0xFF7CB342),
    Color(0xFF00897B), Color(0xFFF4511E)   // 신규
)
```

### 2. onAddInput 조건 (line 176)
```kotlin
onAddInput = { if (inputs.size < 12) inputs = inputs + "꽝" },
```

### 3. 안내 문구 (line 467)
```kotlin
Text("참가 항목 입력 (2~12개)", ...)
```

### 4. 추가 버튼 노출 조건 (line 491)
```kotlin
if (inputs.size < 12) {
```
