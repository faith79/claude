# Plan: calendar-daycell-fix

## Context Anchor
- **WHY**: (1) 60dp 셀에 이모지+날짜가 넘쳐 날짜 숫자 잘림, (2) 오늘 표시가 채워진 원 → 빈 네모(테두리)로 변경 요청
- **WHO**: 조이어리 앱 사용자
- **RISK**: 셀 높이 증가 시 달력 카드 전체 높이 증가 — 화면 레이아웃 확인 필요
- **SUCCESS**: 이모지+날짜 완전 노출 / 오늘 = 테두리만 있는 네모
- **SCOPE**: HomeScreen.kt 1개 파일

[CP-1 Auto] 요구사항 확인됨
[CP-2 Auto] 합리적 기본값 적용

## 원인 분석

| 항목 | 현재 | 문제 |
|------|------|------|
| DayCell height | 60.dp | 이모지(~32dp) + spacer(2dp) + 날짜(~17dp) + padding(8dp) = 59dp → 겨우 맞음, 실제론 잘림 |
| 오늘 표시 | `clip(CircleShape).background(primary)` 채워진 원 | 채워진 원 → 빈 네모 테두리로 변경 필요 |
| 빈 셀 height | Box(60.dp) | DayCell과 동일하게 맞춰야 함 |

## 요구사항
- **FR-01**: DayCell.height 60dp → 76dp (이모지 + 날짜 여유있게 표시)
- **FR-02**: 빈 셀 Box.height 60dp → 76dp
- **FR-03**: 오늘 표시 = `.border(2.dp, primary, RoundedCornerShape(4.dp))` (테두리만, 채우기 없음)
- **FR-04**: `.clip(CircleShape).background(bgColor)` 제거
- **FR-05**: 오늘 날짜 텍스트 색상: `onPrimary`(흰색) → `primary` (테두리와 동일 색상으로 강조)

## Success Criteria
- SC-01: DayCell Modifier.height = 76.dp
- SC-02: 빈 셀 Box Modifier.height = 76.dp
- SC-03: isToday 시 border(2.dp, primary, RoundedCornerShape(4.dp)) 존재
- SC-04: clip(CircleShape), background(bgColor) 없음
- SC-05: isToday 날짜 텍스트 color = MaterialTheme.colorScheme.primary
