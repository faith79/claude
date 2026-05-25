# Report: diary-editor-bg-setting

## 완료 요약
- **Feature**: diary-editor-bg-setting
- **Quality Gate**: 100% / 100% PASSED ✅
- **Iterations**: 1 / 5
- **Status**: completed

## 변경 내용

### 1. 어두운 테마 3종 추가 (AppThemeTemplate.kt)
| 인덱스 | 이름 | 달력배경 | 앱배경 | 액센트 |
|--------|------|---------|--------|--------|
| 20 | 미드나잇 | `#2C2C3E` (다크 퍼플) | `#121220` (거의 검정) | `#BB86FC` 보라 |
| 21 | 곤색 | `#1C3050` (딥 네이비) | `#0A1628` (딥 블루블랙) | `#7EAEDD` 하늘 |
| 22 | 다크브라운 | `#3E2018` (다크 브라운) | `#1C0F08` (에스프레소) | `#D4A876` 황금 |

모든 어두운 템플릿: `onSurface` = 밝은 색 (`#E0E0EE`, `#D8E8F8`, `#F0DCC8`)

### 2. DiaryBgPalette 확장 (Color.kt)
- 기존 10종 → 15종 (Light 5 + Medium 5 + Dark 5)
- 추가된 Dark: 슬레이트(#2C3E50), 곤색(#0D1E3A), 다크브라운(#2A1408), 미드나잇(#1E1A2E), 다크그린(#1A2E1C)

### 3. 글쓰기/수정 배경색 독립 설정 (SettingsScreen.kt)
- "글쓰기/수정 배경색" ColorPaletteRow 추가 (평일 글씨색 아래)
- `settingsViewModel.diaryBgColor` 연결

### 4. MainActivity.kt — 독립 설정값 사용
- `diaryBg = template.themeColors.calendarBg` → `diaryBg = settingsViewModel.diaryBgColor`
- 달력 색상과 글쓰기 배경색 완전 분리

### 5. Adaptive 텍스트/아이콘 색상 (DiaryEditorScreen + DiaryDetailScreen)
```
luminance = 0.299R + 0.587G + 0.114B
contentColor = if (luminance < 0.5) White else Black
```
- TopAppBar: title, 뒤로가기, 액션 아이콘 모두 adaptive
- OutlinedTextField: 입력 텍스트 adaptive

## 변경 파일
| File | 변경 유형 |
|------|-----------|
| `ui/theme/AppThemeTemplate.kt` | 수정 — 3 dark templates |
| `ui/theme/Color.kt` | 수정 — DiaryBgPalette 확장 |
| `ui/settings/SettingsScreen.kt` | 수정 — 글쓰기 배경색 설정 추가 |
| `MainActivity.kt` | 수정 — diaryBgColor 독립 사용 |
| `ui/diary/DiaryEditorScreen.kt` | 수정 — adaptive contentColor |
| `ui/diary/DiaryDetailScreen.kt` | 수정 — adaptive contentColor |
