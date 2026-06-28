# joyary-upgrade-v3 Design

> **Feature**: joyary-upgrade-v3
> **Date**: 2026-05-17
> **Author**: faith79@jobkorea.co.kr
> **Architecture**: Option C — Pragmatic Balance
> **Phase**: Design

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 달력 가시성 문제 즉시 해결 + 사용자 취향 반영 가능한 테마 설정 |
| **WHO** | 조이어리 앱 기존 사용자 (기본 색상이 안 보인다는 불편 경험자) |
| **RISK** | CompositionLocal provide 누락 시 crash, SharedPreferences Int↔Color 변환 오류, 팔레트 색상 텍스트 대비 |
| **SUCCESS** | FR-01~FR-08 구현 완료 + 색상 변경 후 즉시 홈/편집 화면 반영 확인 |
| **SCOPE** | UI 레이어 + 설정 레이어 (데이터/인증 무변경), 신규 2개 + 수정 7개 파일 |

---

## 1. Overview

### 1.1 아키텍처 흐름

```
┌─────────────────────────────────────────────────────────────┐
│ ThemePreferences (SharedPreferences)                        │
│   calendarBgColor: Int  appBgColor: Int  todayBgColor: Int  │
└──────────────────────┬──────────────────────────────────────┘
                       │ inject
┌──────────────────────▼──────────────────────────────────────┐
│ SettingsViewModel                                           │
│   _calendarBgColor: MutableStateFlow<Color>                 │
│   _appBgColor:      MutableStateFlow<Color>                 │
│   _todayBgColor:    MutableStateFlow<Color>                 │
└──────────────────────┬──────────────────────────────────────┘
                       │ collectAsStateWithLifecycle
┌──────────────────────▼──────────────────────────────────────┐
│ MainActivity (DiaryAppTheme 내부)                            │
│   LocalThemeColors.provides(ThemeColors(cal, app, today))   │
└──────────────────────┬──────────────────────────────────────┘
                       │ CompositionLocal
       ┌───────────────┼───────────────────┐
┌──────▼──────┐  ┌─────▼──────┐  ┌────────▼───────┐
│ HomeScreen  │  │DiaryEditor │  │SettingsScreen  │
│ .calendarBg │  │ .appBg     │  │ 팔레트 UI       │
│ .todayBg    │  │            │  │                │
└─────────────┘  └────────────┘  └────────────────┘
```

### 1.2 선택 이유 (Option C)

- 기존 `NotificationPreferences` 패턴과 100% 동일 — 학습 비용 0
- `CompositionLocal`로 각 화면이 SettingsViewModel을 직접 import할 필요 없음
- Repository 계층 없이 단순하게 색상 저장/로드

---

## 2. Data Layer

### 2.1 ThemePreferences.kt (신규)

**위치**: `notification/ThemePreferences.kt` → NotificationPreferences와 같은 패키지

```kotlin
class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    // 달력 배경색 (기본: SkyCalendarBg = 0xFF8EC6E6)
    var calendarBgColor: Int
        get() = prefs.getInt("calendar_bg_color", 0xFF8EC6E6.toInt())
        set(value) { prefs.edit().putInt("calendar_bg_color", value).apply() }

    // 앱 배경색 (기본: SkyBackground = 0xFFF0F8FF)
    var appBgColor: Int
        get() = prefs.getInt("app_bg_color", 0xFFF0F8FF.toInt())
        set(value) { prefs.edit().putInt("app_bg_color", value).apply() }

    // 오늘 날짜 배경색 (기본: SkyBlue = 0xFF7EC8E3)
    var todayBgColor: Int
        get() = prefs.getInt("today_bg_color", 0xFF7EC8E3.toInt())
        set(value) { prefs.edit().putInt("today_bg_color", value).apply() }

    fun resetToDefaults() {
        prefs.edit()
            .putInt("calendar_bg_color", 0xFF8EC6E6.toInt())
            .putInt("app_bg_color", 0xFFF0F8FF.toInt())
            .putInt("today_bg_color", 0xFF7EC8E3.toInt())
            .apply()
    }
}
```

### 2.2 DI 연결

**위치**: `di/NotificationModule.kt` — ThemePreferences도 같이 provide

```kotlin
// NotificationModule.kt에 추가
@Provides
@Singleton
fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences =
    ThemePreferences(context)
```

