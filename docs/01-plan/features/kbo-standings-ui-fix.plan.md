# Plan: kbo-standings-ui-fix

## WHY
KBO 순위표의 컬럼 순서가 "승 무 패"로 표시되어 일반적인 야구 순위표 표기(승 패 무)와 다름.
도구 탭의 아이콘이 generic Material 아이콘이어서 각 기능을 직관적으로 표현하지 못함.

## WHO
조이어리 앱 사용자 (야구 팬)

## SCOPE
1. KboStandingsScreen.kt — 헤더 및 데이터 셀 순서: 승·무·패 → 승·패·무
2. ToolsScreen.kt — 사다리 게임 아이콘: 🪜 (사다리), KBO 아이콘: ⚾ (야구공)

## OUT OF SCOPE
- KboStandingsViewModel 로직 변경 없음 (데이터 모델 필드 순서 유지)
- LadderGameScreen 내부 변경 없음

## SUCCESS
- 순위표: 헤더 "승 패 무" 순서, 데이터 색상 유지 (승=빨강, 패=파랑)
- 아이콘: 사다리=🪜, KBO=⚾ (48dp 크기)
