# Plan: diary-editor-bg-setting

## Context Anchor
- **WHY**: 글쓰기/수정 화면 배경이 달력과 동일하게 연동되어 독립 설정 불가 + 어두운 템플릿 부재
- **WHO**: 조이어리 앱 사용자
- **RISK**: 어두운 배경 선택 시 텍스트 가시성 — luminance 기반 adaptive 텍스트 색상으로 해결
- **SUCCESS**: (1) 설정 화면에 글쓰기 배경색 독립 팔레트 추가, (2) 미드나잇·곤색·다크브라운 템플릿 추가
- **SCOPE**: AppThemeTemplate.kt, Color.kt, SettingsScreen.kt, MainActivity.kt, DiaryEditorScreen.kt, DiaryDetailScreen.kt

[CP-1 Auto] 요구사항 확인됨
[CP-2 Auto] 합리적 기본값 적용

## 요구사항

### FR-01: 어두운 테마 템플릿 추가 (3종)
- 미드나잇 (index 20): 거의 검정, 보라 액센트
- 곤색 (index 21): 딥 네이비, 하늘 액센트
- 다크브라운 (index 22): 진한 초콜릿 브라운, 황금 액센트

### FR-02: DiaryBgPalette 확장
- 기존 10종 light 색상 + 5종 dark 색상 (슬레이트, 곤색, 다크브라운, 미드나잇, 다크그린)

### FR-03: 설정 화면에 글쓰기/수정 배경색 팔레트 추가
- 평일 글씨색 아래에 "글쓰기/수정 배경색" ColorPaletteRow 추가

### FR-04: MainActivity에서 settingsViewModel.diaryBgColor 사용
- diaryBg = template.themeColors.calendarBg → diaryBg = settingsViewModel.diaryBgColor

### FR-05: 텍스트 색상 adaptive (luminance 기반)
- luminance < 0.5 → White, else → Black
- DiaryEditorScreen (TopAppBar colors + TextField textColor)
- DiaryDetailScreen/DiaryPageContent (TopAppBar colors)

## Success Criteria
- SC-01: AppThemeTemplates 리스트에 미드나잇(20), 곤색(21), 다크브라운(22) 포함
- SC-02: DiaryBgPalette.colors.size >= 15 (dark 포함)
- SC-03: SettingsScreen에 diaryBgColor ColorPaletteRow 존재
- SC-04: MainActivity diaryBg = settingsViewModel.diaryBgColor
- SC-05: DiaryEditorScreen TopAppBar/TextField 색상 adaptive
- SC-06: DiaryDetailScreen TopAppBar 색상 adaptive
