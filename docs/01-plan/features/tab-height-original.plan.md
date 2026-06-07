# Plan: tab-height-original

## Executive Summary

| 관점 | 내용 |
|------|------|
| Problem | 하단 탭 높이가 192dp로 과도하게 큼 |
| Solution | 처음 만들었을 때(20a3d7b)의 기본 높이로 복원 — height 지정 제거 (Material3 기본 80dp) |
| UX Effect | 탭이 적절한 크기로 복원, 콘텐츠 영역 확보 |
| Core Value | 원래 의도한 NavigationBar 비율 복원 |

## Context Anchor

| 항목 | 내용 |
|------|------|
| WHY | 192dp는 너무 커서 화면 공간 낭비, 원본(diary-tab-memo) 높이 복원 요청 |
| SUCCESS | NavigationBar height 지정 없음 (Material3 기본 80dp) |
| SCOPE | HomeScreen.kt 1줄 수정 |

## Requirements

- 원본 커밋(20a3d7b diary-tab-memo): `NavigationBar { ... }` — height 미지정 (기본 80dp)
- 현재: `NavigationBar(modifier = Modifier.height(192.dp))`
- 목표: height modifier 제거 → Material3 기본값 80dp 적용
