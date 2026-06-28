# Plan: calendar-diary-bg-fix

## Context Anchor
- **WHY**: 달력 셀 정렬 개선 + 글쓰기 배경색 팔레트를 색상테마와 통일
- **WHO**: 조이어리 앱 사용자
- **RISK**: 없음 — UI 레이아웃 미세 조정
- **SUCCESS**: 달력 날짜 셀 상단 정렬 / 글쓰기 배경색에 테마 20종 표시
- **SCOPE**: HomeScreen.kt, SettingsScreen.kt

## Requirements
- FR-01: 달력 DayCell 내용 상단 정렬 (Arrangement.Top + paddingTop 6dp) + weight(1f) Box 제거
- FR-02: 글쓰기 배경색 팔레트 = AppThemeTemplates.appBg 색상 (DiaryBgPalette 대체)
