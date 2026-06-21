# Plan: kbo-standings

## Context Anchor
- **WHY**: 도구탭에 프로야구 순위 실시간 조회 기능 추가 — 앱 내에서 KBO 순위 확인 가능
- **WHO**: 조이어리 앱 사용자
- **RISK**: KBO 공식 사이트 HTML 구조 변경 시 파싱 실패 가능. 에러 상태 + 재시도 필수.
- **SUCCESS**: 도구탭에 아이콘 노출 → 클릭 → 현재 년도 KBO 순위 표 표시
- **SCOPE**: ToolsScreen, HomeScreen, NavGraph, Screen + 신규 KboStandingsScreen

## 요구사항
- F1: ToolsScreen에 "프로야구 순위" 아이콘 카드 추가 (사다리 게임 옆)
- F2: KboStandingsScreen — Jsoup으로 KBO 공식 실시간 스크래핑
- F3: 표 컬럼: 순위·팀·경기·승·무·패·승률·게임차·연속
- F4: 현재 날짜의 년도를 타이틀에 표시, 새로고침 버튼
- F5: Loading / Error(재시도) / Success 상태 처리
- F6: 승 =파란색, 패 =빨간색, 연속(승/패) 색상 구분, 1~3위 굵게

## 변경 파일
| 파일 | 종류 |
|------|------|
| app/build.gradle.kts | 수정 — jsoup 추가 |
| navigation/Screen.kt | 수정 — KboStandings 라우트 |
| ui/tools/ToolsScreen.kt | 수정 — onNavigateToKbo + 아이콘 |
| ui/home/HomeScreen.kt | 수정 — onNavigateToKbo 파라미터 |
| navigation/NavGraph.kt | 수정 — 라우트 + 콜백 |
| ui/tools/KboStandingsScreen.kt | 신규 |
