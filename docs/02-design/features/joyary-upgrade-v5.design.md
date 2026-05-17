# joyary-upgrade-v5 Design Document

> **Summary**: 달력 6줄 고정 + 이미지 300KB 압축 + 일기 배경색/평일 글씨색 개별 10색 선택
>
> **Project**: claude / diary-app
> **Version**: 0.5.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-17
> **Status**: Draft
> **Planning Doc**: [joyary-upgrade-v5.plan.md](../01-plan/features/joyary-upgrade-v5.plan.md)

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 달력 높이 불일치로 UX 이질감; 이미지 용량 과다; 일기 배경/평일색 커스터마이징 불가 |
| **WHO** | 조이어리 기존 사용자 (꼼꼼한 UI 일관성을 원하는 사용자, 색상 커스터마이징을 즐기는 사용자) |
| **RISK** | ThemeColors에 필드 추가 시 v4 AppThemeTemplate과 충돌 가능성; LazyVerticalGrid 42개 고정 시 빈 셀 처리 |
| **SUCCESS** | 달력 모든 달 동일 높이 + 이미지 ≤300KB 저장 + 설정에서 일기배경/평일색 선택 즉시 반영 |
| **SCOPE** | HomeScreen(달력), ImageCompressor, ThemeColors/Preferences/ViewModel/Screen(색상), DiaryEditor/Detail(배경) |

---

## 1. Overview

### 1.1 Design Goals

1. **달력 6줄 고정**: HorizontalPager 월 전환 시 레이아웃 점프 제거
2. **이미지 압축 강화**: 300KB 한도로 Firebase Storage/네트워크 비용 절감
3. **일기 배경색 통일**: 작성·보기 화면 동일 배경 + 10색 팔레트 선택
4. **달력 평일 글씨색 선택**: 10색 팔레트 선택 + 즉시 반영

### 1.2 Design Principles

- **기존 패턴 재사용**: v3/v4에서 검증된 ColorPaletteRow + SharedPreferences + StateFlow 패턴 그대로 적용
- **기본값 보호**: ThemeColors 신규 필드에 기본값 부여 → AppThemeTemplate.kt 10개 인스턴스 무수정
- **단일 진입점**: MainActivity에서만 ThemeColors를 생성/override → 데이터 흐름 일관성 유지

---

## 2. Architecture

### 2.0 Architecture Selection

**Selected**: **Option C — Pragmatic Balance**

| 기준 | 결정 |
|------|------|
| 팔레트 정의 위치 | `Color.kt` 내 `object DiaryBgPalette`, `object WeekdayColorPalette` |
| ThemeColors 확장 | 기본값 있는 선택적 파라미터로 추가 — AppThemeTemplate.kt 무수정 |
| 설정 UI | 기존 `ColorPaletteRow` 재사용 (v3 스타일) |
| 상태 관리 | 기존 `SettingsViewModel` + `ThemePreferences` 확장 |
| 신규 파일 | 0개 |

**Rationale**: v3/v4에서 동일 패턴이 검증됨. 신규 파일 없이 일관성 유지.

### 2.1 Component Diagram

```
SharedPreferences
  ├── "diary_bg_color"     (Int, default = 0xFFFFF8F0)
  └── "weekday_color"      (Int, default = 0xFF424242)
       ↓
ThemePreferences
  ├── diaryBgColor: Int
  └── weekdayColor: Int
       ↓
SettingsViewModel
  ├── _diaryBgColor: MutableStateFlow<Color>
  ├── _weekdayColor: MutableStateFlow<Color>
  ├── setDiaryBgColor(color: Color)
  └── setWeekdayColor(color: Color)
       ↓
MainActivity (collectAsStateWithLifecycle)
  └── LocalThemeColors provides template.themeColors.copy(
          diaryBg = diaryBg,
          weekdayColor = weekday
      )
       ↓
┌─────────────────────────────┐
│ HomeScreen                  │
│   DayCell: weekdayColor     │ ← LocalThemeColors.current.weekdayColor
├─────────────────────────────┤
│ DiaryEditorScreen           │
│   containerColor: diaryBg   │ ← LocalThemeColors.current.diaryBg
├─────────────────────────────┤
│ DiaryDetailScreen           │
│   containerColor: diaryBg   │ ← LocalThemeColors.current.diaryBg
└─────────────────────────────┘
```

### 2.2 Data Flow

```
SettingsScreen 팔레트 클릭
  → SettingsViewModel.setDiaryBgColor(color) / setWeekdayColor(color)
  → ThemePreferences.diaryBgColor = color.toArgb()
  → StateFlow emit
  → MainActivity collectAsStateWithLifecycle 반응
  → LocalThemeColors.copy() 재생성
  → Compose recomposition → 즉시 반영
```

