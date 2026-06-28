# Design: calendar-diary-bg-fix

## Architecture: Option C — Pragmatic Balance

### FR-01: 달력 상단 정렬
- `DayCell`: `verticalArrangement = Arrangement.Center` → `Arrangement.Top`
- `DayCell` modifier에 `.padding(top = 6.dp)` 추가
- `HomeScreen` Column에서 `Box(modifier = Modifier.weight(1f))` 제거

### FR-02: 글쓰기 배경색 = 색상테마 팔레트
- `SettingsScreen.kt`: `DiaryBgPalette` import 제거
- `colors = AppThemeTemplates.map { it.themeColors.appBg }`
- `labels = AppThemeTemplates.map { it.nameKo }`
- ColorPaletteRow 시그니처 변경 없음
