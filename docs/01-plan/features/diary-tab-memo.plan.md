# diary-tab-memo Plan

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 앱에 메모/할일 기능이 없어 일기 외 간단한 기록을 남길 수 없음 |
| **Solution** | 하단 NavigationBar(일기/메모장 탭) 추가 + Firestore 기반 메모·TODO 저장 |
| **Function/UX Effect** | 탭 전환으로 일기 ↔ 메모장 이동, 메모는 텍스트/TODO 타입 선택 저장 |
| **Core Value** | 하나의 앱에서 일기와 메모를 함께 관리 |

## Context Anchor

| Key | Value |
|-----|-------|
| WHY | 일기 앱에 메모·할일 기능 추가로 일상 기록 통합 |
| WHO | 기존 조이어리 사용자 |
| RISK | HomeScreen 구조 변경 → 기존 일기 기능 회귀 주의 |
| SUCCESS | 탭 전환 정상 동작, 메모/TODO 저장·삭제·편집 완료 |
| SCOPE | HomeScreen 탭 추가, Memo 데이터 레이어, MemoScreen UI |

## Requirements

- FR-01: 앱 하단에 NavigationBar — 일기 / 메모장 탭
- FR-02: 일기 탭 — 기존 HomeScreen 달력+FAB 기능 유지
- FR-03: 메모장 탭 — 메모 목록 + 추가/편집/삭제
- FR-04: 메모 타입 선택 — 텍스트 메모 / TODO 리스트
- FR-05: TODO — 항목별 완료 체크박스
- FR-06: Firestore 저장 (users/{userId}/memos 컬렉션)

## Success Criteria

- SC-01: NavigationBar 탭 전환 정상
- SC-02: 일기 탭 기존 기능 회귀 없음
- SC-03: 메모 추가(텍스트/TODO) → Firestore 저장
- SC-04: 메모 삭제 동작
- SC-05: 메모 편집(탭하여 재오픈) 동작
- SC-06: TODO 항목 체크 상태 저장
