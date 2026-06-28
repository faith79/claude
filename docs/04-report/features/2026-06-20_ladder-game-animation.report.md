# Report: ladder-game-animation

## 결과 요약
- **Match Rate**: 100% ✅
- **Status**: PASSED (Iteration 1)
- **변경 파일**: 1개 (LadderGameScreen.kt 전면 재작성)
- **빌드**: BUILD SUCCESSFUL in 11s

## 구현 내용

### F1: 자동 스크롤 (INPUT 단계)
- `LaunchedEffect(inputs.size)` → `delay(50)` → `scrollState.animateScrollTo(maxValue)`
- 항목 추가 시 레이아웃 정착 후 하단 자동 스크롤

### F2: 이동하는 점 애니메이션
- `animate(0f, 1f, tween(rungs.size * 100ms))` suspend 함수
- `computeDotOffset()`: 각 행 세그먼트에서 vertFrac=0.7(내려가기), 0.3(방향전환)
- Canvas에서 `drawCircle(color, 8dp) + drawCircle(White, 3.6dp)` 점 표시

### F3: 기본값 "꽝"
- INPUT `onStart`: `inputs.map { if (it.isBlank()) "꽝" else it.trim() }`
- Name dialog 확인: `if (dialogText.isBlank()) "꽝" else dialogText.trim()`
- `OutlinedTextField placeholder = Text("꽝")`

### F4: 다중 결과 유지 (색상 구분)
- `revealedPaths: Map<Int, List<Int>>` — 이름 클릭할 때마다 누적
- `LADDER_PATH_COLORS`: 10가지 색 (빨·파·초·주·보·청·분·갈·남·연두)
- 이름 버튼: 공개됨 → 해당 색 배경 + 흰 글자
- 하단 항목: `itemColorMap[itemIdx] = nameIdx` → 해당 색 강조
- 다음 이름 클릭 시 이전 결과 경로 그대로 유지 (초기화 없음)
- 이미 공개된 이름 재클릭 → 상태 텍스트만 갱신 (재애니메이션 없음)

### F5: 한번에 모두 보기
- REVEALING 단계에 "한번에 모두 보기" 버튼 표시 (애니메이션 중 비활성화)
- `startAllReveal()`: 모든 경로 동시 계산 → 같은 `animProgress`로 N개 점 동시 이동
- 애니메이션 완료 → `revealedPaths = allPaths` → `showResultsDialog = true`
- `AlertDialog`: 색 점(14dp) + "이름 → 결과" 리스트 팝업

## 주요 상태 흐름
```
isHidden = revealedPaths.isEmpty() && animatingIndex == null && !isAnimatingAll
→ true: 🔒 오버레이 표시
→ false: 가로줄 + 완료 경로 + 이동 점 모두 표시
```
