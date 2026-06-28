# Report: ladder-game-restart

## 결과 요약
- **Match Rate**: 100% ✅
- **Status**: PASSED (Iteration 1)
- **변경 파일**: 1개 (LadderGameScreen.kt)
- **빌드**: BUILD SUCCESSFUL in 7s

## 구현 내용

### F1: 바로 시작 버튼
- `onQuickStart` 콜백을 `LadderInputContent`에 추가
- "바로 시작" + "이름 입력" 두 버튼을 Row로 나란히 배치
- 바로 시작 시: `names = List(n) { "${it+1}" }` (숫자 자동 설정) → `phase = REVEALING`
- NAMING 단계 완전히 건너뜀

### F2: 마지막 경로 색상 버그 수정
- **원인**: `rowH = size.height / (rungs.size + 1)` → 마지막 세그먼트 종점 = `numSegs * rowH < size.height`
- **drawLadderPath**: 루프 후 `path.last()` 열에서 `lastSegEndY → size.height` 연장 선 추가
- **drawPartialLadderPath**: `curSeg == numSegs-1 && col == next`일 때 `y2 = size.height`
- **computeDotOffset**: `canvasH` 파라미터 추가, 마지막 직진 세그먼트에서 y2=canvasH 사용
- 방향전환(col != next)이 있는 마지막 세그먼트는 rungY 기준 유지 (타이밍 정확성 보장)

### F3: 다시 하기 버튼
- REVEALING 단계 버튼 Row: "다시 하기" + "모두 보기" 나란히 배치 (애니메이션 중 disabled)
- 결과 팝업(showResultsDialog) dismissButton에 "다시 하기" 추가
- `restart()` 동작:
  - `rungs = generateLadder(inputs.size)` — 사다리 랜덤 재생성
  - `items = allItems.shuffled()` — 항목 재셔플
  - `names` 유지 — 이전 이름 기억, NAMING에서 변경 가능
  - `revealedPaths`, 애니메이션 상태 모두 초기화
  - `phase = LadderPhase.NAMING`

## 상태 흐름
```
INPUT
  ├─ [이름 입력] → NAMING → (모든 이름 입력) → REVEALING
  └─ [바로 시작] → REVEALING (names = ["1","2","3"...])

REVEALING
  ├─ [다시 하기] → restart() → NAMING (새 사다리, 이름 유지)
  └─ [모두 보기] → 전체 애니메이션 → 결과 팝업
       └─ [다시 하기] → restart() → NAMING
```
