# Design: ladder-game-ux-polish

## Architecture: Option C (Pragmatic Balance)

## F1: verticalScrollbar DrawScope 확장

```kotlin
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size

private fun Modifier.verticalScrollbar(state: ScrollState): Modifier =
    this.drawWithContent {
        drawContent()
        val scrollMax = state.maxValue.toFloat()
        if (scrollMax > 0f) {
            val viewH = size.height
            val thumbH = (viewH * viewH / (viewH + scrollMax)).coerceAtLeast(40f)
            val thumbY = (state.value.toFloat() / scrollMax) * (viewH - thumbH)
            val bw = 4.dp.toPx()
            drawRect(
                color = Color(0xFF9E9E9E).copy(alpha = if (state.isScrollInProgress) 0.9f else 0.5f),
                topLeft = Offset(size.width - bw - 2.dp.toPx(), thumbY),
                size = Size(bw, thumbH)
            )
        }
    }
```

적용 위치 (LadderInputContent):
```kotlin
Column(
    modifier = modifier
        .fillMaxSize()
        .verticalScrollbar(scrollState)   // drawWithContent 먼저
        .verticalScroll(scrollState)
        .padding(16.dp)
)
```

## F2: 꽝 초기값

```kotlin
var inputs by remember { mutableStateOf(listOf("꽝", "꽝")) }
onAddInput = { if (inputs.size < 10) inputs = inputs + "꽝" }
```

## F3: 다이얼로그 숫자 기본값

```kotlin
dialogText = names.getOrNull(idx) ?: "${idx + 1}"
```

NAMING 단계 버튼 미입력 표시 (기존 "${idx+1}" 이미 처리됨):
```kotlin
text = if (isFilled) name!! else "${idx + 1}"
```

## F4: 느린 애니메이션 + 프로그레시브 경로 색상

```kotlin
// 속도: rungs.size * 200ms (기존 100ms → 2배)
tween(durationMillis = rungs.size * 200)

// DrawScope 확장 - 점 위치까지만 색상 표시
private fun DrawScope.drawPartialLadderPath(
    path: List<Int>, progress: Float, color: Color,
    colW: Float, rowH: Float, strokeW: Float
) {
    val numSegs = path.size - 1
    val raw = (progress * numSegs).coerceIn(0f, numSegs.toFloat())
    val curSeg = raw.toInt().coerceIn(0, numSegs - 1)
    val frac = raw - curSeg
    // 완료 세그먼트 전부 그리기
    for (i in 0 until curSeg) { ... }
    // 현재 세그먼트: 점 위치까지
    val dotPos = computeDotOffset(path, progress, colW, rowH)
    // vertFrac=0.7: 수직 이동 중이면 수직선만, 수평 전환 중이면 두 선 모두
}
```

## File Modified
- `diary-app/app/src/main/java/com/example/diaryapp/ui/tools/LadderGameScreen.kt`
