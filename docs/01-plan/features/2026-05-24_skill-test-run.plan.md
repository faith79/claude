# Plan: skill-test-run

## WHY
Joey 스킬 수정 후 "Git Push 먼저 → APK Build 나중" 새 순서가 실제로 작동하는지
검증하는 테스트 런. 실제 기능(앱 버전 정보 표시)을 구현하여 전체 파이프라인을 통과한다.

## WHO
조이어리 앱 사용자, 설정 화면 방문자

## SCOPE
- FR-01: 설정 화면 하단에 "앱 정보" 섹션 추가
- FR-02: 앱 버전명(versionName) 표시 — PackageManager 사용
- FR-03: 버전 코드(versionCode) 함께 표시

## SUCCESS
- 설정 화면 하단에 버전 정보 카드가 표시됨
- Git Push가 APK Build 이전에 완료됨 (새 순서 검증)
- APK 빌드 성공 후 파이프라인 종료

## RISK
- buildConfig 비활성화 → PackageManager로 해결