### 2.3 Dependencies

| Component | Depends On | Purpose |
|-----------|-----------|---------|
| `ThemeColors` | - | diaryBg, weekdayColor 필드 보유 |
| `Color.kt` | - | DiaryBgPalette, WeekdayColorPalette object 정의 |
| `ThemePreferences` | SharedPreferences | 색상 Int 저장/복원 |
| `SettingsViewModel` | ThemePreferences | StateFlow 관리 |
| `MainActivity` | SettingsViewModel, AppThemeTemplates | ThemeColors override 및 provide |
| `SettingsScreen` | SettingsViewModel | ColorPaletteRow 2개 표시 |
| `HomeScreen` | LocalThemeColors | weekdayColor 소비 |
| `DiaryEditorScreen` | LocalThemeColors | diaryBg 소비 |
| `DiaryDetailScreen` | LocalThemeColors | diaryBg 소비 |

---

## 3. Data Model

### 3.1 ThemeColors 확장

```kotlin
// ui/theme/LocalThemeColors.kt
data class ThemeColors(
    val calendarBg: Color,
    val appBg: Color,
    val todayBg: Color,
    // v5 추가 — 기본값으로 AppThemeTemplate.kt 무수정
    val diaryBg: Color = Color(0xFFFFF8F0),      // 크림 (기본)
    val weekdayColor: Color = Color(0xFF424242)   // 진회색 (기본)
)
```

### 3.2 색상 팔레트 (Color.kt)

```kotlin
// ui/theme/Color.kt — object로 네임스페이스 분리

object DiaryBgPalette {
    val colors = listOf(
        Color(0xFFFFFFFF),  // 0: 흰색
        Color(0xFFFFF8F0),  // 1: 크림 (기본)
        Color(0xFFFFFDE7),  // 2: 연노랑
        Color(0xFFFFF0F5),  // 3: 연분홍
        Color(0xFFF5F0FF),  // 4: 연보라
        Color(0xFFF0FAF6),  // 5: 민트 크림
        Color(0xFFF0F8FF),  // 6: 하늘 크림
        Color(0xFFF2F8F0),  // 7: 세이지 크림
        Color(0xFFFBF8F5),  // 8: 모카 크림
        Color(0xFFFFF2F0),  // 9: 코랄 크림
    )
    val labels = listOf(
        "흰색", "크림", "연노랑", "연분홍", "연보라",
        "민트", "하늘", "세이지", "모카", "코랄"
    )
}

object WeekdayColorPalette {
    val colors = listOf(
        Color(0xFF424242),  // 0: 진회색 (기본)
        Color(0xFF37474F),  // 1: 차콜
        Color(0xFF1A237E),  // 2: 네이비
        Color(0xFF1B5E20),  // 3: 딥그린
        Color(0xFF4A148C),  // 4: 딥퍼플
        Color(0xFF0D47A1),  // 5: 로얄블루
        Color(0xFF004D40),  // 6: 딥틸
        Color(0xFF3E2723),  // 7: 딥브라운
        Color(0xFFBF360C),  // 8: 딥오렌지
        Color(0xFF546E7A),  // 9: 블루그레이
    )
    val labels = listOf(
        "진회색", "차콜", "네이비", "딥그린", "딥퍼플",
        "로얄블루", "딥틸", "딥브라운", "딥오렌지", "블루그레이"
    )
}
```

### 3.3 ThemePreferences 확장

```kotlin
// notification/ThemePreferences.kt
var diaryBgColor: Int
    get() = prefs.getInt("diary_bg_color", 0xFFFFF8F0.toInt())
    set(value) { prefs.edit().putInt("diary_bg_color", value).apply() }

var weekdayColor: Int
    get() = prefs.getInt("weekday_color", 0xFF424242.toInt())
    set(value) { prefs.edit().putInt("weekday_color", value).apply() }

fun resetDiaryColors() {
    diaryBgColor = 0xFFFFF8F0.toInt()
    weekdayColor = 0xFF424242.toInt()
}
```

---

## 4. API Specification

해당 없음 — 로컬 UI 전용 기능 (SharedPreferences 기반).

---

## 5. UI/UX Design

### 5.1 SettingsScreen — 색상 팔레트 행

