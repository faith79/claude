# Plan: ladder-game-complexity

## Context Anchor
- **WHY**: 사다리 게임 4가지 개선 — 키보드 스크롤, 바로 시작 위치, 다시 하기 동작, 사다리 복잡도
- **WHO**: 조이어리 앱 사용자
- **RISK**: WindowInsets.ime API 버전 호환성
- **SUCCESS**: 4가지 모두 동작, 빌드 성공
- **SCOPE**: LadderGameScreen.kt 단일 파일

## F1: 키보드 자동 스크롤
- `imePadding()` → Column에 추가 (키보드 높이만큼 패딩)
- `WindowInsets.ime.getBottom(density)` 감지 → LaunchedEffect → animateScrollTo(maxValue)
- 키보드 올라올 때 자동으로 하단 항목 보이게 스크롤

## F2: 바로 시작 버튼 위치 변경
- INPUT 창 버튼 Row 제거 → "사다리 만들기" 단일 버튼만 남김
- NAMING 단계에서 names row 아래 "바로 시작" 버튼 가운데 표시
- 클릭 시: names=["1","2"...] → REVEALING

## F3: 다시 하기 → 바로 게임 가능
- `restart()`: `phase = LadderPhase.REVEALING` (기존 NAMING)
- names 유지 → 새 사다리 생성 후 즉시 게임 가능
- "모두 보기", "다시 하기" 버튼이 있는 상태

## F4: 사다리 복잡도 증가
- `rows: Int = 12` → `rows: Int = 20` (행 수 66% 증가)
- `Random.nextBoolean()` (50%) → `Random.nextFloat() < 0.75f` (75%)
- 10개 항목: 평균 연결 4.5/행 → 6.75/행, 54개 → 135개 총 연결
