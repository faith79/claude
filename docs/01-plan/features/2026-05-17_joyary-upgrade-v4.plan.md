# joyary-upgrade-v4 Plan

> **Summary**: v3 개별 색상 팔레트 제거 → Material3 전체를 바꾸는 파스텔 통합 테마 10종 + 앱 아이콘 교체
>
> **Project**: claude / diary-app
> **Version**: 0.4.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-17
> **Status**: Draft

---

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | v3의 달력·앱·오늘날짜 배경을 각각 선택하는 방식은 색상 조합이 어색해질 수 있고 UX가 파편화됨; 앱 아이콘도 일기장 모양으로 브랜드 정체성이 약함 |
| **Solution** | 디자이너가 큐레이션한 10가지 파스텔 통합 테마를 제공하고, 템플릿 하나를 고르면 Material3 colorScheme 전체 + 커스텀 달력색이 일괄 교체됨; 아이콘은 "조이(Joy)"를 상징하는 웃는 별로 교체 |
| **Function/UX Effect** | 설정 > 테마에서 원형 미리보기 카드 10개 표시 → 탭 한 번으로 앱 전체 색상 변경; 저장값은 템플릿 인덱스 1개뿐이라 단순; 앱 아이콘이 조이어리 브랜드와 일치 |
| **Core Value** | "기분에 맞게 앱 분위기를 바꿀 수 있는 조이어리" — 색상 테마 10종으로 개성 표현, 통합 테마라 색상 충돌 없음 |

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

### 1.1 Purpose

v3에서 구현한 달력·앱·오늘날짜 배경색 개별 선택 방식을 **통합 테마 템플릿** 방식으로 전환한다. 사용자는 디자이너가 정의한 10가지 파스텔 테마 중 하나를 선택하며, 선택 즉시 Material3 colorScheme 전체(primary, secondary, background, surface 등)와 달력 전용 커스텀 색상이 함께 교체된다.

### 1.2 Background

- v3 문제: 달력 배경(10색) × 앱 배경(10색) × 오늘날짜 배경(10색) = 1000가지 조합 가능 → 어색한 조합 발생 위험
- 해결: 전문적으로 조화된 10세트 팔레트로 제한 → 항상 아름다운 조합 보장
- 앱 아이콘 교체: 웃는 별(joy star) 컨셉으로 이미 구현 완료 (`ic_launcher_foreground.xml`, `ic_launcher_background.xml`)

### 1.3 Related Documents

- v3 Plan: `docs/01-plan/features/joyary-upgrade-v3.plan.md`
- v3 Report: `docs/04-report/features/joyary-upgrade-v3.report.md`

---

## 2. Scope

### 2.1 In Scope

- [x] 앱 아이콘 교체 (웃는 별 — **이미 구현 완료**)
- [ ] `AppThemeTemplate.kt` (신규): 10종 테마 정의 (ColorScheme + ThemeColors 세트)
- [ ] `Color.kt`: v3 CalendarBgPalette / AppBgPalette / TodayBgPalette 제거, 10종 테마 색상 상수 추가
- [ ] `ThemePreferences.kt`: 3개 색상 Int → 1개 templateIndex Int로 단순화
- [ ] `Theme.kt`: DiaryAppTheme 에 colorScheme 파라미터 추가
- [ ] `SettingsViewModel.kt`: 3개 StateFlow → 1개 `selectedTemplateIndex: StateFlow<Int>`
- [ ] `SettingsScreen.kt`: 3개 ColorPaletteRow 제거 → ThemeTemplateSelector (원형 카드 10개)
- [ ] `MainActivity.kt`: templateIndex로 colorScheme + ThemeColors 조회 → MaterialTheme + LocalThemeColors 동적 제공
- [ ] `LocalThemeColors.kt`: ThemeColors 유지 (calendarBg / appBg / todayBg)

### 2.2 Out of Scope

- 커스텀 색상 직접 입력 (HSV/RGB 슬라이더)
- 다크 모드 별도 테마 세트
- 테마 이름 직접 편집
- Firebase 테마 동기화
- 폰트/타이포그래피 변경

---

## 3. Requirements

### 3.1 Functional Requirements

