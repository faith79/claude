# Design: ladder-game-animation

## Architecture: Option C (Pragmatic Balance)

## State Changes in LadderGameScreen

```
// Replace single-reveal state with multi-reveal:
revealedPaths: Map<Int, List<Int>>   // nameIdx → completed path (persists)
animatingIndex: Int?                  // single mode: which name is animating
animatingPath: List<Int>              // path for single animation
isAnimatingAll: Boolean               // all-mode flag
allAnimPaths: Map<Int, List<Int>>     // paths for "모두 보기" animation
animProgress: Float                   // 0f..1f animation progress
lastRevealedIndex: Int?               // for status text
showResultsDialog: Boolean            // results popup flag

isHidden = revealedPaths.isEmpty() && animatingIndex == null && !isAnimatingAll
```

## F1: 자동 스크롤 (LadderInputContent)
```
val scrollState = rememberScrollState()
LaunchedEffect(inputs.size) {
    kotlinx.coroutines.delay(50)   // layout settle
    scrollState.animateScrollTo(scrollState.maxValue)
}
```

## F2: 이동하는 점 애니메이션
- `animate(0f, 1f, tween(rungs.size * 100ms))` suspend function in coroutine
- Dot position: segment[i] spans y=i*rowH → (i+1)*rowH at column path[i]
- vertFraction=0.7 (go down) then horizFraction=0.3 (turn at rung)
- Dot drawn in Canvas: drawCircle(color, 8dp) + drawCircle(White, 3.6dp) center

## F3: 기본값 "꽝"
- onStart: `inputs.map { if (it.isBlank()) "꽝" else it.trim() }`
- name dialog confirm: `if (dialogText.isBlank()) "꽝" else dialogText.trim()`
- placeholder in OutlinedTextField: Text("꽝")

## F4: 다중 결과 유지
- 10 colors: LADDER_PATH_COLORS = [red, blue, green, orange, purple, ...]
- Name button: revealed → pathColor bg, White text
- Canvas: draw ALL revealedPaths with their colors
- Bottom items: itemColorMap[itemIdx] = nameIdx → use pathColors[nameIdx]

## F5: 한번에 모두 보기
- Button visible in REVEALING phase, disabled during animation
- `allAnimPaths = names.indices.associateWith { tracePath(it, rungs) }`
- Same `animate(0f, 1f)` with all dots using same animProgress
- On complete: `revealedPaths = allPaths`, `showResultsDialog = true`
- AlertDialog: Column of "● nameIdx → result" rows with color dots

## File Modified
- `diary-app/app/src/main/java/com/example/diaryapp/ui/tools/LadderGameScreen.kt`
