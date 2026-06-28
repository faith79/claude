# Plan: editor-bg-theme-color

## Context Anchor
- **WHY**: 글쓰기/수정 화면과 일기 읽기 화면 모두 배경이 흰색으로 나옴 — diaryBg가 appBg(근-백색)로 설정되어 있어 Scaffold containerColor가 있어도 색상 차이 없음
- **WHO**: 조이어리 앱 사용자
- **RISK**: calendarBg는 테마에 따라 채도 높은 색상 — TopAppBar 텍스트 가독성 확인 필요
- **SUCCESS**: 글쓰기/수정/읽기 화면 전체(앱바 포함)가 선택된 테마의 calendarBg 색상으로 표시
- **SCOPE**: MainActivity.kt, DiaryEditorScreen.kt, DiaryDetailScreen.kt 3개 파일

[CP-1 Auto] 요구사항 확인됨
[CP-2 Auto] 합리적 기본값 적용

## 근본 원인

| 항목 | 값 | 설명 |
|------|-----|------|
| `template.themeColors.appBg` | `#F0F8FF` (Sky 기준) | 거의 흰색 |
| `template.themeColors.calendarBg` | `#8EC6E6` (Sky 기준) | 뚜렷한 테마 색상 |
| MainActivity 현재 설정 | `diaryBg = appBg` | → 색상 차이 없음 |
| DiaryEditorScreen TopAppBar | colors 미설정 | → surface(흰색) 사용 |
| DiaryPageContent Scaffold | containerColor 없음 | → surface(흰색) 사용 |
| DiaryPageContent TopAppBar | colors 미설정 | → surface(흰색) 사용 |

## 요구사항
- **FR-01**: MainActivity에서 `diaryBg = calendarBg`로 변경
- **FR-02**: DiaryEditorScreen TopAppBar에 `colors = TopAppBarDefaults.topAppBarColors(containerColor = diaryBg)` 추가
- **FR-03**: DiaryPageContent Scaffold에 `containerColor = diaryBg` 추가
- **FR-04**: DiaryPageContent TopAppBar에 `colors = TopAppBarDefaults.topAppBarColors(containerColor = diaryBg)` 추가

## Success Criteria
- SC-01: MainActivity diaryBg = template.themeColors.calendarBg
- SC-02: DiaryEditorScreen TopAppBar colors.containerColor = diaryBg
- SC-03: DiaryPageContent Scaffold containerColor = diaryBg
- SC-04: DiaryPageContent TopAppBar colors.containerColor = diaryBg
