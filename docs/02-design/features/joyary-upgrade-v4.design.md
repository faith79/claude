# joyary-upgrade-v4 Design Document

> **Summary**: v3 개별 팔레트 → 10종 파스텔 통합 테마 (Material3 colorScheme 전체 교체) + 웃는 별 아이콘
>
> **Project**: claude / diary-app
> **Version**: 0.4.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-17
> **Status**: Draft
> **Planning Doc**: [joyary-upgrade-v4.plan.md](../01-plan/features/joyary-upgrade-v4.plan.md)

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | v3 개별 팔레트가 색상 조합 불일치 유발 + 앱 아이콘이 브랜드와 안 맞음 |
| **WHO** | 조이어리 기존 사용자 (색상 취향 표현 원하는 사용자, 앱 분위기를 기분에 따라 바꾸고 싶은 사용자) |
| **RISK** | Material3 colorScheme 동적 주입 시 DiaryAppTheme 파라미터화 필요, v3 ThemePreferences(3개 Int) → v4(1개 Int) 마이그레이션 |
| **SUCCESS** | 10종 테마 선택 → 즉시 앱 전체 색상 반영 + 재실행 후 유지 + 아이콘 교체 완료 |
| **SCOPE** | UI 레이어 + 설정 레이어 + 테마 시스템 재설계 (데이터/인증 무변경); 아이콘은 이미 구현 완료 |

---

## 1. Overview

### 1.1 Design Goals

- `AppThemeTemplate.kt` 1개 신규 파일로 10종 파스텔 테마 색상 세트 정의
- `DiaryAppTheme()` 함수에 `colorScheme` 파라미터 추가하여 동적 색상 주입
- `ThemePreferences`에 `selectedTemplateIndex` 키 추가 (기존 3개 키 유지 — Option A 방식)
- `SettingsViewModel`에 `selectedTemplateIndex` StateFlow 추가 (기존 3개 StateFlow 유지 — 미사용 dead code 허용)
- `SettingsScreen`의 ColorPaletteRow 3개 제거 → `ThemeTemplateSelector` 컴포넌트로 대체

### 1.2 Design Principles

- **Option A — Minimal**: v3 코드 위에 v4 기능 추가, 기존 키/StateFlow 그대로 유지
- **단일 진입점**: MainActivity가 templateIndex로 colorScheme + ThemeColors를 한 번에 결정
- **CompositionLocal 재사용**: LocalThemeColors 패턴 그대로 유지

---

## 2. Architecture

### 2.0 Architecture Selection

**Selected**: Option A — Minimal
**Rationale**: v3 코드 호환성 유지, 최소 변경으로 빠른 구현. ThemePreferences의 기존 3개 Int 키는 unused로 남음.

| Criteria | Option A (Selected) |
|----------|:-------------------:|
| 신규 파일 | 1개 |
| 수정 파일 | 5개 |
| v3 Dead code | 있음 (CalendarBgPalette 등 미삭제) |
| 위험도 | 낮음 |

### 2.1 Component Diagram

```
SharedPreferences (selectedTemplateIndex: Int)
        ↓
ThemePreferences.selectedTemplateIndex (get/set)
        ↓
SettingsViewModel.selectedTemplateIndex: StateFlow<Int>
        ↓ collectAsStateWithLifecycle
MainActivity
        ↓ AppThemeTemplates[index]
        ├── template.colorScheme
        │       ↓
        │   DiaryAppTheme(colorScheme = template.colorScheme)
        │       ↓
        │   MaterialTheme (TopAppBar, Button, Card 등 전체 반영)
        │
        └── template.themeColors
                ↓
            CompositionLocalProvider(LocalThemeColors provides template.themeColors)
                ↓
            HomeScreen.calendarBg / DayCell.todayBg / DiaryEditorScreen.appBg
```

### 2.2 Data Flow

```
사용자 탭 → ThemeTemplateSelector.onSelect(index)
    → SettingsViewModel.selectTemplate(index)
    → ThemePreferences.selectedTemplateIndex = index
    → StateFlow emit → MainActivity recompose
    → DiaryAppTheme + LocalThemeColors 갱신
    → 모든 화면 즉시 반영
```

### 2.3 Dependencies

