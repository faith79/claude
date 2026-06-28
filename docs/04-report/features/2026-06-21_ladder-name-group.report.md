# Report: ladder-name-group

| 항목 | 내용 |
|------|------|
| Feature | ladder-name-group |
| Quality Gate | 100% ✅ |
| 반복 횟수 | 1회 |
| 변경 파일 | LadderGameScreen.kt (1개) |

## F1: 바로 시작 이름 보존 버그 수정

**원인**: `onQuickStart`에서 `List(items.size) { "${it+1}" }` 로 새 리스트 생성 → 기존 names 완전 교체

**수정**: `names.mapIndexed { idx, name -> if (name.isNullOrBlank()) "${idx+1}" else name }`
- null/blank 슬롯만 번호 부여, 기존 입력값 보존

| 케이스 | 수정 전 | 수정 후 |
|--------|---------|---------|
| "Alice" 입력 후 바로 시작 | ["1","2","3",...] | ["Alice","2","3",...] ✓ |
| 일부 입력 후 바로 시작 | 모두 숫자로 초기화 | 입력값 유지 + 빈자리만 숫자 ✓ |

## F2: 하단 결과 그룹 색상

**추가**: `LADDER_GROUP_PALETTE` (파스텔 8색)

**로직**: `itemGroupColorMap` — 항목 텍스트를 빈도 내림차순 정렬 후 색상 할당
- 최빈도 그룹 → `surfaceVariant` (중립색, "기본")
- 나머지 그룹 → 팔레트에서 순서대로 할당

| 상태 | 배경색 | 글자색 |
|------|--------|--------|
| isHidden | surfaceVariant | onSurfaceVariant |
| 경로 공개됨 | LADDER_PATH_COLORS (경로색) | White, Bold |
| 비공개지만 그룹 있음 | groupColor (파스텔) | onSurface |
| 그룹 없음(최빈) | surfaceVariant | onSurface |

**예시 동작**:
- ["꽝"×4, "당첨"×1] → "꽝"=중립, "당첨"=Red 200
- ["A"×2, "B"×2, "C"×1] → A=중립, B=Red 200, C=Blue 200
- 3그룹 → 3가지 다른 색 ✓

## Match Rate: 100%