```
┌────────────────────────────────────────────────────────┐
│  테마 색상            [현재 10종 테마 카드 LazyRow]    │
├────────────────────────────────────────────────────────┤
│  일기 배경색                                           │
│  ○ ○ ○ ○ ○ ○ ○ ○ ○ ○    ← DiaryBgPalette (10색)      │
│  흰 크 노 분 보 민 하 세 모 코                         │
├────────────────────────────────────────────────────────┤
│  평일 글씨색                                           │
│  ● ● ● ● ● ● ● ● ● ●    ← WeekdayColorPalette (10색)  │
│  진 차 네 딥 딥 로 딥 딥 딥 블                         │
├────────────────────────────────────────────────────────┤
│  [초기화]                                              │
└────────────────────────────────────────────────────────┘
```

### 5.2 CalendarGrid — 6줄 고정

```
변경 전: 4월 → 5행, 5월 → 6행 (레이아웃 점프)
변경 후: 모든 달 → 6행 (42셀 패딩)

┌─────────────────────────────────────────┐
│  일  월  화  수  목  금  토             │
│  [ ] [ ] [ ] 1   2   3   4             │
│  5   6   7   8   9   10  11            │
│  12  13  14  15  16  17  18            │
│  19  20  21  22  23  24  25            │
│  26  27  28  29  30  [ ] [ ]           │
│  [ ] [ ] [ ] [ ] [ ] [ ] [ ]  ← 패딩  │
└─────────────────────────────────────────┘
```

### 5.3 Component List

| Component | 위치 | 변경 내용 |
|-----------|------|---------|
| `ThemeColors` | `ui/theme/LocalThemeColors.kt` | `diaryBg`, `weekdayColor` 필드 추가 |
| `DiaryBgPalette` | `ui/theme/Color.kt` | object로 10색 팔레트 정의 |
| `WeekdayColorPalette` | `ui/theme/Color.kt` | object로 10색 팔레트 정의 |
| `CalendarGrid` | `ui/home/HomeScreen.kt` | cells 42개 패딩 |
| `DayCell` | `ui/home/HomeScreen.kt` | weekdayColor 파라미터 추가 |
| `ColorPaletteRow` | `ui/settings/SettingsScreen.kt` | 재사용 (일기배경, 평일글씨 2행) |

### 5.4 Page UI Checklist

#### SettingsScreen