| Component | Depends On | Purpose |
|-----------|-----------|---------|
| `AppThemeTemplate` | Material3 `lightColorScheme`, `ThemeColors` | 테마 데이터 정의 |
| `SettingsViewModel` | `ThemePreferences` | templateIndex 저장/로드 |
| `MainActivity` | `SettingsViewModel`, `AppThemeTemplates` | colorScheme 동적 제공 |

---

## 3. Data Model

### 3.1 AppThemeTemplate 구조

```kotlin
// ui/theme/AppThemeTemplate.kt
data class AppThemeTemplate(
    val index: Int,
    val nameKo: String,
    val previewColor: Color,      // 원형 카드에 보여줄 대표색 (primary)
    val colorScheme: ColorScheme, // Material3 전체 색상
    val themeColors: ThemeColors  // calendarBg / appBg / todayBg
)

val AppThemeTemplates: List<AppThemeTemplate> = listOf(
    AppThemeTemplate(0, "하늘",    skyPreview,    skyColorScheme,    skyThemeColors),
    AppThemeTemplate(1, "민트",    mintPreview,   mintColorScheme,   mintThemeColors),
    AppThemeTemplate(2, "라벤더",  lavPreview,    lavColorScheme,    lavThemeColors),
    AppThemeTemplate(3, "피치",    peachPreview,  peachColorScheme,  peachThemeColors),
    AppThemeTemplate(4, "로즈",    rosePreview,   roseColorScheme,   roseThemeColors),
    AppThemeTemplate(5, "세이지",  sagePreview,   sageColorScheme,   sageThemeColors),
    AppThemeTemplate(6, "버터",    butterPreview, butterColorScheme, butterThemeColors),
    AppThemeTemplate(7, "릴락",    lilacPreview,  lilacColorScheme,  lilacThemeColors),
    AppThemeTemplate(8, "코랄",    coralPreview,  coralColorScheme,  coralThemeColors),
    AppThemeTemplate(9, "모카",    mochaPreview,  mochaColorScheme,  mochaThemeColors),
)
```

### 3.2 10종 파스텔 테마 색상 세트

| # | 이름 | primary | onPrimary | background | surface | calendarBg | appBg | todayBg |
|---|-----|---------|----------|-----------|---------|-----------|-------|---------|
| 0 | 하늘 | #5BBEE0 | #FFFFFF | #F0F8FF | #FFFFFF | #8EC6E6 | #F0F8FF | #7EC8E3 |
| 1 | 민트 | #4CAF8B | #FFFFFF | #F0FAF6 | #FFFFFF | #80CBA9 | #F0FAF6 | #66BB99 |
| 2 | 라벤더 | #9C88C8 | #FFFFFF | #F5F0FF | #FFFFFF | #B4A0D8 | #F5F0FF | #A08AC0 |
| 3 | 피치 | #E8956D | #FFFFFF | #FFF5EE | #FFFFFF | #F0A882 | #FFF5EE | #E89070 |
| 4 | 로즈 | #D4779A | #FFFFFF | #FFF0F5 | #FFFFFF | #E099B4 | #FFF0F5 | #CC80A8 |
| 5 | 세이지 | #78A882 | #FFFFFF | #F2F8F0 | #FFFFFF | #95C09A | #F2F8F0 | #80B087 |
| 6 | 버터 | #D4B84A | #FFFFFF | #FFFDF0 | #FFFFFF | #E0C860 | #FFFDF0 | #CDB845 |
| 7 | 릴락 | #B088C8 | #FFFFFF | #F8F0FF | #FFFFFF | #C4A0D8 | #F8F0FF | #A878C0 |
| 8 | 코랄 | #E87870 | #FFFFFF | #FFF2F0 | #FFFFFF | #F09090 | #FFF2F0 | #E07878 |
| 9 | 모카 | #B09070 | #FFFFFF | #FBF8F5 | #FFFFFF | #C8A888 | #FBF8F5 | #A89070 |

> 각 테마의 secondary = primary 20% 어두운 색, secondaryContainer / surfaceVariant = background 계열 유지

---

## 4. API Specification

해당 없음 (로컬 UI 전용 기능)

---

## 5. UI/UX Design

### 5.1 설정 화면 테마 섹션 레이아웃

