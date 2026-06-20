# Report: ladder-game-complexity

## 결과 요약
- **Match Rate**: 100% ✅
- **Status**: PASSED (Iteration 1)
- **변경 파일**: 1개 (LadderGameScreen.kt)
- **빌드**: BUILD SUCCESSFUL in 8s

## 구현 내용

### F1: 키보드 자동 스크롤
- `imePadding()` — Column에 추가, 키보드 높이만큼 하단 패딩 자동 생성
- `WindowInsets.ime.getBottom(density)` → `LaunchedEffect(imeBottom)` → 키보드 올라올 때 `animateScrollTo(maxValue)`
- 기존 `LaunchedEffect(inputs.size)` (항목 추가 스크롤)와 병행 동작
- 키보드 패딩 결과: 키보드 위까지 스크롤 가능, 모든 항목 확인 가능

### F2: 바로 시작 버튼 위치 변경
- INPUT 창: "바로 시작" + "이름 입력" Row 제거 → "사다리 만들기" 단일 버튼
- NAMING 단계: names 입력 Row 아래 "바로 시작" 버튼 가운데 정렬
- `Modifier.align(Alignment.CenterHorizontally)` in Column scope
- 클릭: `names = List(items.size) { "${it+1}" }` → `phase = REVEALING`

### F3: 다시 하기 → 즉시 게임 가능
- `restart()`: `phase = LadderPhase.NAMING` → `LadderPhase.REVEALING`
- 새 사다리(`rungs = generateLadder(...)`) + 재셔플(`items`) + names 유지
- 결과: "모두 보기" + "다시 하기" 버튼이 있는 REVEALING 단계로 즉시 진입

### F4: 사다리 복잡도 증가
- `rows: 12 → 20` (66% 행 증가)
- `Random.nextBoolean()` (50%) → `Random.nextFloat() < 0.75f` (75%)
- 10개 항목 기준: 54개 평균 연결 → 135개 (2.5배 복잡)
- 인접 제약(`col - 1 !in row`) 유지로 시각적 일관성 보존
- 20행 × 75% 연결밀도 → 1번에서 10번까지 도달 가능한 경우의 수 대폭 증가

## 상태 흐름 (변경 후)
```
INPUT
  └─ [사다리 만들기] → NAMING
       ├─ 슬롯 클릭 → dialog 이름 입력
       ├─ [바로 시작] (가운데) → names=숫자 → REVEALING
       └─ 모든 이름 입력 완료 → REVEALING

REVEALING
  ├─ [다시 하기] → restart() → 새 사다리, 이름 유지 → REVEALING (즉시 게임)
  └─ [모두 보기] → 전체 애니메이션 → 결과 팝업
       └─ [다시 하기] → restart() → REVEALING
```
