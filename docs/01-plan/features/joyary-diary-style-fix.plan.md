# Plan: joyary-diary-style-fix

## Context Anchor
- **WHY**: 일기 배경색이 색상테마와 별개로 관리되어 UX 일관성 저하. 일기 입력 텍스트가 하늘색으로 표시되어 가독성 문제.
- **WHO**: 조이어리 앱 사용자
- **RISK**: 기존 diaryBgColor 설정값 무시됨 (사용자가 의도한 변경)
- **SUCCESS**: 테마 변경 시 일기 배경도 자동 연동, 텍스트 가시성 개선
- **SCOPE**: SettingsScreen, MainActivity, DiaryEditorScreen 3파일 수정

## Requirements
1. 일기 배경색 → 색상테마의 appBg 색상과 동일하게 자동 연동
2. DiaryEditorScreen OutlinedTextField 입력 텍스트 색상 → 검정색

## Changes
- `MainActivity.kt`: `diaryBg = diaryBg` → `diaryBg = template.themeColors.appBg`
- `DiaryEditorScreen.kt`: OutlinedTextField에 `colors` 파라미터로 텍스트 색상 명시
- `SettingsScreen.kt`: "일기 배경색" 팔레트 행 제거 (테마 연동으로 불필요)
