# Design: joyary-ux-improvements

## Architecture: Option C — Pragmatic Balance

### FR-01/02: 알림 아이콘 + 클릭 액션
- `DailyReminderWorker.kt`: setSmallIcon → R.drawable.ic_launcher_foreground
- PendingIntent(FLAG_IMMUTABLE) → MainActivity (NEW_TASK | CLEAR_TASK)
- setContentIntent(pendingIntent) 추가

### FR-03: 이전/다음 달 미리 불러오기
- `DiaryViewModel.prefetchMonth(userId, yearMonth)`: 캐시 miss 시 background 로드, _diaries 업데이트 없음
- `HomeScreen.LaunchedEffect(settledPage)`: loadMonth + prefetchMonth(-1달, +1달)

### FR-04: 에디터 배경색 관리자 설정
- `MainActivity.kt`: diaryBg = settingsViewModel.diaryBgColor (appBg 하드코딩 해제)
- `DiaryEditorScreen.kt`: Column에 .background(LocalThemeColors.current.diaryBg) 추가
- `SettingsScreen.kt`: ColorPaletteRow("글쓰기 배경색", DiaryBgPalette) 추가

### FR-05: 색상 템플릿 20개
- `AppThemeTemplate.kt`: 10개 신규 추가 (인디고, 에메랄드, 써니, 체리, 딥블루, 올리브, 스틸, 자수정, 오션, 차콜)
- 인덱스 10-19, AppThemeTemplates 리스트에 append