```
┌─────────────────────────────────────────────┐
│ 테마                            [primary색] │  ← titleMedium
├─────────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐    │
│  │  ○  ○  ○  ○  ○                     │    │  ← LazyRow, 원형 카드
│  │ 하늘 민트 라벤더 피치 로즈           │    │     size=44dp
│  │  ○  ○  ○  ○  ○                     │    │     선택 시 3dp 테두리
│  │ 세이지 버터 릴락 코랄 모카           │    │
│  │                                     │    │
│  │              [기본값으로 초기화]     │    │  ← TextButton, Alignment.End
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

### 5.2 User Flow

```
설정 화면 진입 → 테마 섹션 확인 → 원형 카드 탭
    → ViewModel.selectTemplate(index)
    → SharedPreferences 저장
    → StateFlow emit → MainActivity recompose
    → 뒤로 가기 → 홈 화면에 즉시 새 색상 반영
```

### 5.3 Component List

| 컴포넌트 | 위치 | 역할 |
|---------|------|------|
| `ThemeTemplateSelector` | `SettingsScreen.kt` private | 10개 원형 카드 + 초기화 버튼 |
| `ThemeCircleCard` | `SettingsScreen.kt` private | 개별 원형 색상 카드 (44dp) |
| `AppThemeTemplate` | `AppThemeTemplate.kt` | 테마 데이터 클래스 + 10개 인스턴스 |

### 5.4 Page UI Checklist

#### 설정 화면 (SettingsScreen)

- [ ] 섹션 타이틀: "테마" (titleMedium, primary 색상)
- [ ] 테마 카드: 10개 원형 (각 44dp, 테마 primary 색상으로 채워짐)
- [ ] 테마 카드 선택 표시: 선택된 카드에 3dp primary 색 테두리
- [ ] 테마 카드 레이블: 카드 아래 한국어 이름 (labelSmall)
- [ ] 초기화 버튼: "기본값으로 초기화" TextButton (Alignment.End)
- [ ] 기존 ColorPaletteRow 3개 제거됨 (달력 배경색 / 앱 배경색 / 오늘 날짜 배경색)

#### 홈 화면 (HomeScreen) — 테마 적용 확인

- [ ] 달력 Card 배경: `LocalThemeColors.current.calendarBg` (템플릿별로 다름)
- [ ] 오늘 날짜 배경: `LocalThemeColors.current.todayBg` (템플릿별로 다름)
- [ ] TopAppBar 색상: `MaterialTheme.colorScheme.primary` (템플릿별로 다름)

#### 편집 화면 (DiaryEditorScreen) — 테마 적용 확인

- [ ] Scaffold containerColor: `LocalThemeColors.current.appBg` (템플릿별로 다름)

---

## 6. Error Handling

| 상황 | 처리 |
|------|------|
| templateIndex 범위 초과 (0~9 외) | `AppThemeTemplates.getOrElse(index) { AppThemeTemplates[0] }` 로 폴백 |
| SharedPreferences 읽기 실패 | default = 0 (하늘 테마) |

---

## 7. Security Considerations

해당 없음 (로컬 설정 저장, 외부 통신 없음)

---

## 8. Test Plan

### 8.1 Test Scope

| Type | Target | 검증 방법 |
|------|--------|---------|
| L2: UI Action | 테마 카드 탭 → 전체 색상 변경 | 에뮬레이터 시각적 확인 |
| L2: UI Action | 초기화 버튼 → 0번 테마 복원 | 에뮬레이터 확인 |
| L3: E2E | 앱 재실행 후 선택 테마 유지 | 에뮬레이터 재실행 확인 |

### 8.2 L2 UI Action Test Scenarios

| # | 화면 | 액션 | 기대 결과 |
|---|------|------|---------|
| 1 | 설정 | 앱 진입 | 10개 원형 테마 카드 표시, 현재 선택 테마 테두리 강조 |
| 2 | 설정 | 민트 테마 탭 | 카드 테두리 이동, TopAppBar/배경색 즉시 변경 |
| 3 | 홈 | 설정에서 뒤로 | 달력 배경색, 오늘 날짜 배경색 새 테마로 표시 |
| 4 | 설정 | 초기화 버튼 탭 | 하늘 테마(0번)로 복원, 모든 색상 초기화 |

### 8.3 L3 E2E Scenario

| # | 시나리오 | 단계 | 성공 기준 |
|---|---------|------|---------|
| 1 | 테마 지속 | 라벤더 선택 → 앱 종료 → 재실행 | 라벤더 테마 유지 |
| 2 | 회귀 없음 | 민트 선택 → 일기 작성 → 알림 설정 | 기존 기능 정상 동작 |

---

## 9. Clean Architecture (Android MVVM)

### 9.1 Layer Assignment

| 컴포넌트 | 레이어 | 위치 |
|---------|-------|------|
| `ThemeTemplateSelector`, `ThemeCircleCard` | Presentation | `SettingsScreen.kt` |
| `SettingsViewModel.selectTemplate()` | Application | `SettingsViewModel.kt` |
| `AppThemeTemplate`, `ThemeColors` | Domain | `ui/theme/AppThemeTemplate.kt` |
| `ThemePreferences.selectedTemplateIndex` | Infrastructure | `notification/ThemePreferences.kt` |

---

## 10. Coding Conventions

| 항목 | 컨벤션 |
|------|-------|
| 데이터 클래스 | PascalCase (`AppThemeTemplate`) |
| 상수 리스트 | camelCase (`AppThemeTemplates`) |
| Composable | PascalCase (`ThemeTemplateSelector`, `ThemeCircleCard`) |
| StateFlow | `selectedTemplateIndex` (camelCase) |

---

## 11. Implementation Guide

### 11.1 File Structure

```
diary-app/app/src/main/java/com/example/diaryapp/
├── ui/theme/
│   ├── AppThemeTemplate.kt    ← 신규: 10종 테마 정의
│   ├── Color.kt               ← 미변경 (v3 Palette 상수 유지)
│   ├── LocalThemeColors.kt    ← 미변경
│   └── Theme.kt               ← 수정: colorScheme 파라미터 추가
├── notification/
│   └── ThemePreferences.kt    ← 수정: selectedTemplateIndex 추가
├── viewmodel/
│   └── SettingsViewModel.kt   ← 수정: selectedTemplateIndex StateFlow 추가
├── ui/settings/
│   └── SettingsScreen.kt      ← 수정: ColorPaletteRow 3개 제거, ThemeTemplateSelector 추가
└── MainActivity.kt             ← 수정: templateIndex로 colorScheme + ThemeColors 동적 제공
```

### 11.2 Implementation Order

1. [ ] `AppThemeTemplate.kt` 신규 — 10종 테마 데이터 정의
2. [ ] `Theme.kt` 수정 — `DiaryAppTheme(colorScheme: ColorScheme)` 파라미터화
3. [ ] `ThemePreferences.kt` 수정 — `selectedTemplateIndex` get/set 추가
4. [ ] `SettingsViewModel.kt` 수정 — `selectedTemplateIndex` StateFlow + `selectTemplate()` + `resetTemplate()`
5. [ ] `SettingsScreen.kt` 수정 — ColorPaletteRow 3개 제거, ThemeTemplateSelector 추가
6. [ ] `MainActivity.kt` 수정 — template 조회 → DiaryAppTheme + LocalThemeColors 동적 제공

### 11.3 Session Guide

#### Module Map

| Module | Scope Key | 설명 | 예상 turns |
|--------|-----------|------|:----------:|
| 테마 데이터 + Theme 파라미터화 | `module-1` | AppThemeTemplate.kt 신규, Theme.kt 수정 | 2-3 |
| 저장/ViewModel | `module-2` | ThemePreferences + SettingsViewModel 수정 | 2-3 |
| 설정 UI | `module-3` | SettingsScreen ColorPaletteRow 제거 + ThemeTemplateSelector | 3-4 |
| MainActivity 연결 | `module-4` | templateIndex → colorScheme 동적 주입 | 2-3 |

#### Recommended Session Plan

| Session | Phase | Scope | Turns |
|---------|-------|-------|:-----:|
| Session 1 | Plan + Design | 전체 | 완료 |
| Session 2 | Do | `--scope module-1,module-2` | 25-30 |
| Session 3 | Do | `--scope module-3,module-4` | 25-30 |
| Session 4 | Check + Report | 전체 | 20-25 |

---

## 12. 핵심 구현 상세

### 12.1 AppThemeTemplate.kt 전체 구조

```kotlin
package com.example.diaryapp.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class AppThemeTemplate(
    val index: Int,
    val nameKo: String,
    val previewColor: Color,
    val colorScheme: androidx.compose.material3.ColorScheme,
    val themeColors: ThemeColors
)

