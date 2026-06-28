# Plan: settings-defaults-calendar-year-nav

## Context Anchor
- **WHY**: 기본 테마가 밝은 하늘색으로 설정되어 있어 어두운 테마 선호 사용자가 매번 수동 변경 필요. 달력 화살표가 월 단위여서 연도 이동이 불편함.
- **WHO**: 조이어리 앱 신규 설치 사용자 + 연도 단위 일기 탐색 사용자
- **RISK**: 기존 설정이 저장된 사용자는 영향 없음 (SharedPreferences에 저장된 값 우선). 신규 설치 및 초기화 시에만 새 기본값 적용.
- **SUCCESS**: 앱 최초 실행 시 미드나잇 테마 + 흰색 평일 글씨 + 검정 에디터 배경 적용. << >> 버튼 클릭 시 1년(12개월) 이동.
- **SCOPE**: 3개 파일 수정

[CP-1 Auto] 요구사항 확인됨 → 계속 진행
[CP-2 Auto] 명확화 질문 생략 → 합리적 기본값 적용

## 요구사항

| ID | 요구사항 |
|----|---------|
| R-01 | `selectedTemplateIndex` 기본값 0(하늘) → 20(미드나잇) |
| R-02 | `weekdayColor` 기본값 0xFF424242 → 0xFFFFFFFF (흰색) |
| R-03 | `diaryBgColor` 기본값 0xFFFFF8F0 → 0xFF000000 (검정) |
| R-04 | `resetToDefault()` / `resetDiaryColors()` 새 기본값으로 업데이트 |
| R-05 | `SettingsViewModel.resetThemeTemplate()` hardcoded `0` → `20` |
| R-06 | 달력 화살표 클릭: -1/+1 페이지 → -12/+12 페이지 (년 단위) |
| R-07 | 화살표 아이콘을 `<<` / `>>` 텍스트 스타일로 교체 |

## 변경 파일

| 파일 | 변경 내용 |
|------|---------|
| ThemePreferences.kt | R-01, R-02, R-03, R-04 — 기본값 및 reset 메서드 |
| SettingsViewModel.kt | R-05 — resetThemeTemplate hardcoded 0 수정 |
| HomeScreen.kt | R-06, R-07 — 년 단위 내비게이션 + << >> UI |
