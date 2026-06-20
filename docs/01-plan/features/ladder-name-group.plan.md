# Plan: ladder-name-group

## Context Anchor
- **WHY**: (1) 바로 시작 시 입력한 이름이 리셋되는 버그, (2) 하단 결과가 그룹 구분 없이 단색으로 표시되어 가독성 낮음
- **WHO**: 조이어리 앱 사다리 게임 사용자
- **RISK**: names 리스트의 null/비어있음 구분 로직 정확성; 그룹 색상이 경로 색상과 충돌 없이 공존
- **SUCCESS**: 부분 입력 후 바로 시작 시 입력값 보존; 동일 텍스트 항목이 같은 배경색으로 묶임
- **SCOPE**: LadderGameScreen.kt 단일 파일

## 요구사항

### F1: 바로 시작 이름 보존 버그 수정
- 현황: `onQuickStart`에서 `List(items.size) { "${it+1}" }` — 기존 names 완전 덮어씌움
- 목표: 이미 입력된 name은 유지, null/blank인 슬롯만 숫자로 채움
- 구현: `names.mapIndexed { idx, name -> if (name.isNullOrBlank()) "${idx+1}" else name }`

### F2: 결과 그룹 색상
- 현황: 하단 items Row — 경로 미공개 시 모두 `surfaceVariant`, 경로 공개 시 경로 색상
- 목표: 동일 텍스트 항목 = 동일 배경색 그룹; 다른 텍스트 = 다른 색; 경로 공개 시 경로 색이 우선
- 구현:
  - `LADDER_GROUP_PALETTE`: 파스텔 톤 8색 팔레트
  - `itemGroupColorMap`: items 텍스트 → 그룹 색상 맵 (최빈도 그룹 = surfaceVariant, 나머지 = 팔레트)
  - items Row에서 `pathColor`/`groupColor` 우선순위 분기

## 범위 내
- `onQuickStart` lambda 수정
- `LADDER_GROUP_PALETTE` 상수 추가
- `LadderGameContent` 내 items Row 색상 로직 수정

## 범위 외
- tracePath, 애니메이션, 캔버스, 네비게이션 — 변경 없음
