# Plan: ladder-game-fix

## Context Anchor
- **WHY**: 사다리 게임이 한 줄로 직선 내려가는 경우가 많고, 게임 시작 전에 결과가 노출되어 서프라이즈 효과가 없음
- **WHO**: 조이어리 앱 사용자 (다이어리 앱 내 미니게임)
- **RISK**: generateLadder 알고리즘 변경 시 tracePath 로직과의 호환성 유지 필요
- **SUCCESS**: 모든 경로가 최소 2회 이상 좌우 이동, 게임 시작 전 결과 완전 비공개
- **SCOPE**: LadderGameScreen.kt 단일 파일

## 요구사항

### F1: 복잡한 사다리 생성
- 현황: rows=20, probability=0.75f로 생성하나 연속 직선 구간 발생
- 목표: 어떤 컬럼도 2행 이상 연속 직선 이동 금지
- 구현: straight-streak 추적 + 강제 가로 연결 삽입

### F2: 결과 시작 전 비공개
- 현황: 하단 items 행이 NAMING/REVEALING 진입 직후부터 결과값 노출
- 목표: `isHidden=true` 상태(게임 시작 전)일 때 하단 결과를 "?"로 표시
- 구현: items Row에서 `isHidden` 조건 분기 추가

### F3: 결과 랜덤 배치 (기존 동작 확인)
- `items = allItems.shuffled()` — onStart, restart() 모두 적용 중 → 변경 불필요

## 범위 내

- `generateLadder()` 알고리즘 개선
- `LadderGameContent()` 하단 items Row 표시 조건 수정

## 범위 외

- tracePath(), 애니메이션 로직, 결과 다이얼로그, 네비게이션 — 변경 없음
