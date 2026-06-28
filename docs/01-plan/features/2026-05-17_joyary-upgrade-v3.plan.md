# joyary-upgrade-v3 Plan

> **Feature**: joyary-upgrade-v3
> **Date**: 2026-05-17
> **Author**: faith79@jobkorea.co.kr
> **Phase**: Plan

---

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 달력 배경(#E8F4FD)이 너무 연해 평일 글씨 가시성이 낮고, 사용자가 앱 색상을 취향에 맞게 바꿀 수 없어 시각적 불편이 반복됨 |
| **Solution** | 달력 기본 배경색 즉시 진하게 변경 + 설정에서 달력/앱/오늘날짜 배경색을 팔레트 선택으로 커스텀 가능하게 함 |
| **Function/UX Effect** | 텍스트 가시성 즉시 개선; 설정→테마에서 원터치로 3가지 배경색 커스텀; 변경 즉시 전 화면 반영; SharedPreferences 로컬 저장으로 앱 재실행 후에도 유지 |
| **Core Value** | 눈에 잘 보이고 취향에 맞게 꾸밀 수 있는 조이어리 — 시각적 쾌적함과 개인화로 일기 작성 습관 강화 |

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 달력 가시성 문제 즉시 해결 + 사용자 취향 반영 가능한 테마 설정 |
| **WHO** | 조이어리 앱 기존 사용자 (기본 색상이 안 보인다는 불편 경험자) |
| **RISK** | CompositionLocal 전파 범위, SharedPreferences → StateFlow 초기값 동기화, 팔레트 색상과 텍스트 가시성 보장 |
| **SUCCESS** | FR-01~FR-05 구현 완료 + 설정에서 색상 변경 후 즉시 홈/편집 화면에 반영 확인 |
| **SCOPE** | UI 레이어 + 설정 레이어만 변경 (데이터/인증 무변경) |

---

## 1. Requirements

### 1.1 Functional Requirements

| ID | 요구사항 | 우선순위 |
|----|---------|---------|
| FR-01 | 달력 기본 배경색을 현재(#E8F4FD)보다 진한 색으로 즉시 변경 | Must |
| FR-02 | 설정 화면에 **테마** 섹션 추가 | Must |
| FR-03 | 테마 섹션: 달력 배경색 팔레트 선택 (10가지 색상) | Must |
| FR-04 | 테마 섹션: 앱 배경색 팔레트 선택 (글쓰기/수정 화면 배경 포함, 10가지 색상) | Must |
| FR-05 | 테마 섹션: 오늘 날짜 배경색 팔레트 선택 (10가지 색상) | Must |
| FR-06 | 색상 변경 즉시 반영 (앱 재시작 불필요) | Must |
| FR-07 | 앱 재실행 후에도 설정값 유지 (SharedPreferences 저장) | Must |
| FR-08 | 기본값으로 초기화 버튼 제공 | Should |

### 1.2 Non-Functional Requirements

| ID | 요구사항 |
|----|---------|
| NFR-01 | 팔레트 색상은 모두 텍스트 가시성 기준 통과 (배경 대비 충분) |
| NFR-02 | 기존 기능 (알림 설정, 로그아웃, CRUD) 회귀 없음 |
| NFR-03 | 신규 외부 라이브러리 추가 없음 |

---

## 2. Scope

### 2.1 In Scope

- `Color.kt`: FR-01 — SkyCalendarBg 기본값 진하게 변경 + 팔레트 상수 정의
- `ThemePreferences.kt` (신규): SharedPreferences 색상 저장소
- `LocalThemeColors.kt` (신규): CompositionLocal + ThemeColors 데이터 클래스
- `SettingsViewModel.kt`: 3개 색상 StateFlow 추가
- `SettingsScreen.kt`: 테마 섹션 + ColorPaletteRow 컴포넌트 추가
- `Theme.kt`: DiaryAppTheme에서 LocalThemeColors provide
- `MainActivity.kt`: SettingsViewModel inject → LocalThemeColors provide at root
- `HomeScreen.kt`: LocalThemeColors.current.calendarBg + todayBg 사용
- `DiaryEditorScreen.kt`: LocalThemeColors.current.appBg 사용

### 2.2 Out of Scope

- 자유 색상 선택기 (HSV/RGB 슬라이더)
- Firebase 클라우드 색상 동기화
- 다크 모드 별도 팔레트
- 텍스트 색상/폰트 크기 커스텀

---

## 3. Technical Design Overview

### 3.1 아키텍처 패턴

```
SharedPreferences (ThemePreferences)
    ↓ (읽기/쓰기)
SettingsViewModel (StateFlow<Color> × 3)
    ↓ (collectAsStateWithLifecycle)
MainActivity → LocalThemeColors.provides(ThemeColors(...))
    ↓ (CompositionLocal)
HomeScreen, DiaryEditorScreen, SettingsScreen
    (LocalThemeColors.current.calendarBg / .appBg / .todayBg)
```

### 3.2 ThemeColors 데이터 클래스

```kotlin
data class ThemeColors(
    val calendarBg: Color,   // 달력 배경
    val appBg: Color,        // 앱/편집기 배경
    val todayBg: Color       // 오늘 날짜 강조 배경
)
```

### 3.3 팔레트 정의 (Color.kt)

| 용도 | 팔레트 색상 (10가지) |
|------|-------------------|
| 달력 배경 | #B3D9F0, #8EC6E6, #6BB4DC, #E8F4FD, #D1EAF8, #A0CBDF, #7ABCD6, #5AAAC8, #3D98BA, #2E86AB |
| 앱 배경 | #F0F8FF, #E8F4FD, #DDF0FB, #D0E8F5, #C2E0EF, #B0D4E8, #F5FBFF, #EBF6FC, #E0F1FA, #D5ECF8 |
| 오늘 날짜 배경 | #7EC8E3, #5BB8D4, #3DA8C5, #2998B6, #1588A7, #81D4FA, #4FC3F7, #29B6F6, #03A9F4, #0288D1 |

**FR-01 기본값**: `SkyCalendarBg = Color(0xFF8EC6E6)` (현재 #E8F4FD → #8EC6E6으로 진하게)

### 3.4 파일 영향 범위

| 파일 | 변경 유형 | 주요 내용 |
|------|---------|---------|
| `Color.kt` | 수정 | SkyCalendarBg 기본값 변경 + 팔레트 상수 추가 |
| `ThemePreferences.kt` | **신규** | SharedPreferences 색상 저장/로드 |
| `LocalThemeColors.kt` | **신규** | CompositionLocal<ThemeColors> 정의 |
| `SettingsViewModel.kt` | 수정 | ThemePreferences inject + 3개 StateFlow 추가 |
| `SettingsScreen.kt` | 수정 | 테마 섹션 + ColorPaletteRow 컴포넌트 |
| `Theme.kt` | 수정 | LocalThemeColors provide |
| `MainActivity.kt` | 수정 | SettingsViewModel inject + provide |
| `HomeScreen.kt` | 수정 | LocalThemeColors.current.calendarBg / todayBg 사용 |
| `DiaryEditorScreen.kt` | 수정 | LocalThemeColors.current.appBg 사용 |

**신규 파일 2개 / 수정 파일 7개**

---

## 4. Risk & Mitigation

| Risk | 심각도 | 대응 |
|------|--------|------|
| CompositionLocal provide 누락 시 crash | High | MainActivity 최상위 provide, 기본값 설정 |
| SharedPreferences Int → Color 변환 오류 | Medium | Color(int) / color.toArgb() 명확히 사용 |
| 팔레트 색상 텍스트 대비 불충분 | Medium | 모든 팔레트 색상 WCAG AA 기준 사전 검증 |
| 기존 SkyCalendarBg 하드코딩 참조 누락 | Low | HomeScreen.kt 전체 검색으로 확인 |

---

## 5. Success Criteria

| # | 기준 | 검증 방법 |
|---|------|---------|
| SC-01 | FR-01: 달력 배경이 기존보다 진하게 표시, 평일 글씨 가독성 확인 | 에뮬레이터 시각적 확인 |
| SC-02 | FR-02~FR-05: 설정 > 테마에서 3가지 색상 팔레트 표시 | UI 확인 |
| SC-03 | FR-06: 색상 선택 후 홈 화면 복귀 시 즉시 반영 | 에뮬레이터 동작 확인 |
| SC-04 | FR-07: 앱 종료 후 재실행해도 선택 색상 유지 | 에뮬레이터 재실행 확인 |
| SC-05 | FR-08: 초기화 버튼으로 기본값 복원 | UI 동작 확인 |
| SC-06 | 기존 알림 설정, 로그아웃, CRUD 회귀 없음 | 기능 테스트 |

---

## Version History

| Version | Date | Author |
|---------|------|--------|
| 0.1 | 2026-05-17 | faith79@jobkorea.co.kr |