---

## 3. Theme Layer

### 3.1 LocalThemeColors.kt (신규)

**위치**: `ui/theme/LocalThemeColors.kt`

```kotlin
data class ThemeColors(
    val calendarBg: Color,   // 달력 카드 배경
    val appBg: Color,        // 앱 배경 (편집기 포함)
    val todayBg: Color       // 오늘 날짜 셀 배경
) {
    companion object {
        val Default = ThemeColors(
            calendarBg = Color(0xFF8EC6E6),  // SkyCalendarBg (진하게)
            appBg      = Color(0xFFF0F8FF),  // SkyBackground
            todayBg    = Color(0xFF7EC8E3)   // SkyBlue
        )
    }
}

val LocalThemeColors = compositionLocalOf { ThemeColors.Default }
```

### 3.2 Color.kt 변경 (FR-01)

```kotlin
// 변경 전
val SkyCalendarBg = Color(0xFFE8F4FD)  // 너무 연함

// 변경 후
val SkyCalendarBg = Color(0xFF8EC6E6)  // 진한 하늘색 (가시성 향상)
```

### 3.3 팔레트 상수 정의 (Color.kt 추가)

```kotlin
// 달력 배경 팔레트 (10가지 — 중간~진한 하늘색)
val CalendarBgPalette = listOf(
    Color(0xFFB3D9F0), Color(0xFF8EC6E6), Color(0xFF6BB4DC),
    Color(0xFF5AAAC8), Color(0xFF3D98BA), Color(0xFF2E86AB),
    Color(0xFFD1EAF8), Color(0xFFA0CBDF), Color(0xFF7ABCD6),
    Color(0xFFE8F4FD)  // 원래 기본값 (연함)
)

// 앱 배경 팔레트 (10가지 — 매우 연한 하늘/흰색 계열)
val AppBgPalette = listOf(
    Color(0xFFF0F8FF), Color(0xFFE8F4FD), Color(0xFFDDF0FB),
    Color(0xFFD0E8F5), Color(0xFFC2E0EF), Color(0xFFB0D4E8),
    Color(0xFFF5FBFF), Color(0xFFEBF6FC), Color(0xFFE0F1FA),
    Color(0xFFFFFFFF)  // 순수 흰색
)

// 오늘 날짜 강조 팔레트 (10가지 — 선명한 파랑)
val TodayBgPalette = listOf(
    Color(0xFF7EC8E3), Color(0xFF5BB8D4), Color(0xFF3DA8C5),
    Color(0xFF2998B6), Color(0xFF1588A7), Color(0xFF81D4FA),
    Color(0xFF4FC3F7), Color(0xFF29B6F6), Color(0xFF03A9F4),
    Color(0xFF0288D1)
)
```

---

## 4. ViewModel Layer

### 4.1 SettingsViewModel.kt 확장

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val notificationPreferences: NotificationPreferences,
    private val themePreferences: ThemePreferences,  // 신규 inject
    private val workManager: WorkManager
) : ViewModel() {

    // 기존 알림 관련 상태 유지 ...

    // 신규: 테마 색상 StateFlow
    private val _calendarBgColor = MutableStateFlow(Color(themePreferences.calendarBgColor))
    val calendarBgColor: StateFlow<Color> = _calendarBgColor.asStateFlow()

    private val _appBgColor = MutableStateFlow(Color(themePreferences.appBgColor))
    val appBgColor: StateFlow<Color> = _appBgColor.asStateFlow()

    private val _todayBgColor = MutableStateFlow(Color(themePreferences.todayBgColor))
    val todayBgColor: StateFlow<Color> = _todayBgColor.asStateFlow()

    fun setCalendarBgColor(color: Color) {
        themePreferences.calendarBgColor = color.toArgb()
        _calendarBgColor.value = color
    }

    fun setAppBgColor(color: Color) {
        themePreferences.appBgColor = color.toArgb()
        _appBgColor.value = color
    }

    fun setTodayBgColor(color: Color) {
        themePreferences.todayBgColor = color.toArgb()
        _todayBgColor.value = color
    }

    fun resetThemeColors() {
        themePreferences.resetToDefaults()
        _calendarBgColor.value = Color(themePreferences.calendarBgColor)
        _appBgColor.value = Color(themePreferences.appBgColor)
        _todayBgColor.value = Color(themePreferences.todayBgColor)
    }
}
```

---

## 5. UI Layer

### 5.1 MainActivity.kt 수정

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val calendarBg by settingsViewModel.calendarBgColor.collectAsStateWithLifecycle()
            val appBg      by settingsViewModel.appBgColor.collectAsStateWithLifecycle()
            val todayBg    by settingsViewModel.todayBgColor.collectAsStateWithLifecycle()

            DiaryAppTheme {
                CompositionLocalProvider(
                    LocalThemeColors provides ThemeColors(calendarBg, appBg, todayBg)
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val start = remember {
                        if (authViewModel.isLoggedIn) Screen.Home.route else Screen.Login.route
                    }
                    NavGraph(navController = navController, startDestination = start)
                }
            }
        }
    }
}
```

