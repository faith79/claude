# Plan: emotion-weather-limit-required

## Context Anchor
- **WHY**: 다중 선택 무제한 허용 시 과도한 태그 + 필수 입력 없이 저장 가능 → 품질 저하
- **WHO**: 일기 편집 화면에서 감정/날씨/내용을 입력하는 사용자
- **RISK**: 기존 저장된 일기(감정 0개) 수정 시 저장 버튼 비활성화 → 사용자가 감정 선택 후 저장 필요
- **SUCCESS**: 감정·날씨 각 최대 3개, 저장 버튼은 감정·날씨·내용 모두 입력 시에만 활성화
- **SCOPE**: DiaryEditorScreen.kt, WeatherSelector.kt

## 요구사항

| # | 요구사항 |
|---|---------|
| R-01 | 감정 최대 3개 — 3개 선택 시 미선택 항목 dimmed + 클릭 비활성 |
| R-02 | 날씨 최대 3개 — 3개 선택 시 미선택 항목 dimmed + 클릭 비활성 |
| R-03 | 감정 헤더에 현재 선택 수 표시 "오늘의 감정 (N/3)" |
| R-04 | 날씨 헤더에 현재 선택 수 표시 "날씨 (N/3)" |
| R-05 | 저장 버튼: `selectedEmotions.isNotEmpty() && selectedWeathers.isNotEmpty() && content.isNotBlank()` |
| R-06 | 선택된 항목은 최대 도달 시에도 클릭하여 해제 가능 |

## 변경 파일

| 파일 | 변경 내용 |
|------|---------|
| `DiaryEditorScreen.kt` | 저장 버튼 조건, EmotionSelector maxReached + 카운트, 호출부 |
| `WeatherSelector.kt` | maxReached 파라미터, FilterChip disabled, 카운트 헤더 |

[CP-1 Auto] 요구사항 확인됨 → 계속 진행
[CP-2 Auto] 명확화 질문 생략 → 합리적 기본값 적용
