# Plan: settings-color-palette-expand

## Context Anchor
- **WHY**: 설정 색상 팔레트에 흰색·검정·다크그린이 누락 — 어두운 테마와 함께 활용 불가
- **WHO**: 조이어리 앱 사용자
- **RISK**: 없음 (팔레트 항목 추가만, 기존 항목 변경 없음)
- **SUCCESS**: 모든 색상 팔레트에 흰색, 검정, 다크그린 포함
- **SCOPE**: Color.kt 1개 파일 (DiaryBgPalette + WeekdayColorPalette)

[CP-1 Auto] 요구사항 확인됨
[CP-2 Auto] 합리적 기본값 적용

## 현재 상태 vs 목표

| 팔레트 | 하얀색 | 검정 | 다크그린 |
|--------|--------|------|---------|
| DiaryBgPalette (글쓰기 배경) | ✅ 흰색 있음 | ✗ 없음 → 추가 | ✅ 다크그린 있음 |
| WeekdayColorPalette (평일 글씨) | ✗ 없음 → 추가 | ✗ 없음 → 추가 | ✗ 없음 → 추가 (딥그린과 별도) |

## 추가할 색상값
| 색상명 | 색상코드 | 용도 |
|--------|---------|------|
| 검정 | `#000000` | DiaryBgPalette, WeekdayColorPalette |
| 하얀색 | `#FFFFFF` | WeekdayColorPalette |
| 다크그린 | `#1A2E1A` | WeekdayColorPalette (DiaryBgPalette는 이미 존재) |

## Success Criteria
- SC-01: DiaryBgPalette에 "검정" Color(0xFF000000) 추가
- SC-02: WeekdayColorPalette에 "하얀색" Color(0xFFFFFFFF) 추가
- SC-03: WeekdayColorPalette에 "검정" Color(0xFF000000) 추가
- SC-04: WeekdayColorPalette에 "다크그린" Color(0xFF1A2E1A) 추가