### 5.2 SettingsScreen.kt 테마 섹션 UI

#### ColorPaletteRow 컴포넌트

```kotlin
@Composable
private fun ColorPaletteRow(
    label: String,
    palette: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(palette) { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}
```

#### 테마 섹션 (SettingsScreen 본문에 추가)

```kotlin
// 기존 알림 섹션 아래에 추가
Spacer(Modifier.height(24.dp))
Text(
    "테마",
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.primary
)
Spacer(Modifier.height(8.dp))

Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        ColorPaletteRow(
            label = "달력 배경색",
            palette = CalendarBgPalette,
            selectedColor = calendarBgColor,
            onColorSelected = settingsViewModel::setCalendarBgColor
        )
        HorizontalDivider()
        ColorPaletteRow(
            label = "앱 배경색",
            palette = AppBgPalette,
            selectedColor = appBgColor,
            onColorSelected = settingsViewModel::setAppBgColor
        )
        HorizontalDivider()
        ColorPaletteRow(
            label = "오늘 날짜 배경색",
            palette = TodayBgPalette,
            selectedColor = todayBgColor,
            onColorSelected = settingsViewModel::setTodayBgColor
        )
        HorizontalDivider()
        TextButton(
            onClick = settingsViewModel::resetThemeColors,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("기본값으로 초기화")
        }
    }
}
```

### 5.3 HomeScreen.kt 색상 적용

```kotlin
// 기존: Card(containerColor = SkyCalendarBg) { ... }
// 변경: LocalThemeColors.current.calendarBg 사용
val themeColors = LocalThemeColors.current

Card(containerColor = themeColors.calendarBg) { ... }

// DayCell에서 오늘 날짜 배경
// 기존: background(MaterialTheme.colorScheme.primaryContainer)
// 변경: background(themeColors.todayBg)
```

### 5.4 DiaryEditorScreen.kt 색상 적용

```kotlin
// Scaffold 배경색에 appBg 적용
val themeColors = LocalThemeColors.current

Scaffold(
    containerColor = themeColors.appBg,
    ...
)
```

---

## 6. Page UI Checklist

### 설정 화면 — 테마 섹션

- [ ] "테마" 섹션 헤더 표시
- [ ] 달력 배경색 레이블 + 10개 색상 원형 팔레트
- [ ] 앱 배경색 레이블 + 10개 색상 원형 팔레트
- [ ] 오늘 날짜 배경색 레이블 + 10개 색상 원형 팔레트
- [ ] 선택된 색상 굵은 테두리 표시
- [ ] 기본값으로 초기화 버튼

### 홈 화면 (색상 반영 확인)

- [ ] 달력 배경이 테마 설정 색상으로 표시
- [ ] 오늘 날짜 셀 배경이 테마 설정 색상으로 표시

### 일기 편집기 화면

- [ ] 화면 배경이 테마 설정 앱 배경색으로 표시

---

## 7. DI Module 변경

### NotificationModule.kt 추가

```kotlin
@Provides
@Singleton
fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences =
    ThemePreferences(context)
```

---

## 8. Test Plan

