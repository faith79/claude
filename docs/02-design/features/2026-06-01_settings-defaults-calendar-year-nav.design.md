# Design: settings-defaults-calendar-year-nav

## Architecture: Option C — Pragmatic Balance

기존 SharedPreferences 구조와 SettingsViewModel을 유지하면서 최소 수정으로 기본값만 변경.

---

## CHANGE-01: ThemePreferences.kt — 기본값 변경

```kotlin
// selectedTemplateIndex: 0 → 20 (미드나잇)
var selectedTemplateIndex: Int
    get() = prefs.getInt("selected_theme_index", 20)   // 0 → 20
    set(value) { ... }

fun resetToDefault() { selectedTemplateIndex = 20 }   // 0 → 20

// weekdayColor: 0xFF424242 → 0xFFFFFFFF (흰색)
var weekdayColor: Int
    get() = prefs.getInt("weekday_color", 0xFFFFFFFF.toInt())
    set(value) { ... }

// diaryBgColor: 0xFFFFF8F0 → 0xFF000000 (검정)
var diaryBgColor: Int
    get() = prefs.getInt("diary_bg_color", 0xFF000000.toInt())
    set(value) { ... }

fun resetDiaryColors() {
    diaryBgColor = 0xFF000000.toInt()    // 검정
    weekdayColor = 0xFFFFFFFF.toInt()    // 흰색
}
```

---

## CHANGE-02: SettingsViewModel.kt — resetThemeTemplate 수정

```kotlin
fun resetThemeTemplate() {
    themePreferences.resetToDefault()
    _selectedTemplateIndex.value = 20   // 0 → 20 (미드나잇 기본)
}
```

---

## CHANGE-03: HomeScreen.kt — 년 단위 화살표 네비게이션

```kotlin
// Before — 월 단위 이동
onPrev = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }

// After — 년 단위 이동 (12개월)
onPrev = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 12) } }
onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 12) } }
```

---

## CHANGE-04: CalendarHeader — << >> 아이콘 교체

```kotlin
// Before
IconButton(onClick = onPrev) {
    Icon(Icons.AutoMirrored.Filled.ArrowBack, "이전 달")
}
IconButton(onClick = onNext) {
    Icon(Icons.AutoMirrored.Filled.ArrowForward, "다음 달")
}

// After
IconButton(onClick = onPrev) {
    Text("<<", style = MaterialTheme.typography.titleMedium,
         fontWeight = FontWeight.Bold,
         color = MaterialTheme.colorScheme.onSurfaceVariant)
}
IconButton(onClick = onNext) {
    Text(">>", style = MaterialTheme.typography.titleMedium,
         fontWeight = FontWeight.Bold,
         color = MaterialTheme.colorScheme.onSurfaceVariant)
}
```

- `ArrowBack`, `ArrowForward` import 제거
- `FontWeight` import 추가: `androidx.compose.ui.text.font.FontWeight`
