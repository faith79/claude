# Report: ladder-game-ux-polish

## 결과 요약
- **Match Rate**: 100% ✅
- **Status**: PASSED (Iteration 1)
- **변경 파일**: 1개 (LadderGameScreen.kt 전면 재작성)
- **빌드**: BUILD SUCCESSFUL in 7s

## 구현 내용

### F1: 스크롤바 표시
- `Modifier.verticalScrollbar(ScrollState)` DrawScope 확장 함수 추가
- `drawWithContent { drawContent(); drawRect(...) }` — 콘텐츠 위에 스크롤바 오버레이
- 스크롤 중 alpha=0.9, 정지 시 alpha=0.5 (회색 4dp 막대)
- `modifier.verticalScrollbar(scrollState).verticalScroll(scrollState)` 순서로 적용
- 수식: thumbH = viewH² / (viewH + scrollMax), thumbY = (scrollValue/scrollMax) × (viewH - thumbH)

### F2: 꽝 초기값 (실제 입력값)
- `inputs = listOf("꽝", "꽝")` — placeholder 아닌 실제 값으로 초기화
- `onAddInput = { inputs = inputs + "꽝" }` — 항목 추가 시도 "꽝"으로 채워짐
- placeholder가 아닌 실제 TextField 값이므로 생성 시 바로 사용 가능

### F3: 숫자 기본값 + 빠른 저장
- NAMING 단계 미입력 버튼: `text = if (isFilled) name!! else "${idx + 1}"` 로 1,2,3... 표시
- 다이얼로그 열릴 때: `dialogText = names.getOrNull(idx) ?: "${idx + 1}"`
- 사용자가 확인 버튼만 눌러도 숫자로 바로 저장 → 빠른 게임 시작 가능

### F4: 느린 애니메이션 + 프로그레시브 경로 색상
- 속도: `tween(rungs.size * 100)` → `tween(rungs.size * 200)` (2배 느리게)
- `drawPartialLadderPath()` 신규 DrawScope 확장 추가:
  - 완료된 세그먼트: 전체 선분 색상 표시
  - 현재 세그먼트: computeDotOffset() 위치까지만 색상 표시
  - 수직 이동 중(frac ≤ 0.7): 수직선만, 수평 전환(frac > 0.7): 수직+수평 선 표시
- 단일/전체 모드 모두 drawPartialLadderPath + 점 드로잉으로 교체

## 주요 상태 흐름
```
INPUT:  inputs = ["꽝","꽝"] → 추가 시 "꽝" → 생성 버튼
NAMING: 버튼 표시 = name ?: "${idx+1}" → 클릭 → dialog 기본값 = name ?: "${idx+1}" → 확인
REVEALING: 클릭 → drawPartialLadderPath(progress) + 점 이동 (200ms/rung) → 완료 시 revealedPaths 추가
```
