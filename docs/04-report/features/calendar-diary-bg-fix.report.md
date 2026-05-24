# Report: calendar-diary-bg-fix — 100% ✅

## 구현

### FR-01: 달력 상단 정렬
- `HomeScreen.kt` DayCell: `Arrangement.Center` → `Arrangement.Top` + `padding(top=6dp)`
- `HomeScreen.kt` Column 하단 `Box(weight(1f))` 제거 — 달력이 상단에 자연스럽게 위치

### FR-02: 글쓰기 배경색 = 색상테마 팔레트
- `SettingsScreen.kt`: `DiaryBgPalette` → `AppThemeTemplates.map { it.themeColors.appBg/nameKo }`
- 글쓰기 배경색 팔레트에 하늘~차콜 20종 테마 이름 그대로 표시