- [ ] 팔레트 행: "일기 배경색" 레이블 + 원형 색상 10개 (흰색~코랄 크림)
- [ ] 팔레트 행: "평일 글씨색" 레이블 + 원형 색상 10개 (진회색~블루그레이)
- [ ] 선택된 색상에 체크마크 또는 강조 테두리 표시
- [ ] 초기화 버튼 → diaryBg=크림(#FFF8F0), weekday=진회색(#424242) 복원
- [ ] 기존 "색상 테마" 행(ThemeTemplateSelector) 회귀 없음

#### HomeScreen — CalendarGrid

- [ ] 모든 달에서 6줄(42셀) 동일하게 표시
- [ ] 패딩 빈 셀은 텍스트 없는 투명 셀로 표시
- [ ] 평일(월~금) 날짜 텍스트 색상이 weekdayColor 반영
- [ ] 토·일요일 색상 변화 없음 (DateSaturday/DateSunday 유지)
- [ ] 오늘 날짜 배경색 변화 없음 (todayBg 유지)

#### DiaryEditorScreen

- [ ] Scaffold containerColor = LocalThemeColors.current.diaryBg
- [ ] 작성 화면 배경이 설정에서 선택한 일기 배경색으로 표시

#### DiaryDetailScreen

- [ ] Scaffold containerColor = LocalThemeColors.current.diaryBg
- [ ] 보기 화면 배경이 설정에서 선택한 일기 배경색으로 동일 표시

---

## 6. Error Handling

| 상황 | 처리 방식 |
|------|---------|
| SharedPreferences 색상 값 범위 오류 | `getOrElse` → 기본값(크림/진회색) 사용 |
| 이미지 quality=10에서도 300KB 초과 | 그대로 저장 (무한루프 방지) |
| 달력 42셀 패딩 시 음수 startDayOfWeek | `coerceIn(0..6)` 처리 |
| ThemeColors.copy() null 위험 | Color 기본값이 null 아님 — 해당 없음 |

---

## 7. Security Considerations

- 해당 없음 — 로컬 UI 전용, 외부 API/입력값 없음
- 이미지 압축은 기존 검증된 `ImageCompressor` 로직 활용

---

## 8. Test Plan

### 8.1 Test Scope (Android 앱 — Static 검증)

| Type | Target | 검증 방법 |
|------|--------|---------|
| 시각 검증 | 달력 6줄 고정 | 4월·5월 스와이프 에뮬레이터 확인 |
| 시각 검증 | 일기 배경색 통일 | 작성→보기 배경 동일 확인 |
| 기능 검증 | 색상 즉시 반영 | SettingsScreen 선택 후 HomeScreen 평일색 확인 |
| 기능 검증 | 재실행 후 유지 | 앱 종료 후 재실행 색상 유지 확인 |
| 기능 검증 | 초기화 버튼 | diaryBg=크림, weekday=진회색 복원 확인 |
| 용량 검증 | 이미지 300KB | 사진 첨부 후 Logcat 파일 크기 확인 |
| 회귀 검증 | v4 통합 테마 | 10종 테마 전환 정상 동작 확인 |

### 8.2 L1: API Test Scenarios

해당 없음 (서버 API 없음)

### 8.3 L2: UI Verification Scenarios

| # | 화면 | 동작 | 기대 결과 |
|---|------|------|---------|
| 1 | SettingsScreen | 일기 배경색 팔레트 중 "연분홍" 선택 | 해당 색상 체크마크, DiaryEditorScreen 배경 변경 |
| 2 | SettingsScreen | 평일 글씨색 팔레트 중 "네이비" 선택 | HomeScreen 평일 날짜 텍스트 네이비로 변경 |
| 3 | SettingsScreen | 초기화 버튼 클릭 | 크림+진회색으로 복원 |
| 4 | HomeScreen | 4월→5월 스와이프 | 달력 높이 동일 (6줄 유지) |
| 5 | DiaryEditorScreen | 일기 작성 화면 열기 | 배경색이 diaryBg 색상으로 표시 |
| 6 | DiaryDetailScreen | 일기 보기 화면 열기 | 배경색이 diaryBg 색상으로 동일하게 표시 |

### 8.4 L3: E2E Scenario

| # | 시나리오 | 단계 | 성공 기준 |
|---|---------|------|---------|
| 1 | 색상 선택 지속성 | 설정에서 색상 선택 → 앱 종료 → 재실행 | 선택 색상 유지 |
| 2 | 달력+일기 연동 | 달력 날짜 클릭 → 일기 작성 → 보기 | 작성/보기 배경 동일 |
| 3 | v4 회귀 확인 | 테마 선택 → 달력배경/오늘배경 변경 확인 | v4 기능 정상 |

---

## 9. Clean Architecture

### 9.1 Layer Assignment (Android MVVM)

| Component | Layer | 위치 |
|-----------|-------|------|
| `ThemeColors`, `DiaryBgPalette`, `WeekdayColorPalette` | Domain (상수/타입) | `ui/theme/` |
| `ThemePreferences` | Infrastructure (저장소) | `notification/` |
| `SettingsViewModel` | Application (상태 관리) | `viewmodel/` |
| `SettingsScreen`, `HomeScreen`, `DiaryEditorScreen`, `DiaryDetailScreen` | Presentation (UI) | `ui/` |
| `ImageCompressor` | Infrastructure (유틸) | `data/util/` |

### 9.2 Dependency Rules

```
SettingsScreen → SettingsViewModel → ThemePreferences → SharedPreferences
HomeScreen → LocalThemeColors (CompositionLocal)
DiaryEditorScreen → LocalThemeColors
DiaryDetailScreen → LocalThemeColors
MainActivity → SettingsViewModel + AppThemeTemplates → LocalThemeColors provide
```

---

## 10. Coding Convention Reference

### 10.1 v5 규칙

| 항목 | 규칙 |
|------|------|
| 팔레트 object 명 | `DiaryBgPalette`, `WeekdayColorPalette` (PascalCase) |
| SharedPreferences 키 | `"diary_bg_color"`, `"weekday_color"` (snake_case) |
| StateFlow 명 | `_diaryBgColor`, `_weekdayColor` (camelCase, private prefix `_`) |
| Design Ref 주석 | `// Design Ref: joyary-upgrade-v5 §{section} — {rationale}` |
| 기본값 Color | `Color(0xFFFFF8F0)` (0xFF 접두어 필수) |

---

## 11. Implementation Guide

### 11.1 File Structure (수정 대상 10개)

```
diary-app/app/src/main/java/com/example/diaryapp/
├── ui/theme/
│   ├── LocalThemeColors.kt     [수정] ThemeColors에 diaryBg, weekdayColor 추가
│   └── Color.kt                [수정] DiaryBgPalette, WeekdayColorPalette object 추가
├── notification/
│   └── ThemePreferences.kt     [수정] diaryBgColor, weekdayColor Int 추가
├── viewmodel/
│   └── SettingsViewModel.kt    [수정] StateFlow + setter 2개 추가
├── ui/settings/
│   └── SettingsScreen.kt       [수정] ColorPaletteRow 2개 추가
├── MainActivity.kt              [수정] diaryBg, weekdayColor collectAsState + copy()
├── ui/home/
│   └── HomeScreen.kt           [수정] CalendarGrid 42셀 패딩 + DayCell weekdayColor
├── ui/diary/
│   ├── DiaryEditorScreen.kt    [수정] containerColor: appBg → diaryBg
│   └── DiaryDetailScreen.kt    [수정] containerColor 추가: diaryBg
└── data/util/
    └── ImageCompressor.kt      [수정] maxSizeBytes: 1MB → 300KB
```

### 11.2 Implementation Order

> 의존성 순서: 타입 → 저장소 → ViewModel → MainActivity → UI 소비처

1. [ ] **`ui/theme/LocalThemeColors.kt`** — ThemeColors에 `diaryBg`, `weekdayColor` 필드 추가 (기본값 포함)
2. [ ] **`ui/theme/Color.kt`** — `DiaryBgPalette`, `WeekdayColorPalette` object 추가
3. [ ] **`notification/ThemePreferences.kt`** — `diaryBgColor`, `weekdayColor` Int 프로퍼티 추가
4. [ ] **`viewmodel/SettingsViewModel.kt`** — StateFlow 2개 + setter 2개 추가
5. [ ] **`MainActivity.kt`** — `diaryBg`, `weekdayColor` collect + `themeColors.copy()` 적용
6. [ ] **`ui/settings/SettingsScreen.kt`** — `ColorPaletteRow` 2개 추가 (일기배경, 평일글씨)
7. [ ] **`ui/home/HomeScreen.kt`** — CalendarGrid 42셀 패딩 + DayCell weekdayColor 적용
8. [ ] **`ui/diary/DiaryEditorScreen.kt`** — containerColor: appBg → diaryBg
9. [ ] **`ui/diary/DiaryDetailScreen.kt`** — Scaffold containerColor = diaryBg 추가
10. [ ] **`data/util/ImageCompressor.kt`** — `maxSizeBytes = 307_200L`

### 11.3 Session Guide

#### Module Map

| Module | Scope Key | 파일 범위 | 예상 작업량 |
|--------|-----------|---------|:----------:|
| 기반 레이어 | `module-1` | LocalThemeColors, Color.kt, ThemePreferences, SettingsViewModel | Small |
| 진입점 + 설정 UI | `module-2` | MainActivity, SettingsScreen | Small |
| UI 소비처 | `module-3` | HomeScreen, DiaryEditorScreen, DiaryDetailScreen, ImageCompressor | Small |

#### Recommended Session Plan

| Session | Phase | Scope | 비고 |
|---------|-------|-------|------|
| Session 1 | Plan + Design | 전체 | 완료 |
| Session 2 | Do | `--scope module-1,module-2,module-3` | 파일 수 적어 단일 세션 권장 |
| Session 3 | Check + Report | 전체 | Gap 분석 + 완료 보고 |

> v5는 수정 파일 10개, 신규 0개로 범위가 작아 **단일 Do 세션**으로 완료 가능.

---

## Key Technical Decisions

### KD-01: ThemeColors 선택적 파라미터

**결정**: `diaryBg`, `weekdayColor`를 기본값 있는 선택적 파라미터로 추가
**이유**: AppThemeTemplate.kt의 10개 인스턴스 수정 불필요 — 기존 코드 보호
**트레이드오프**: 기본값이 하드코딩되어 있으나 SharedPreferences override로 런타임 변경

### KD-02: MainActivity override 패턴

**결정**: `template.themeColors.copy(diaryBg = diaryBg, weekdayColor = weekday)` 방식
**이유**: template 고정 색상(calendarBg, appBg, todayBg)은 테마에서 결정, 개별 선택 색상은 Prefs에서 override
**트레이드오프**: MainActivity에 2개 StateFlow 추가되지만 단일 진입점 원칙 유지

### KD-03: CalendarGrid 42셀 패딩

**결정**: `while (size < 42) add(null)` — LazyVerticalGrid에 null 셀 42개 고정
**이유**: 6×7=42로 모든 달 동일 높이 보장
**트레이드오프**: 마지막 행이 모두 null인 달(예: 2월)은 빈 행 표시 — 허용

### KD-04: 이미지 압축 307,200 bytes

**결정**: `maxSizeBytes = 307_200L` (300 × 1024)
**이유**: 1MB 대비 70% 용량 절감
**트레이드오프**: 고해상도 사진에서 quality=10 도달 시 추가 압축 불가 — 원본 inSampleSize 선적용 권장

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-17 | Initial draft (Option C Pragmatic) | faith79@jobkorea.co.kr |