// 하늘 (0)
private val SkyTemplate = AppThemeTemplate(
    index = 0, nameKo = "하늘",
    previewColor = Color(0xFF5BBEE0),
    colorScheme = lightColorScheme(
        primary = Color(0xFF5BBEE0), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB3E5FC), onPrimaryContainer = Color(0xFF1565C0),
        secondary = Color(0xFF4AABCC), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE1F5FE), onSecondaryContainer = Color(0xFF01579B),
        background = Color(0xFFF0F8FF), onBackground = Color(0xFF1A2A3A),
        surface = Color(0xFFFFFFFF), onSurface = Color(0xFF1A2A3A),
        surfaceVariant = Color(0xFFE1F5FE), onSurfaceVariant = Color(0xFF4A6072),
        outline = Color(0xFF90CAD8), error = Color(0xFFE57373), onError = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF8EC6E6), appBg = Color(0xFFF0F8FF), todayBg = Color(0xFF7EC8E3)
    )
)

// 민트 (1)
private val MintTemplate = AppThemeTemplate(
    index = 1, nameKo = "민트",
    previewColor = Color(0xFF4CAF8B),
    colorScheme = lightColorScheme(
        primary = Color(0xFF4CAF8B), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB2DFDB), onPrimaryContainer = Color(0xFF1B5E20),
        secondary = Color(0xFF3A9B78), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8F5E9), onSecondaryContainer = Color(0xFF1B5E20),
        background = Color(0xFFF0FAF6), onBackground = Color(0xFF1A2E24),
        surface = Color(0xFFFFFFFF), onSurface = Color(0xFF1A2E24),
        surfaceVariant = Color(0xFFE8F5E9), onSurfaceVariant = Color(0xFF4A6058),
        outline = Color(0xFF90C8A8), error = Color(0xFFE57373), onError = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF80CBA9), appBg = Color(0xFFF0FAF6), todayBg = Color(0xFF66BB99)
    )
)

