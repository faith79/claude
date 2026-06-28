# Plan: tools-tab-ladder-game

## Context Anchor
- **WHY**: 앱에 게임/도구 기능 추가 — 향후 여러 도구를 확장 가능한 허브 탭 구성
- **WHO**: 일기 앱 사용자 (그룹 결정, 역할 분배 등에 사다리 게임 활용)
- **RISK**: HomeScreen 탭 구조 변경 → 기존 일기/메모 탭 동작 영향 없이 tab 2 추가
- **SUCCESS**: 하단 탭 3개(일기/메모/도구), 사다리 게임 완전 동작
- **SCOPE**: 새 파일 2개, 기존 파일 3개 수정

## Feature Requirements
1. HomeScreen: 도구모음 탭(tab 2) 추가, 아이콘 그리드(가로 2열)
2. 현재 도구: 사다리 게임 1개
3. 사다리 게임 흐름:
   - INPUT: 항목 2~10개 입력 (+ 버튼으로 추가, X 버튼으로 제거)
   - NAMING: 사다리 생성 후 상단 빈 버튼 클릭 → 이름 입력 (중간 숨김)
   - REVEALING: 모든 이름 입력 완료 → 이름 클릭 시 사다리 공개 + 경로 표시

## Architecture: Option C — Pragmatic Balance
- 순수 로컬 상태 (ViewModel 불필요)
- Canvas API로 사다리 드로잉
- 상태: INPUT / NAMING / REVEALING enum

## Files
- **Create** `ui/tools/ToolsScreen.kt` — 도구 허브 그리드
- **Create** `ui/tools/LadderGameScreen.kt` — 사다리 게임 전체
- **Edit** `navigation/Screen.kt` — LadderGame 라우트 추가
- **Edit** `navigation/NavGraph.kt` — LadderGame composable + HomeScreen 콜백
- **Edit** `ui/home/HomeScreen.kt` — tab 2 추가, ToolsContent 연동
