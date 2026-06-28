# Design: ladder-game-restart

## Architecture: Option C (Pragmatic Balance)

## F1: 바로 시작 버튼

LadderInputContent에 `onQuickStart` 콜백 추가:
```kotlin
// LadderInputContent params
onQuickStart: () -> Unit

// UI: 두 버튼 Row로 배치
Row {
    OutlinedButton("바로 시작", modifier = Modifier.weight(1f)) { onQuickStart() }
    Spacer(4.dp)
    Button("사다리 만들기", modifier = Modifier.weight(1f)) { onStart() }
}
```

Parent에서 onQuickStart:
```kotlin
onQuickStart = {
    if (inputs.size >= 2) {
        val allItems = inputs.map { if (it.isBlank()) "꽝" else it.trim() }
        items = allItems.shuffled()
        rungs = generateLadder(allItems.size)
        names = List(allItems.size) { "${it + 1}" }  // 숫자 자동 설정
        revealedPaths = emptyMap()
        animatingIndex = null; isAnimatingAll = false; lastRevealedIndex = null
        phase = LadderPhase.REVEALING  // NAMING 건너뜀
    }
}
```

## F2: 마지막 경로 색상 버그

### computeDotOffset — canvasH 파라미터 추가
```kotlin
private fun computeDotOffset(
    path: List<Int>, progress: Float, colW: Float, rowH: Float, canvasH: Float = 0f
): Offset {
    // ...
    val y2 = if (canvasH > 0f && seg == numSegs - 1 && col == next) canvasH
             else (seg + 1) * rowH
    // col == next 직진 마지막 세그먼트: size.height까지 점 이동
}
```

### drawLadderPath — 마지막 열 연장
```kotlin
// 루프 후 추가:
val finalX = path.last() * colW + colW / 2f
drawLine(color, Offset(finalX, (path.size - 1) * rowH), Offset(finalX, size.height), strokeW, cap = StrokeCap.Round)
```

### drawPartialLadderPath — 마지막 세그먼트 y2 연장
```kotlin
// 현재 세그먼트 직진+마지막인 경우 y2 = size.height
val y2cur = if (curSeg == numSegs - 1 && col == next) size.height else (curSeg + 1) * rowH
```

Canvas 호출부: computeDotOffset에 size.height 전달
```kotlin
val dot = computeDotOffset(path, progress, colW, rowH, size.height)
```

## F3: 다시 하기 버튼

LadderGameContent에 `onRestart: (() -> Unit)?` 파라미터 추가:
```kotlin
// REVEALING 단계 + 애니메이션 없을 때 버튼 표시
if (!isNaming && onRestart != null) {
    OutlinedButton(onClick = onRestart, enabled = !isAnimating) { Text("다시 하기") }
}
```

Parent restart 함수:
```kotlin
fun restart() {
    val allItems = inputs.map { if (it.isBlank()) "꽝" else it.trim() }
    items = allItems.shuffled()
    rungs = generateLadder(allItems.size)
    // names 유지 (이전 이름 유지, 변경 가능)
    revealedPaths = emptyMap()
    animatingIndex = null; animatingPath = emptyList()
    isAnimatingAll = false; allAnimPaths = emptyMap()
    lastRevealedIndex = null; showResultsDialog = false
    phase = LadderPhase.NAMING
}
```

결과 팝업 dismissButton:
```kotlin
dismissButton = {
    TextButton(onClick = { showResultsDialog = false; restart() }) { Text("다시 하기") }
}
confirmButton = {
    TextButton(onClick = { showResultsDialog = false }) { Text("닫기") }
}
```

## File Modified
- `diary-app/app/src/main/java/com/example/diaryapp/ui/tools/LadderGameScreen.kt`
