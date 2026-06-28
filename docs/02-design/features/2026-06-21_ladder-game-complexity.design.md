# Design: ladder-game-complexity

## Architecture: Option C (Pragmatic Balance)

## F1: 키보드 자동 스크롤

```kotlin
// LadderInputContent
val density = LocalDensity.current
val imeBottom = WindowInsets.ime.getBottom(density)

LaunchedEffect(imeBottom) {
    if (imeBottom > 0) {
        delay(100)
        scrollState.animateScrollTo(scrollState.maxValue)
    }
}

Column(
    modifier = modifier
        .fillMaxSize()
        .imePadding()           // 키보드 높이 패딩
        .verticalScrollbar(scrollState)
        .verticalScroll(scrollState)
        .padding(16.dp)
)
```

## F2: 바로 시작 버튼 위치

INPUT: "바로 시작" 버튼 제거, "사다리 만들기" 단일 버튼만
```kotlin
Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
    Text("사다리 만들기")
}
```

NAMING 단계 이름 Row 아래:
```kotlin
if (isNaming) {
    Spacer(Modifier.height(6.dp))
    Button(
        onClick = onQuickStart,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    ) { Text("바로 시작") }
}
```

`LadderGameContent`에 `onQuickStart: () -> Unit` 파라미터 추가
Parent: `onQuickStart = { names = List(items.size) { "${it+1}" }; phase = REVEALING }`

## F3: 다시 하기 → REVEALING

```kotlin
fun restart() {
    ...
    phase = LadderPhase.REVEALING  // 기존 NAMING
    // names 유지 → 즉시 게임 가능 상태
}
```

## F4: 사다리 복잡도

```kotlin
private fun generateLadder(n: Int, rows: Int = 20): List<Set<Int>> =
    (0 until rows).map {
        val row = mutableSetOf<Int>()
        (0 until n - 1).forEach { col ->
            if (col - 1 !in row && Random.nextFloat() < 0.75f) row.add(col)
        }
        row
    }
```

복잡도 비교:
- 기존: 12행 × 50% = 평균 54 연결 (n=10)
- 변경: 20행 × 75% = 평균 135 연결 (n=10, 2.5배)
- 인접 제약(col-1 !in row) 유지 → 시각적 일관성 보장

## File Modified
- `diary-app/app/src/main/java/com/example/diaryapp/ui/tools/LadderGameScreen.kt`