// 라벤더 (2)
private val LavenderTemplate = AppThemeTemplate(
    index = 2, nameKo = "라벤더",
    previewColor = Color(0xFF9C88C8),
    colorScheme = lightColorScheme(
        primary = Color(0xFF9C88C8), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE1D5FF), onPrimaryContainer = Color(0xFF4A148C),
        secondary = Color(0xFF8878B4), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF3E5F5), onSecondaryContainer = Color(0xFF4A148C),
        background = Color(0xFFF5F0FF), onBackground = Color(0xFF1E1A2E),
        surface = Color(0xFFFFFFFF), onSurface = Color(0xFF1E1A2E),
        surfaceVariant = Color(0xFFEDE7FF), onSurfaceVariant = Color(0xFF5A4A72),
        outline = Color(0xFFB0A0D8), error = Color(0xFFE57373), onError = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFB4A0D8), appBg = Color(0xFFF5F0FF), todayBg = Color(0xFFA08AC0)
    )
)

// 피치 (3)
private val PeachTemplate = AppThemeTemplate(
    index = 3, nameKo = "피치",
    previewColor = Color(0xFFE8956D),
    colorScheme = lightColorScheme(
        primary = Color(0xFFE8956D), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFCCBC), onPrimaryContainer = Color(0xFFBF360C),
        secondary = Color(0xFFD4815A), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFF3E0), onSecondaryContainer = Color(0xFFBF360C),
        background = Color(0xFFFFF5EE), onBackground = Color(0xFF2E1A0A),
        surface = Color(0xFFFFFFFF), onSurface = Color(0xFF2E1A0A),
        surfaceVariant = Color(0xFFFFECE0), onSurfaceVariant = Color(0xFF72503A),
        outline = Color(0xFFD8A090), error = Color(0xFFE57373), onError = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFF0A882), appBg = Color(0xFFFFF5EE), todayBg = Color(0xFFE89070)
    )
)

