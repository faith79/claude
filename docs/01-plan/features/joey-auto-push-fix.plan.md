# Plan: joey-auto-push-fix

## WHY
Joey 스킬 완전 무인 자동화 검증. git add Windows 호환 수정 후
실제 파이프라인(commit → push → APK) 전체가 인터럽트 없이 동작하는지 확인.

## WHO
개발자 (스킬 사용자)

## SCOPE
- FR-01: Settings 앱 정보 카드에 저작권 연도 텍스트 추가
- FR-02: 앱 버전 정보 카드를 탭하면 버전 문자열 복사 (Clipboard)

## SUCCESS
- 파이프라인이 사용자 입력 없이 완료됨
- git add → commit → push 순서로 진행, 이후 APK 빌드
- 파이프라인 종료 후 `git status` 에 uncommitted 변경 없음
