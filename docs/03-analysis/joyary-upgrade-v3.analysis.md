# joyary-upgrade-v3 Gap Analysis

> **Feature**: joyary-upgrade-v3
> **Date**: 2026-05-17
> **Phase**: Check
> **Match Rate**: 100%

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 달력 가시성 문제 즉시 해결 + 사용자 취향 반영 가능한 테마 설정 |
| **WHO** | 조이어리 앱 기존 사용자 (기본 색상이 안 보인다는 불편 경험자) |
| **RISK** | CompositionLocal provide 누락, SharedPreferences Int↔Color 변환, 팔레트 색상 대비 |
| **SUCCESS** | FR-01~FR-08 구현 완료 + 색상 변경 후 즉시 홈/편집 화면 반영 확인 |
| **SCOPE** | UI 레이어 + 설정 레이어 (데이터/인증 무변경), 신규 2개 + 수정 7개 파일 |

---

## Gap Analysis Results

### Structural Match: 100%

| 파일 | 상태 | 비고 |
|------|------|------|
| `ui/theme/Color.kt` | ✅ | SkyCalendarBg #8EC6E6 + CalendarBgPalette/AppBgPalette/TodayBgPalette 각 10개 |
| `notification/ThemePreferences.kt` | ✅ | SharedPreferences 3개 색상 저장소 (신규) |
| `ui/theme/LocalThemeColors.kt` | ✅ | ThemeColors data class + compositionLocalOf (신규) |
| `di/NotificationModule.kt` | ✅ | provideThemePreferences @Singleton 추가 |
| `viewmodel/SettingsViewModel.kt` | ✅ | ThemePreferences inject + 3개 StateFlow + setter × 3 + reset |
| `ui/settings/SettingsScreen.kt` | ✅ | 테마 섹션 + ColorPaletteRow 컴포넌트 |
| `MainActivity.kt` | ✅ | CompositionLocalProvider(LocalThemeColors provides ThemeColors(...)) |
| `ui/home/HomeScreen.kt` | ✅ | SkyCalendarBg import 제거, LocalThemeColors.current.calendarBg + todayBg 적용 |
| `ui/diary/DiaryEditorScreen.kt` | ✅ | Scaffold(containerColor = appBg) |

### Functional Match: 100%

| FR | 요구사항 | 상태 | 구현 위치 |
|----|---------|------|---------|
| FR-01 | 달력 기본 배경색 진하게 | ✅ | Color.kt — SkyCalendarBg #E8F4FD→#8EC6E6 |
| FR-02 | 설정 화면 테마 섹션 추가 | ✅ | SettingsScreen.kt — "테마" 섹션 Card |
| FR-03 | 달력 배경색 팔레트 10가지 | ✅ | CalendarBgPalette + ColorPaletteRow |
| FR-04 | 앱 배경색 팔레트 10가지 | ✅ | AppBgPalette + ColorPaletteRow |
| FR-05 | 오늘 날짜 배경색 팔레트 10가지 | ✅ | TodayBgPalette + ColorPaletteRow |
| FR-06 | 색상 변경 즉시 반영 | ✅ | StateFlow → MainActivity CompositionLocalProvider → Recompose |
| FR-07 | 재실행 후 색상 유지 | ✅ | ThemePreferences.putInt() → SettingsViewModel 초기값 |
| FR-08 | 기본값으로 초기화 버튼 | ✅ | resetThemeColors() TextButton |

### Contract Match: 100%

| 설계 결정 | 준수 여부 |
|---------|---------|
| Option C Pragmatic (신규 2개, 수정 7개) | ✅ |
| ThemePreferences — SharedPreferences (NotificationPreferences 동일 패턴) | ✅ |
| LocalThemeColors — compositionLocalOf + ThemeColors.Default 기본값 | ✅ |
| CompositionLocalProvider — MainActivity DiaryAppTheme 내부 최상위 | ✅ |
| `Color(Int)` 라운드트립 — `color.toArgb()` ↔ `Color(argbInt)` 수학적 동일성 | ✅ |
| ColorPaletteRow isSelected — `color == selectedColor` 정확한 비교 | ✅ |

---

## Gap Items: 없음

---

## Plan Success Criteria 검증

| 기준 | 상태 | 근거 |
|------|:----:|------|
| SC-01: 달력 배경 기존보다 진하게, 평일 글씨 가독성 | ✅ Met | Color.kt SkyCalendarBg #8EC6E6 (WCAG 대비 향상) |
| SC-02: 설정 > 테마에서 3가지 색상 팔레트 표시 | ✅ Met | SettingsScreen.kt 테마 섹션 3개 ColorPaletteRow |
| SC-03: 색상 선택 후 즉시 반영 | ✅ Met | StateFlow → CompositionLocalProvider → Recompose |
| SC-04: 앱 종료 후 재실행에도 색상 유지 | ✅ Met | SharedPreferences 저장 + SettingsViewModel 초기값 |
| SC-05: 초기화 버튼으로 기본값 복원 | ✅ Met | resetThemeColors() + ThemePreferences.resetToDefaults() |
| SC-06: 기존 알림 설정, 로그아웃, CRUD 회귀 없음 | ✅ Met | 데이터 레이어 무변경, SettingsScreen 알림 섹션 유지 |

**Success Rate**: 6/6 기준 충족 (100%)

---

## Match Rate Summary

| 축 | 점수 | 가중치 | 기여 |
|----|------|--------|------|
| Structural | 100% | 0.2 | 20 |
| Functional | 100% | 0.4 | 40 |
| Contract | 100% | 0.4 | 40 |
| **Overall** | **100%** | | **100** |

---

## Version History

| Version | Date | Author |
|---------|------|--------|
| 0.1 | 2026-05-17 | faith79@jobkorea.co.kr |
