# Design: calendar-daycell-fix

## Architecture: Option C — Pragmatic Balance

### DayCell 변경 전/후

**변경 전:**
```kotlin
val bgColor = when {
    isToday -> MaterialTheme.colorScheme.primary
    else -> Color.Transparent
}
val dateColor = when {
    isToday -> MaterialTheme.colorScheme.onPrimary  // 흰색
    ...
}
Column(
    modifier = Modifier
        .height(60.dp)
        .padding(2.dp)
        .clip(CircleShape)          // ← 원형 클립
        .background(bgColor)         // ← 채워진 배경
        .clickable(onClick = onClick)
        .padding(top = 6.dp)
    ...
)
```

**변경 후:**
```kotlin
// bgColor 제거
val dateColor = when {
    isToday -> MaterialTheme.colorScheme.primary  // ← 테두리 색과 동일
    ...
}
Column(
    modifier = Modifier
        .height(76.dp)              // ← 60dp → 76dp (+16dp)
        .padding(2.dp)
        .border(                    // ← 테두리만 (채우기 없음)
            width = if (isToday) 2.dp else 0.dp,
            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
            shape = RoundedCornerShape(4.dp)  // ← 네모 (살짝 둥근 모서리)
        )
        .clickable(onClick = onClick)
        .padding(top = 6.dp)
    ...
)
```

### 빈 셀 높이
```kotlin
// 변경 전
Box(Modifier.height(60.dp))
// 변경 후
Box(Modifier.height(76.dp))
```

### import 추가
```kotlin
import androidx.compose.foundation.shape.RoundedCornerShape
```
(CircleShape는 빈 일기 원형 Box에서 계속 사용)

## 변경 파일
| File | 변경 유형 |
|------|-----------|
| `ui/home/HomeScreen.kt` | 수정 |
