# Plan: tab-memo-fullscreen

## Executive Summary

| 관점 | 내용 |
|------|------|
| Problem | 하단 탭 라벨로 높이 낭비; 메모 에디터 BottomSheet가 불안정하게 닫힘 |
| Solution | 탭 라벨 제거+높이 56dp; ModalBottomSheet → 전체화면 Scaffold 에디터 |
| UX Effect | 달력 영역 확보; 메모 편집 화면이 일기처럼 전체화면 + 스크롤 지원 |
| Core Value | 더 안정적이고 여유로운 메모 작성 경험 |

## Context Anchor

| 항목 | 내용 |
|------|------|
| WHY | BottomSheet 불안정 + 탭 높이 낭비 |
| WHO | 메모장/투두 탭 사용자 |
| RISK | Activity-scoped MemoViewModel 공유 필요 |
| SUCCESS | 탭 라벨 없음, 에디터 전체화면, 스크롤 동작 |
| SCOPE | Screen.kt, NavGraph.kt, HomeScreen.kt, MemoScreen.kt (4파일) |

## 요구사항

1. NavigationBar 라벨('일기','메모장') 제거, 높이 56dp
2. MemoEditorSheet(ModalBottomSheet) → MemoEditorScreen(전체화면 Scaffold)
3. 에디터 콘텐츠 verticalScroll 추가
4. Activity-scoped MemoViewModel로 화면 간 상태 공유