| ID | 요구사항 | 우선순위 |
|----|---------|---------|
| FR-01 | 앱 아이콘을 조이어리 브랜드(웃는 별)로 교체 | Must — **완료** |
| FR-02 | 설정 화면 테마 섹션: 10개 파스텔 테마 원형 미리보기 카드 표시 | Must |
| FR-03 | 테마 선택 시 Material3 colorScheme 전체 즉시 교체 (primary/secondary/background/surface 등) | Must |
| FR-04 | 테마 선택 시 달력 배경(calendarBg) + 앱 배경(appBg) + 오늘날짜 배경(todayBg) 커스텀 색상도 교체 | Must |
| FR-05 | 템플릿 인덱스를 SharedPreferences에 저장, 재실행 후에도 유지 | Must |
| FR-06 | v3 개별 팔레트 3개(달력/앱/오늘날짜 ColorPaletteRow) 제거 | Must |
| FR-07 | 선택 중인 테마에 체크마크 또는 테두리 강조 표시 | Should |
| FR-08 | 기본값으로 초기화 버튼 — 0번 테마(Sky Blue)로 리셋 | Should |

### 3.2 Non-Functional Requirements

| 카테고리 | 기준 |
|---------|------|
| 색상 조화 | 10개 모든 테마가 WCAG AA 텍스트 대비비 준수 |
| 기존 회귀 없음 | 알림 설정, 로그아웃, 일기 CRUD 동작 유지 |
| 외부 라이브러리 추가 없음 | 기존 Material3, Compose 내에서 처리 |
| 즉시 반영 | 테마 선택 후 앱 재시작 없이 모든 화면에 적용 |

---

## 4. 10가지 파스텔 테마 정의

| # | 테마 이름 | primary | background | calendarBg | appBg | todayBg |
|---|---------|---------|-----------|-----------|-------|---------|
| 0 | 하늘 (Sky) | #5BBEE0 | #F0F8FF | #8EC6E6 | #F0F8FF | #7EC8E3 |
| 1 | 민트 (Mint) | #4CAF8B | #F0FAF6 | #80CBA9 | #F0FAF6 | #66BB99 |
| 2 | 라벤더 (Lavender) | #9C88C8 | #F5F0FF | #B4A0D8 | #F5F0FF | #A08AC0 |
| 3 | 피치 (Peach) | #E8956D | #FFF5EE | #F0A882 | #FFF5EE | #E89070 |
| 4 | 로즈 (Rose) | #D4779A | #FFF0F5 | #E099B4 | #FFF0F5 | #CC80A8 |
| 5 | 세이지 (Sage) | #78A882 | #F2F8F0 | #95C09A | #F2F8F0 | #80B087 |
| 6 | 버터 (Butter) | #D4B84A | #FFFDF0 | #E0C860 | #FFFDF0 | #CDB845 |
| 7 | 릴락 (Lilac) | #B088C8 | #F8F0FF | #C4A0D8 | #F8F0FF | #A878C0 |
| 8 | 코랄 (Coral) | #E87870 | #FFF2F0 | #F09090 | #FFF2F0 | #E07878 |
| 9 | 모카 (Mocha) | #B09070 | #FBF8F5 | #C8A888 | #FBF8F5 | #A89070 |

> 각 테마는 `lightColorScheme()` 으로 Material3 전체 colorScheme을 생성하고, ThemeColors로 커스텀 3색을 정의

---

## 5. 파일 영향 범위

| 파일 | 변경 유형 | 주요 내용 |
|------|---------|---------|
| `drawable/ic_launcher_foreground.xml` | 수정 — **완료** | 웃는 별 아이콘 |
| `drawable/ic_launcher_background.xml` | 신규 — **완료** | 파스텔 그라데이션 배경 |
| `values/colors.xml` | 수정 — **완료** | 아이콘 배경색 #7EC8E3 |
| `ui/theme/Color.kt` | 수정 | v3 Palette 3개 제거, 테마 상수 정리 |
| `ui/theme/AppThemeTemplate.kt` | **신규** | 10종 테마 정의 (colorScheme + ThemeColors) |
| `ui/theme/Theme.kt` | 수정 | DiaryAppTheme(colorScheme: ColorScheme) 파라미터화 |
| `notification/ThemePreferences.kt` | 수정 | 3개 Int → 1개 templateIndex Int |
| `viewmodel/SettingsViewModel.kt` | 수정 | 3개 StateFlow → selectedTemplateIndex StateFlow |
| `ui/settings/SettingsScreen.kt` | 수정 | 3개 ColorPaletteRow 제거, ThemeTemplateSelector 추가 |
| `MainActivity.kt` | 수정 | templateIndex로 colorScheme + ThemeColors 동적 주입 |

