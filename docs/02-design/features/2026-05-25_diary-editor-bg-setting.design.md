# Design: diary-editor-bg-setting

## Architecture: Option C — Pragmatic Balance

### 핵심 설계 원칙
- `diaryBg` = `settingsViewModel.diaryBgColor` (SharedPreferences, 독립 설정)
- Luminance 기반 adaptive 텍스트 색상: `luminance = 0.299*R + 0.587*G + 0.114*B`
  - < 0.5 → `Color.White`, ≥ 0.5 → `Color.Black`
- 어두운 템플릿: `lightColorScheme`을 사용하되 어두운 배경값과 밝은 텍스트값 명시

### 어두운 템플릿 색상 설계

| 템플릿 | background | calendarBg | onBackground | primary accent |
|--------|-----------|------------|--------------|----------------|
| 미드나잇 | `#121220` | `#2C2C3E` | `#E0E0EE` | `#BB86FC` 퍼플 |
| 곤색 | `#0A1628` | `#1C3050` | `#D8E8F8` | `#7EAEDD` 하늘 |
| 다크브라운 | `#1C0F08` | `#3E2018` | `#F0DCC8` | `#D4A876` 황금 |

### DiaryBgPalette 확장 (Color.kt)
```
Light  : 흰색, 크림, 연분홍, 연보라, 민트
Medium : 하늘, 라벤더, 모카, 세이지, 코랄
Dark   : 슬레이트(#2C3E50), 곤색(#0D1E3A), 다크브라운(#2A1408), 미드나잇(#1E1A2E), 다크그린(#1A2E1C)
```

### Adaptive 텍스트 색상 (DiaryEditorScreen / DiaryDetailScreen)
```kotlin
val luminance = 0.299f * diaryBg.red + 0.587f * diaryBg.green + 0.114f * diaryBg.blue
val contentColor = if (luminance < 0.5f) Color.White else Color.Black

// TopAppBar
colors = TopAppBarDefaults.topAppBarColors(
    containerColor = diaryBg,
    titleContentColor = contentColor,
    navigationIconContentColor = contentColor,
    actionIconContentColor = contentColor
)

// OutlinedTextField (Editor only)
colors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = contentColor,
    unfocusedTextColor = contentColor,
)
```

### MainActivity.kt 변경
```kotlin
val diaryBg by settingsViewModel.diaryBgColor.collectAsStateWithLifecycle()
LocalThemeColors provides template.themeColors.copy(
    diaryBg = diaryBg,  // ← user-configured
    weekdayColor = weekday
)
```

## 변경 파일
| File | 변경 유형 |
|------|-----------|
| `ui/theme/Color.kt` | 수정 — DiaryBgPalette 확장 |
| `ui/theme/AppThemeTemplate.kt` | 수정 — 3 dark templates 추가 |
| `ui/settings/SettingsScreen.kt` | 수정 — diary bg 팔레트 추가 |
| `MainActivity.kt` | 수정 — diaryBgColor 사용 |
| `ui/diary/DiaryEditorScreen.kt` | 수정 — adaptive text color |
| `ui/diary/DiaryDetailScreen.kt` | 수정 — adaptive TopAppBar color |