| 테스트 | 시나리오 | 기대 결과 |
|--------|---------|---------|
| T-01 | 설정 > 달력 배경색 팔레트에서 다른 색 선택 → 홈 복귀 | 달력 배경 즉시 변경 |
| T-02 | 설정 > 앱 배경색 변경 → 일기 편집 화면 진입 | 편집기 배경 변경 |
| T-03 | 설정 > 오늘 날짜 배경색 변경 → 홈 복귀 | 오늘 날짜 셀 배경 변경 |
| T-04 | 색상 변경 후 앱 종료 → 재실행 | 변경 색상 유지 |
| T-05 | 기본값으로 초기화 버튼 탭 | 3가지 색상 모두 기본값 복원 |
| T-06 | 달력 평일 글씨 가독성 확인 (FR-01) | 글씨가 배경과 충분히 대비 |
| T-07 | 기존 알림 설정 동작 확인 | 회귀 없음 |
| T-08 | 기존 일기 CRUD 동작 확인 | 회귀 없음 |

---

## 9. Risk & Mitigation

| Risk | 대응 |
|------|------|
| CompositionLocal provide 범위 누락 | MainActivity 최상위에서 provide + ThemeColors.Default 기본값으로 null safe |
| `color.toArgb()` 부호 처리 | `Color(int)` 생성자는 ARGB int를 그대로 처리 — `0xFF8EC6E6.toInt()` 형태로 명시 |
| 기존 `SkyCalendarBg` 직접 참조 | HomeScreen.kt grep으로 하드코딩 참조 확인 후 LocalThemeColors.current.calendarBg로 교체 |

---

## 10. File Summary

| 파일 | 변경 유형 | 변경 내용 |
|------|---------|---------|
| `ui/theme/Color.kt` | 수정 | SkyCalendarBg #8EC6E6으로 변경 + 3개 팔레트 List 추가 |
| `notification/ThemePreferences.kt` | **신규** | SharedPreferences 색상 저장소 |
| `ui/theme/LocalThemeColors.kt` | **신규** | ThemeColors 데이터 클래스 + CompositionLocal |
| `di/NotificationModule.kt` | 수정 | ThemePreferences DI 바인딩 추가 |
| `viewmodel/SettingsViewModel.kt` | 수정 | ThemePreferences inject + 3개 StateFlow + 3개 setter + reset |
| `ui/settings/SettingsScreen.kt` | 수정 | 테마 섹션 + ColorPaletteRow 컴포넌트 추가 |
| `MainActivity.kt` | 수정 | SettingsViewModel collect + CompositionLocalProvider |
| `ui/home/HomeScreen.kt` | 수정 | LocalThemeColors.current.calendarBg / todayBg 사용 |
| `ui/diary/DiaryEditorScreen.kt` | 수정 | LocalThemeColors.current.appBg 사용 |

**신규 2개 / 수정 7개**

---

## 11. Implementation Guide

### 11.1 구현 순서

```
① Color.kt — SkyCalendarBg 변경 + 팔레트 상수 정의
② ThemePreferences.kt — SharedPreferences 래퍼 생성
③ LocalThemeColors.kt — ThemeColors + CompositionLocal 생성
④ NotificationModule.kt — ThemePreferences DI 추가
⑤ SettingsViewModel.kt — ThemePreferences inject + StateFlow 3개 추가
⑥ SettingsScreen.kt — 테마 섹션 + ColorPaletteRow UI
⑦ MainActivity.kt — collect + CompositionLocalProvider
⑧ HomeScreen.kt — LocalThemeColors.current.calendarBg / todayBg 교체
⑨ DiaryEditorScreen.kt — LocalThemeColors.current.appBg 교체
```

### 11.2 핵심 주의사항

- `Color(int)` 생성자에서 ARGB Int 처리: `0xFF8EC6E6.toInt()` (Long → Int 명시 변환 필수)
- `CompositionLocalProvider`는 `DiaryAppTheme { ... }` 내부, `NavGraph` 외부에 배치
- `LocalThemeColors.current`는 Composable 함수 내에서만 호출 가능

### 11.3 Session Guide

| 모듈 | 파일 | 예상 시간 |
|------|------|---------|
| Module 1 — 토대 | Color.kt, ThemePreferences, LocalThemeColors, NotificationModule | 15분 |
| Module 2 — ViewModel + UI | SettingsViewModel, SettingsScreen | 20분 |
| Module 3 — 연결 | MainActivity, HomeScreen, DiaryEditorScreen | 15분 |

---

## Version History

| Version | Date | Author |
|---------|------|--------|
| 0.1 | 2026-05-17 | faith79@jobkorea.co.kr |
