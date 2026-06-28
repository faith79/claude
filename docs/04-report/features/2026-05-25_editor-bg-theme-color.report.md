# Report: editor-bg-theme-color

## 완료 요약
- **Feature**: editor-bg-theme-color
- **Quality Gate**: 100% / 100% PASSED ✅
- **Iterations**: 1 / 5
- **Status**: completed

## 근본 원인

| 문제 | 원인 |
|------|------|
| 글쓰기/수정/읽기 화면 배경 흰색 | `diaryBg = appBg` (근-백색 `#F0F8FF`) |
| TopAppBar 영역도 흰색 | TopAppBar에 colors 미설정 → surface(흰색) 사용 |
| DiaryPageContent 배경 흰색 | inner Scaffold에 containerColor 없음 |

## 변경 내용

### 1. MainActivity.kt
- `diaryBg = template.themeColors.appBg` → `diaryBg = template.themeColors.calendarBg`
- 효과: Sky 테마 기준 `#F0F8FF`(근-백색) → `#8EC6E6`(하늘색)으로 변경

### 2. DiaryEditorScreen.kt
- TopAppBar에 `colors = TopAppBarDefaults.topAppBarColors(containerColor = diaryBg)` 추가
- 효과: 앱바 영역도 테마 배경색으로 통일

### 3. DiaryDetailScreen.kt (DiaryPageContent)
- `val diaryBg = LocalThemeColors.current.diaryBg` 추가
- Scaffold에 `containerColor = diaryBg` 추가
- TopAppBar에 `colors = TopAppBarDefaults.topAppBarColors(containerColor = diaryBg)` 추가
- 효과: 읽기 화면 내부 페이지도 전체 배경 통일

## 변경 파일
| File | 변경 유형 |
|------|-----------|
| `MainActivity.kt` | 수정 |
| `ui/diary/DiaryEditorScreen.kt` | 수정 |
| `ui/diary/DiaryDetailScreen.kt` | 수정 |
