# joyary-upgrade-v5 Plan

> **Summary**: 달력 6줄 고정 + 이미지 300KB 압축 + 일기 배경색/평일 글씨색 개별 10색 선택
>
> **Project**: claude / diary-app
> **Version**: 0.5.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-17
> **Status**: Draft

---

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 달력이 월별로 5줄/6줄이 달라 월 전환 시 레이아웃 점프 발생; 사진 용량이 최대 1MB로 과다; 일기 작성·보기 배경이 흰색으로 고정되어 테마와 불일치; 달력 평일 글씨색 변경 불가 |
| **Solution** | 달력 셀 항상 42개(6×7)로 패딩하여 높이 고정; 이미지 압축 한도 300KB로 하향; 일기 배경색 10색 팔레트 추가; 달력 평일 글씨색 10색 팔레트 추가 |
| **Function/UX Effect** | 월 스와이프 시 레이아웃 점프 없음; 저장 사진 용량 70% 감소; 설정 > 테마에서 일기 배경·평일 글씨색 독립 선택; 작성·보기 화면 배경 색상 통일 |
| **Core Value** | "세부적으로 내 취향대로 꾸미는 조이어리" — 달력·일기 각 영역 색상을 섬세하게 조정 가능 |

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

### 1.1 Purpose

v5는 4가지 독립된 UX 개선을 묶어 처리한다:

1. **달력 높이 고정**: 5줄 달(예: 4월)도 6줄 높이로 통일하여 HorizontalPager 스와이프 시 레이아웃 재조정 없음
2. **이미지 압축 강화**: 현재 1MB 한도 → 300KB로 줄여 Firebase Storage/네트워크 비용 절감
3. **일기 배경색 통일·선택**: DiaryDetailScreen에 LocalThemeColors.diaryBg 적용, 작성/보기 동일 배경; 설정에서 10색 선택
4. **달력 평일 글씨색 선택**: 현재 `onSurface` 고정 → 설정에서 10색 선택

### 1.2 Background

- v4에서 10종 통합 테마를 도입했지만 일기 화면 배경색과 달력 평일 글씨색은 개별 조정 불가로 남음
- v3 방식(개별 색상 선택)을 이 두 요소에 적용하여 세밀한 커스터마이징 완성

### 1.3 Related Documents

- v4 Plan: `docs/01-plan/features/joyary-upgrade-v4.plan.md`
- v4 Report: `docs/04-report/features/joyary-upgrade-v4.report.md`

---

## 2. Scope

### 2.1 In Scope

- [x] **달력 6줄 고정**: `CalendarGrid`에서 `cells` 항상 42개 패딩
- [ ] **이미지 압축 300KB**: `ImageCompressor.maxSizeBytes = 307_200L`
- [ ] **일기 배경색 통일**: `DiaryDetailScreen` containerColor = `LocalThemeColors.current.diaryBg`
- [ ] **일기 배경색 10색 팔레트**: `ThemeColors.diaryBg` 필드 + Color.kt 상수 + 설정 UI
- [ ] **달력 평일 글씨색 10색 팔레트**: `ThemeColors.weekdayColor` 필드 + Color.kt 상수 + 설정 UI
- [ ] `ThemePreferences`, `SettingsViewModel`, `SettingsScreen`, `MainActivity` 연동

### 2.2 Out of Scope

- v4 통합 테마(AppThemeTemplate) 색상 변경
- 토·일요일 글씨색 변경 (DateSaturday/DateSunday 유지)
- 폰트 크기/타이포그래피 변경
- 달력 셀 크기(60dp) 변경

---

## 3. Requirements

### 3.1 Functional Requirements

| ID | 요구사항 | 우선순위 |
|----|---------|---------|
| FR-01 | 달력 셀을 항상 42개로 패딩하여 모든 달 6줄 높이로 통일 | Must |
| FR-02 | 이미지 저장 시 300KB(307,200 bytes) 이하로 압축 보장 | Must |
| FR-03 | DiaryDetailScreen 배경을 LocalThemeColors.diaryBg로 교체 | Must |
| FR-04 | DiaryEditorScreen 배경을 diaryBg로 변경 (기존 appBg에서 교체) | Must |
| FR-05 | 설정에 "일기 배경색" 팔레트 행 추가 — 파스텔 10색 | Must |
| FR-06 | 설정에 "평일 글씨색" 팔레트 행 추가 — 가독성 10색 | Must |
| FR-07 | 선택한 색상을 SharedPreferences 저장, 재실행 후 유지 | Must |
| FR-08 | 기본값으로 초기화 버튼 (diaryBg=크림, weekdayColor=진회색) | Should |

