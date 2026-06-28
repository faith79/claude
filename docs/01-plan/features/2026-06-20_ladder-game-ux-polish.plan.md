# Plan: ladder-game-ux-polish

## Context Anchor
- **WHY**: 사다리 게임 UX 세부 개선 — 스크롤바 미표시, 꽝 입력값 미리채움, 숫자 기본값, 애니메이션 속도 및 경로 시각화
- **WHO**: 조이어리 앱 사용자
- **RISK**: drawWithContent 수정자 순서 오류 시 스크롤바 미표시
- **SUCCESS**: 4가지 수정사항 모두 동작, 빌드 성공
- **SCOPE**: LadderGameScreen.kt 단일 파일

## Requirements

### F1: 스크롤바 표시
- INPUT 화면 Column에 세로 스크롤바 시각적 표시
- 스크롤 중: 불투명도 0.9, 정지 시: 0.5
- ScrollState.isScrollInProgress 활용

### F2: 꽝 초기값
- inputs 초기값: `listOf("꽝", "꽝")` (빈 문자열 → "꽝")
- 항목 추가 시: `inputs + "꽝"`
- 생성 시 빈 칸 → "꽝" 처리는 기존 유지

### F3: 숫자 기본값 (이름 버튼 + 다이얼로그)
- NAMING 단계: 이름 미입력 버튼 → 슬롯 번호 표시 (`"${idx + 1}"`)
- 다이얼로그 열릴 때 기본값: `names.getOrNull(idx) ?: "${idx + 1}"`
- 사용자가 바로 확인 → 숫자로 저장 가능

### F4: 애니메이션 개선
- 속도: `tween(rungs.size * 100)` → `tween(rungs.size * 200)` (2배 느리게)
- 점 이동 중 지나간 경로 구간 색상 변경 (프로그레시브 드로잉)
- `drawPartialLadderPath()` 함수 추가

## Files
- `diary-app/app/src/main/java/com/example/diaryapp/ui/tools/LadderGameScreen.kt`

## CP-1 Auto: 요구사항 확인됨
## CP-2 Auto: 합리적 기본값 적용