// 로즈 (4)
private val RoseTemplate = AppThemeTemplate(
    index = 4, nameKo = "로즈",
    previewColor = Color(0xFFD4779A),
    colorScheme = lightColorScheme(
        primary = Color(0xFFD4779A), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFCDD2), onPrimaryContainer = Color(0xFF880E4F),
        secondary = Color(0xFFC06488), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFCE4EC), onSecondaryContainer = Color(0xFF880E4F),
        background = Color(0xFFFFF0F5), onBackground = Color(0xFF2E0A1A),
        surface = Color(0xFFFFFFFF), onSurface = Color(0xFF2E0A1A),
        surfaceVariant = Color(0xFFFFE0EC), onSurfaceVariant = Color(0xFF724050),
        outline = Color(0xFFD890A8), error = Color(0xFFE57373), onError = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFE099B4), appBg = Color(0xFFFFF0F5), todayBg = Color(0xFFCC80A8)
    )
)

// 세이지 (5)
private val SageTemplate = AppThemeTemplate(
    index = 5, nameKo = "세이지",
    previewColor = Color(0xFF78A882),
    colorScheme = lightColorScheme(
        primary = Color(0xFF78A882), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC8E6C9), onPrimaryContainer = Color(0xFF1B5E20),
        secondary = Color(0xFF66946E), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8F5E9), onSecondaryContainer = Color(0xFF1B5E20),
        background = Color(0xFFF2F8F0), onBackground = Color(0xFF1A2E1C),
        surface = Color(0xFFFFFFFF), onSurface = Color(0xFF1A2E1C),
        surfaceVariant = Color(0xFFE8F5E9), onSurfaceVariant = Color(0xFF4A6050),
        outline = Color(0xFF98C0A0), error = Color(0xFFE57373), onError = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF95C09A), appBg = Color(0xFFF2F8F0), todayBg = Color(0xFF80B087)
    )
)

// 버터 (6)
private val ButterTemplate = AppThemeTemplate(
    index = 6, nameKo = "버터",
    previewColor = Color(0xFFD4B84A),
    colorScheme = lightColorScheme(
        primary = Color(0xFFD4B84A), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFF9C4), onPrimaryContainer = Color(0xFF827717),
        secondary = Color(0xFFBCA438), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFFDE7), onSecondaryContainer = Color(0xFF827717),
        background = Color(0xFFFFFDF0), onBackground = Color(0xFF2A2410),
        surface = Color(0xFFFFFFFF), onSurface = Color(0xFF2A2410),
        surfaceVariant = Color(0xFFFFFAD0), onSurfaceVariant = Color(0xFF6A5C20),
        outline = Color(0xFFD8C870), error = Color(0xFFE57373), onError = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFE0C860), appBg = Color(0xFFFFFDF0), todayBg = Color(0xFFCDB845)
    )
)

// 릴락 (7)
private val LilacTemplate = AppThemeTemplate(
    index = 7, nameKo = "릴락",
    previewColor = Color(0xFFB088C8),
    colorScheme = lightColorScheme(
        primary = Color(0xFFB088C8), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE8D5FF), onPrimaryContainer = Color(0xFF6A1B9A),
        secondary = Color(0xFF9C74B4), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF3E5F5), onSecondaryContainer = Color(0xFF6A1B9A),
        background = Color(0xFFF8F0FF), onBackground = Color(0xFF201828),
        surface = Color(0xFFFFFFFF), onSurface = Color(0xFF201828),
        surfaceVariant = Color(0xFFEFE0FF), onSurfaceVariant = Color(0xFF5E4878),
        outline = Color(0xFFBCA8D8), error = Color(0xFFE57373), onError = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFC4A0D8), appBg = Color(0xFFF8F0FF), todayBg = Color(0xFFA878C0)
    )
)

// 코랄 (8)
private val CoralTemplate = AppThemeTemplate(
    index = 8, nameKo = "코랄",
    previewColor = Color(0xFFE87870),
    colorScheme = lightColorScheme(
        primary = Color(0xFFE87870), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFCDD2), onPrimaryContainer = Color(0xFFB71C1C),
        secondary = Color(0xFFD46460), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFEBEE), onSecondaryContainer = Color(0xFFB71C1C),
        background = Color(0xFFFFF2F0), onBackground = Color(0xFF2E0E0A),
        surface = Color(0xFFFFFFFF), onSurface = Color(0xFF2E0E0A),
        surfaceVariant = Color(0xFFFFE0E0), onSurfaceVariant = Color(0xFF724040),
        outline = Color(0xFFD89090), error = Color(0xFFE57373), onError = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFF09090), appBg = Color(0xFFFFF2F0), todayBg = Color(0xFFE07878)
    )
)

