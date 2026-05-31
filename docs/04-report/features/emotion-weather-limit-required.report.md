# Report: emotion-weather-limit-required

**완료일**: 2026-06-01
**Match Rate**: 100% ✅
**변경 파일**: 2개 | **변경 라인**: ~25줄

---

## 수정 내용

### 1. 저장 버튼 필수 조건 추가 (DiaryEditorScreen.kt)

```kotlin
// Before
enabled = content.isNotBlank() && !isLoading

// After — 감정·날씨·내용 모두 필수
enabled = content.isNotBlank() && selectedEmotions.isNotEmpty()
       && selectedWeathers.isNotEmpty() && !isLoading
```

### 2. EmotionSelector 최대 3개 제한 + 카운트 헤더 (DiaryEditorScreen.kt)

```kotlin
// 헤더: "오늘의 감정 (N/3)"
Row(verticalAlignment = Alignment.CenterVertically) {
    Text("오늘의 감정", ...)
    Text("(${selected.size}/3)", color = if(selected.isEmpty()) error else onSurfaceVariant)
}

// 미선택 + maxReached = dimmed + 클릭 비활성
val isClickable = isSelected || !maxReached
Column(
    modifier = Modifier
        .alpha(if (!isSelected && maxReached) 0.38f else 1f)
        .clickable(enabled = isClickable) { onSelect(emotion) }
)
```

### 3. WeatherSelector 최대 3개 제한 + 카운트 헤더 (WeatherSelector.kt)

```kotlin
// 헤더: "날씨 (N/3)"
Row { Text("날씨"); Text("(${selected.size}/3)") }

// FilterChip disabled when maxReached + not selected
FilterChip(
    enabled = isSelected || !maxReached,
    ...
)
```

---

## 동작 방식

| 상황 | 동작 |
|------|------|
| 감정 0개 선택 | 헤더 카운트 빨간색 "(0/3)", 저장 버튼 비활성 |
| 감정 3개 선택 | 미선택 항목 38% 투명, 클릭 불가 |
| 감정 선택 후 재클릭 | 해제 가능 (3개 도달해도 선택된 항목은 해제 가능) |
| 날씨 미선택 | 헤더 카운트 빨간색, 저장 버튼 비활성 |
| 내용 미입력 | 저장 버튼 비활성 (기존 동작 유지) |
| 모두 입력 완료 | 저장 버튼 활성화 |