**신규 1개 / 수정 6개 (아이콘 제외) / 총 신규+수정 9개 파일**

---

## 6. Technical Design

### 6.1 핵심 데이터 구조

```kotlin
// ui/theme/AppThemeTemplate.kt
data class AppThemeTemplate(
    val index: Int,
    val nameKo: String,
    val colorScheme: ColorScheme,       // Material3 전체
    val themeColors: ThemeColors        // calendarBg / appBg / todayBg
)

val AppThemeTemplates: List<AppThemeTemplate> = listOf(
    AppThemeTemplate(0, "하늘", skyColorScheme, ThemeColors(calendarBg=..., appBg=..., todayBg=...)),
    // ... 10개
)
```

### 6.2 상태 흐름

```
SharedPreferences (templateIndex: Int)
    ↓
SettingsViewModel.selectedTemplateIndex: StateFlow<Int>
    ↓
MainActivity: AppThemeTemplates[index]
    ↓
DiaryAppTheme(colorScheme = template.colorScheme) {
    CompositionLocalProvider(LocalThemeColors provides template.themeColors) {
        NavGraph(...)
    }
}
```

### 6.3 ThemePreferences 마이그레이션

```kotlin
// 기존 v3 키 3개 제거, 신규 키 1개 추가
class ThemePreferences(context: Context) {
    var selectedTemplateIndex: Int
        get() = prefs.getInt("selected_theme_index", 0)  // default: 0번 하늘
        set(value) { prefs.edit().putInt("selected_theme_index", value).apply() }
    fun resetToDefault() { selectedTemplateIndex = 0 }
}
```

---

## 7. Success Criteria

| # | 기준 | 검증 방법 |
|---|------|---------|
| SC-01 | 앱 아이콘이 웃는 별로 표시 | 에뮬레이터 홈 화면 확인 — **완료** |
| SC-02 | 설정 > 테마에서 10개 원형 테마 카드 표시 | UI 확인 |
| SC-03 | 테마 선택 시 TopAppBar, 버튼, 배경 등 Material3 색상 전체 변경 | 에뮬레이터 확인 |
| SC-04 | 달력 배경 + 오늘날짜 배경도 템플릿에 맞게 변경 | 홈 화면 달력 확인 |
| SC-05 | 앱 재실행 후에도 선택 테마 유지 | 재실행 확인 |
| SC-06 | 초기화 버튼 → 하늘 테마(0번)로 복원 | UI 동작 확인 |
| SC-07 | 알림 설정, 로그아웃, 일기 CRUD 회귀 없음 | 기능 테스트 |

---

## 8. Risks & Mitigation

| Risk | 심각도 | 대응 |
|------|--------|------|
| lightColorScheme() 미적용 색상 누락 | Medium | onPrimary/onSecondary/outline 등 전체 파라미터 명시 |
| v3 ThemePreferences 기존 키 충돌 | Low | 신규 키 `selected_theme_index` 사용, 기존 키는 무시 |
| 테마 색상 WCAG 대비 미달 | Medium | 10개 모든 테마 primary-on 쌍 대비비 4.5:1 이상 사전 확인 |
| DiaryAppTheme 파라미터화 시 기존 호출부 수정 | Low | MainActivity 1곳만 호출 — 영향 최소 |

---

## 9. Next Steps

1. [ ] `/pdca design joyary-upgrade-v4`
2. [ ] Architecture 선택 (Option C Pragmatic 권장)
3. [ ] 구현 (`/pdca do joyary-upgrade-v4`)
4. [ ] Gap Analysis (`/pdca analyze joyary-upgrade-v4`)

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-17 | Initial draft | faith79@jobkorea.co.kr |
