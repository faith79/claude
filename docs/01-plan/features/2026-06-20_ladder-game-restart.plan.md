# Plan: ladder-game-restart

## Context Anchor
- **WHY**: 사다리 게임 3가지 개선 — 빠른 시작, 마지막 경로 색상 버그, 다시 하기
- **WHO**: 조이어리 앱 사용자
- **RISK**: rowH 계산식 변경 없이 마지막 세그먼트 연장 필요
- **SUCCESS**: 3가지 기능 모두 동작, 빌드 성공
- **SCOPE**: LadderGameScreen.kt 단일 파일

## Requirements

### F1: 바로 시작 버튼
- INPUT 화면에 "바로 시작" 버튼 추가 (기존 "사다리 만들기" 옆에)
- 클릭 시: 이름 입력 단계(NAMING) 없이 names를 숫자("1","2","3"...)로 자동 설정
- 바로 REVEALING 단계로 진입

### F2: 마지막 경로 색상 버그
- 원인: rowH = size.height/(rungs.size+1), 마지막 세그먼트 y2 = numSegs*rowH < size.height
- drawLadderPath: 마지막 열에 numSegs*rowH → size.height 연장 선 추가
- drawPartialLadderPath: 마지막 세그먼트 직진 시 y2 = size.height
- computeDotOffset: canvasH 파라미터 추가, 마지막 직진 세그먼트 y2 연장

### F3: 다시 하기 버튼
- REVEALING 단계(이름 입력 완료)에 "다시 하기" 버튼 표시
- 클릭 시: rungs 재생성(랜덤), items 재셔플, revealedPaths 초기화, phase → NAMING
- inputs(설정값) 유지, names 유지(이전 이름 유지하되 변경 가능)
- 결과 팝업(showResultsDialog)에도 "다시 하기" 버튼 추가

## CP-1 Auto: 요구사항 확인됨
## CP-2 Auto: 합리적 기본값 적용
