# Design: joyary-app-improvements

## Architecture: Option C — Pragmatic Balance

### FR-01: HomeScreen — TopAppBar + FloatingActionButton

**변경 전 → 후:**
```
LargeTopAppBar + exitUntilCollapsedScrollBehavior  →  TopAppBar (no scrollBehavior)
Scaffold(modifier = nestedScroll(...))             →  Scaffold (no nestedScroll)
ExtendedFloatingActionButton(icon, text)           →  FloatingActionButton { Icon(Add) }
```

**제거 항목:**
- `val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()`
- `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)` on Scaffold
- `scrollBehavior = scrollBehavior` param in TopAppBar
- `import androidx.compose.ui.input.nestedscroll.nestedScroll`
- `ExtendedFloatingActionButton` text lambda

### FR-02: 글쓰기 배경 = 테마 연동

**SettingsScreen.kt:**
- `val diaryBgColor by settingsViewModel.diaryBgColor...` 수집 제거
- "글쓰기 배경색" `HorizontalDivider` + `ColorPaletteRow` 블록 제거

**MainActivity.kt:**
- `val diaryBg by settingsViewModel.diaryBgColor...` 수집 제거
- `LocalThemeColors provides template.themeColors.copy(diaryBg = diaryBg, ...)` →
  `LocalThemeColors provides template.themeColors.copy(weekdayColor = weekday)`
  (`diaryBg` 필드는 template.themeColors.appBg 와 동일하게 자동 설정됨)

> `ThemeColors.appBg` 는 이미 각 템플릿의 앱 배경색. `diaryBg` 를 오버라이드 하지 않으면
> 각 템플릿의 `ThemeColors.diaryBg` 기본값이 사용됨. 따라서 **`diaryBg = template.themeColors.appBg`** 로 명시 설정.

### FR-03: POST_NOTIFICATIONS 런타임 권한

**SettingsScreen.kt 추가 항목:**
```kotlin
val context = LocalContext.current  // 상단으로 호이스팅
val notifPermLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted -> if (granted) settingsViewModel.setReminderEnabled(true) }
```

**Switch.onCheckedChange:**
```kotlin
onCheckedChange = { enabled ->
    if (!enabled) { settingsViewModel.setReminderEnabled(false); return@Switch }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) settingsViewModel.setReminderEnabled(true)
        else notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        settingsViewModel.setReminderEnabled(true)
    }
}
```

### FR-04: 알림 아이콘

**신규 drawable `ic_notification.xml`:**
- 24dp viewport (notification small icon 표준)
- 별 모양 흰색 단색 (앱 아이콘 별 모양과 동일한 pathData 재사용)
- 배경 투명

**DailyReminderWorker:**
```kotlin
.setSmallIcon(R.drawable.ic_notification)
```

### FR-06: DiaryEditor 텍스트 검정

**DiaryEditorScreen.kt — OutlinedTextField:**
```kotlin
colors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
)
```

## Files Changed
| File | Type |
|------|------|
| `HomeScreen.kt` | modify |
| `SettingsScreen.kt` | modify |
| `MainActivity.kt` | modify |
| `DailyReminderWorker.kt` | modify |
| `DiaryEditorScreen.kt` | modify |
| `res/drawable/ic_notification.xml` | create |