// 모카 (9)
private val MochaTemplate = AppThemeTemplate(
    index = 9, nameKo = "모카",
    previewColor = Color(0xFFB09070),
    colorScheme = lightColorScheme(
        primary = Color(0xFFB09070), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE8D8C8), onPrimaryContainer = Color(0xFF5D4037),
        secondary = Color(0xFF9C7C5C), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF5EDE0), onSecondaryContainer = Color(0xFF5D4037),
        background = Color(0xFFFBF8F5), onBackground = Color(0xFF261C10),
        surface = Color(0xFFFFFFFF), onSurface = Color(0xFF261C10),
        surfaceVariant = Color(0xFFF0E8D8), onSurfaceVariant = Color(0xFF6A5440),
        outline = Color(0xFFC8A888), error = Color(0xFFE57373), onError = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFC8A888), appBg = Color(0xFFFBF8F5), todayBg = Color(0xFFA89070)
    )
)

val AppThemeTemplates: List<AppThemeTemplate> = listOf(
    SkyTemplate, MintTemplate, LavenderTemplate, PeachTemplate, RoseTemplate,
    SageTemplate, ButterTemplate, LilacTemplate, CoralTemplate, MochaTemplate
)
```

### 12.2 Theme.kt 수정 핵심

```kotlin
// Before
fun DiaryAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit)

// After
fun DiaryAppTheme(
    colorScheme: ColorScheme = SkyLightColorScheme,   // 동적 주입 파라미터
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
)
```

### 12.3 ThemePreferences.kt 수정 핵심

```kotlin
// 기존 3개 메서드 유지 + 신규 추가
var selectedTemplateIndex: Int
    get() = prefs.getInt("selected_theme_index", 0)
    set(value) { prefs.edit().putInt("selected_theme_index", value).apply() }

fun resetToDefault() { selectedTemplateIndex = 0 }
```

### 12.4 SettingsViewModel.kt 수정 핵심

```kotlin
// 기존 3개 StateFlow 유지 + 신규 추가
private val _selectedTemplateIndex = MutableStateFlow(themePreferences.selectedTemplateIndex)
val selectedTemplateIndex: StateFlow<Int> = _selectedTemplateIndex.asStateFlow()

fun selectTemplate(index: Int) {
    themePreferences.selectedTemplateIndex = index
    _selectedTemplateIndex.value = index
}

fun resetThemeTemplate() {
    themePreferences.resetToDefault()
    _selectedTemplateIndex.value = 0
}
```

### 12.5 MainActivity.kt 수정 핵심

```kotlin
setContent {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val templateIndex by settingsViewModel.selectedTemplateIndex.collectAsStateWithLifecycle()
    val template = AppThemeTemplates.getOrElse(templateIndex) { AppThemeTemplates[0] }

    DiaryAppTheme(colorScheme = template.colorScheme) {
        CompositionLocalProvider(LocalThemeColors provides template.themeColors) {
            // NavGraph ...
        }
    }
}
```

### 12.6 SettingsScreen.kt ThemeTemplateSelector 핵심

```kotlin
@Composable
private fun ThemeTemplateSelector(
    selectedIndex: Int,
    onTemplateSelected: (Int) -> Unit,
    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(AppThemeTemplates) { template ->
                ThemeCircleCard(
                    template = template,
                    isSelected = template.index == selectedIndex,
                    onClick = { onTemplateSelected(template.index) }
                )
            }
        }
        TextButton(onClick = onReset, modifier = Modifier.align(Alignment.End)) {
            Text("기본값으로 초기화")
        }
    }
}

@Composable
private fun ThemeCircleCard(template: AppThemeTemplate, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(template.previewColor)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .clickable { onClick() }
        )
        Spacer(Modifier.height(4.dp))
        Text(template.nameKo, style = MaterialTheme.typography.labelSmall)
    }
}
```

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-17 | Initial draft (Option A — Minimal) | faith79@jobkorea.co.kr |