### 3.2 색상 팔레트 정의

#### 일기 배경색 팔레트 (DiaryBgPalette) — 밝고 부드러운 파스텔 10색

| # | 이름 | Hex |
|---|------|-----|
| 0 | 흰색 | #FFFFFF |
| 1 | 크림 (기본) | #FFF8F0 |
| 2 | 연노랑 | #FFFDE7 |
| 3 | 연분홍 | #FFF0F5 |
| 4 | 연보라 | #F5F0FF |
| 5 | 민트 크림 | #F0FAF6 |
| 6 | 하늘 크림 | #F0F8FF |
| 7 | 세이지 크림 | #F2F8F0 |
| 8 | 모카 크림 | #FBF8F5 |
| 9 | 코랄 크림 | #FFF2F0 |

#### 평일 글씨색 팔레트 (WeekdayColorPalette) — 가독성 있는 10색

| # | 이름 | Hex |
|---|------|-----|
| 0 | 진회색 (기본) | #424242 |
| 1 | 차콜 | #37474F |
| 2 | 네이비 | #1A237E |
| 3 | 딥그린 | #1B5E20 |
| 4 | 딥퍼플 | #4A148C |
| 5 | 로얄블루 | #0D47A1 |
| 6 | 딥틸 | #004D40 |
| 7 | 딥브라운 | #3E2723 |
| 8 | 딥오렌지 | #BF360C |
| 9 | 블루그레이 | #546E7A |

### 3.3 Non-Functional Requirements

| 카테고리 | 기준 |
|---------|------|
| 이미지 용량 | 저장 후 파일 크기 ≤ 300KB 보장 |
| 기존 회귀 없음 | v4 통합 테마, 알림, 로그인, 일기 CRUD 동작 유지 |
| 외부 라이브러리 추가 없음 | 기존 Compose, Material3 내에서 처리 |
| 즉시 반영 | 색상 선택 후 재시작 없이 해당 화면에 즉시 적용 |

---

## 4. 파일 영향 범위

| 파일 | 변경 유형 | 주요 내용 |
|------|---------|---------|
| `ui/theme/LocalThemeColors.kt` | 수정 | `ThemeColors`에 `diaryBg`, `weekdayColor` 필드 추가 |
| `ui/theme/Color.kt` | 수정 | `DiaryBgPalette`, `WeekdayColorPalette` 상수 추가 |
| `notification/ThemePreferences.kt` | 수정 | `diaryBgColor`, `weekdayColor` Int 추가 |
| `viewmodel/SettingsViewModel.kt` | 수정 | `diaryBgColor`, `weekdayColor` StateFlow + setter 추가 |
| `ui/settings/SettingsScreen.kt` | 수정 | ColorPaletteRow 2개 추가 (일기배경, 평일글씨) |
| `MainActivity.kt` | 수정 | ThemeColors 생성 시 diaryBg, weekdayColor 포함 |
| `ui/home/HomeScreen.kt` | 수정 | CalendarGrid: cells 42개 패딩; DayCell: weekdayColor 적용 |
| `ui/diary/DiaryEditorScreen.kt` | 수정 | containerColor: appBg → diaryBg |
| `ui/diary/DiaryDetailScreen.kt` | 수정 | containerColor: default → diaryBg |
| `data/util/ImageCompressor.kt` | 수정 | maxSizeBytes: 1MB → 300KB |

**신규 0개 / 수정 10개**

---

## 5. Success Criteria

