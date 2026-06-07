# Plan: tab-height-3x

## Executive Summary

| 관점 | 내용 |
|------|------|
| Problem | 하단 탭(NavigationBar)이 64dp로 너무 작아 터치가 어려움 |
| Solution | 현재 높이의 3배(192dp)로 확대 |
| UX Effect | 탭 터치 영역이 넓어져 조작 편의성 향상 |
| Core Value | 손가락 터치 실수 감소 |

## Context Anchor

| 항목 | 내용 |
|------|------|
| WHY | 사용자가 64dp 탭이 너무 작다고 불편 호소 |
| WHO | 조이어리 앱 사용자 |
| RISK | 화면 하단 많은 공간 차지로 달력·메모 콘텐츠 영역 축소 |
| SUCCESS | NavigationBar height=192dp, 빌드 성공 |
| SCOPE | HomeScreen.kt NavigationBar 높이 수정만 |

## Requirements

- NavigationBar 높이: 64dp → 192dp (3배)
- 아이콘은 그대로 유지 (크기 변경 없음)
