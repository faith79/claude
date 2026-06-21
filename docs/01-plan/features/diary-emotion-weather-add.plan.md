# Plan: diary-emotion-weather-add

## WHY
사용자가 일기 작성 시 선택 가능한 감정·날씨 옵션을 확장하여 더 정확한 기록을 남길 수 있도록 한다.

## WHO
조이어리 앱 사용자 — 일기 편집 화면에서 감정·날씨 태그를 선택하는 모든 사용자

## SCOPE
- EmotionTag enum에 `ANNOYED("😤", "짜증")` 추가
- WeatherTag enum에 `HUMID("💧", "습함")`, `HOT("🥵", "더움")`, `COLD("🥶", "추움")` 추가
- UI 코드는 `*.entries` 순회 방식이므로 추가 변경 불필요

## OUT OF SCOPE
- 기존 일기 데이터 마이그레이션 (enum 이름 기반 저장이므로 기존 데이터 무영향)
- UI 레이아웃 변경

## RISK
- EmotionSelector가 Row + weight(1f) 구조라 8개 항목이 되면 각 칩 폭이 줄어들 수 있음
  → 현재 7개도 동일 방식이므로 허용 범위 내로 판단

## SUCCESS
- 일기 편집 화면에서 '짜증', '습함', '더움', '추움'이 선택 가능하게 표시됨
- 기존 감정·날씨 선택 기능 정상 작동 유지
