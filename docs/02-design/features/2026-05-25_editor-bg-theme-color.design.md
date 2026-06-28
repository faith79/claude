# Design: editor-bg-theme-color

## Architecture: Option C — Pragmatic Balance

### 변경 전/후

#### MainActivity.kt
```kotlin
// 변경 전
LocalThemeColors provides template.themeColors.copy(
    diaryBg = template.themeColors.appBg,   // 근-백색
    weekdayColor = weekday
)

// 변경 후
LocalThemeColors provides template.themeColors.copy(
    diaryBg = template.themeColors.calendarBg,  // 뚜렷한 테마 색상
    weekdayColor = weekday
)
```

#### DiaryEditorScreen.kt
```kotlin
// 변경 전
TopAppBar(
    title = { ... },
    ...
)

// 변경 후
TopAppBar(
    colors = TopAppBarDefaults.topAppBarColors(containerColor = diaryBg),
    title = { ... },
    ...
)
```

#### DiaryDetailScreen.kt (DiaryPageContent)
```kotlin
// 변경 전
Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { ... },
                ...
            )
        }
    ) { ... }
}

// 변경 후
val diaryBg = LocalThemeColors.current.diaryBg
Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = diaryBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = diaryBg),
                title = { ... },
                ...
            )
        }
    ) { ... }
}
```

## 변경 파일
| File | 변경 유형 |
|------|-----------|
| `MainActivity.kt` | 수정 |
| `ui/diary/DiaryEditorScreen.kt` | 수정 |
| `ui/diary/DiaryDetailScreen.kt` | 수정 |