| # | 기준 | 검증 방법 |
|---|------|---------|
| SC-01 | 4월↔5월 스와이프 시 달력 높이가 동일함 | 에뮬레이터 스와이프 확인 |
| SC-02 | 사진 업로드 후 저장된 이미지가 300KB 이하 | 로그 또는 파일 크기 확인 |
| SC-03 | 일기 작성·보기 화면 배경이 동일하게 표시 | 에뮬레이터 시각 확인 |
| SC-04 | 설정에서 일기 배경색 선택 후 즉시 작성·보기 화면 반영 | 에뮬레이터 확인 |
| SC-05 | 설정에서 평일 글씨색 선택 후 달력 평일 텍스트 즉시 반영 | 에뮬레이터 확인 |
| SC-06 | 재실행 후 선택 색상 유지 | 재실행 확인 |
| SC-07 | 초기화 버튼 → diaryBg=크림(#FFF8F0), weekday=진회색(#424242) | UI 동작 확인 |
| SC-08 | v4 통합 테마, 알림, 일기 CRUD 회귀 없음 | 기능 테스트 |

---

## 6. Risks & Mitigation

| Risk | 심각도 | 대응 |
|------|--------|------|
| `ThemeColors` 필드 추가 시 v4 `AppThemeTemplate.themeColors` 생성자 미갱신 | Medium | ThemeColors 기본값 설정 후 AppThemeTemplate.kt는 수정하지 않음 (별도 SharedPreferences로 관리) |
| `LazyVerticalGrid` 42개 고정 시 내부 높이 계산 오류 | Low | `userScrollEnabled = false` + 부모에 `wrapContentHeight` 제거로 처리 |
| 이미지 품질 저하 (300KB 한도에서 과도한 압축) | Low | 최저 quality=10 도달 시 그대로 저장; 원본 해상도 먼저 낮추는 inSampleSize 옵션 추가 |
| MainActivity ThemeColors 생성 위치 변경 | Low | v4처럼 template.themeColors 사용 중 — diaryBg/weekdayColor는 별도 Prefs에서 override |

### 6.1 ThemeColors 확장 전략

v4의 `AppThemeTemplate`은 `ThemeColors(calendarBg, appBg, todayBg)`를 제공한다. v5에서 `ThemeColors`에 `diaryBg`와 `weekdayColor`를 추가하면 `AppThemeTemplate.kt`의 10개 인스턴스가 이 필드를 명시해야 한다.

**결정**: `ThemeColors`에 기본값이 있는 선택적 파라미터로 추가하여 기존 AppThemeTemplate 호출부 무수정.

```kotlin
data class ThemeColors(
    val calendarBg: Color,
    val appBg: Color,
    val todayBg: Color,
    val diaryBg: Color = Color(0xFFFFF8F0),     // 기본: 크림
    val weekdayColor: Color = Color(0xFF424242)  // 기본: 진회색
)
```

`MainActivity`에서 template.themeColors 위에 diaryBg/weekdayColor를 override하여 provide:

```kotlin
val template = AppThemeTemplates.getOrElse(templateIndex) { AppThemeTemplates[0] }
val diaryBg by settingsViewModel.diaryBgColor.collectAsStateWithLifecycle()
val weekday by settingsViewModel.weekdayColor.collectAsStateWithLifecycle()

DiaryAppTheme(colorScheme = template.colorScheme) {
    CompositionLocalProvider(
        LocalThemeColors provides template.themeColors.copy(
            diaryBg = diaryBg,
            weekdayColor = weekday
        )
    ) { ... }
}
```

---

## 7. Technical Design

### 7.1 달력 6줄 고정 구현

```kotlin
// 기존
val cells = buildList {
    repeat(startDayOfWeek) { add(null) }
    for (d in 1..daysInMonth) add(d)
}

// 변경 — 항상 42개 패딩
val cells = buildList {
    repeat(startDayOfWeek) { add(null) }
    for (d in 1..daysInMonth) add(d)
    while (size < 42) add(null)  // 6줄×7일 고정
}
```

### 7.2 ImageCompressor 300KB

```kotlin
private val maxSizeBytes = 307_200L  // 300 × 1024 bytes
```

---

## 8. Next Steps

1. [ ] `/pdca design joyary-upgrade-v5`
2. [ ] 구현 (`/pdca do joyary-upgrade-v5`)
3. [ ] Gap Analysis (`/pdca analyze joyary-upgrade-v5`)

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-17 | Initial draft | faith79@jobkorea.co.kr |
