# Plan: ladder-game-animation

## WHY
사다리 게임 UX 개선 — 시각적 피드백, 멀티 결과 표시, "모두 보기" 기능 추가

## Scope
- F1: INPUT 자동 스크롤 (항목 추가 시 하단 표시)
- F2: 이름 클릭 시 이동하는 점 애니메이션 (coroutine + animate())
- F3: 빈 항목 기본값 "꽝" (item input + name dialog 모두)
- F4: 다중 결과 유지 (revealedPaths Map, 10가지 색상)
- F5: "한번에 모두 보기" 버튼 — 전체 점 동시 이동 → 결과 팝업

## Risk
- 애니메이션 중 중복 클릭 방지 (guard 패턴)
- 같은 하단 항목에 두 경로가 도착할 때 색상 충돌 (마지막 승리)

## Success
- BUILD SUCCESSFUL, 5개 요구사항 100% 충족
