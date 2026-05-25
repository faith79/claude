# Report: calendar-daycell-fix

## 완료 요약
- **Feature**: calendar-daycell-fix
- **Quality Gate**: 100% / 100% PASSED ✅
- **Iterations**: 1 / 5
- **Status**: completed

## 변경 내용 (HomeScreen.kt)

### 1. DayCell 높이 확대 (60dp → 76dp)
- `DayCell` Column: `.height(60.dp)` → `.height(76.dp)` (+16dp)
- 빈 셀 Box: `Box(Modifier.height(60.dp))` → `Box(Modifier.height(76.dp))`
- 이모지(~32dp) + spacer(2dp) + 날짜(~17dp) + padding(6dp) = ~57dp → 76dp 내에 충분히 수용

### 2. 오늘 표시: 채워진 원 → 빈 네모 테두리

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 모양 | 원형 (CircleShape) | 4dp 둥근 네모 (RoundedCornerShape(4.dp)) |
| 채우기 | primary 색 채움 | 없음 (투명) |
| 테두리 | 없음 | 2dp, primary 색 |
| 날짜 텍스트 | `onPrimary` (흰색) | `primary` (테두리와 동일 색) |

```kotlin
// 제거된 코드
val bgColor = ...
.clip(CircleShape)
.background(bgColor)

// 추가된 코드
.border(
    width = if (isToday) 2.dp else 0.dp,
    color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
    shape = RoundedCornerShape(4.dp)
)
```

## 변경 파일
| File | 변경 유형 |
|------|-----------|
| `ui/home/HomeScreen.kt` | 수정 |
