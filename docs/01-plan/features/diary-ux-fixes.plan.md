# Plan: diary-ux-fixes

## WHY
1. 일기 저장 후 달력(Home)으로 이동하지 않음 — Detail 경유 시 Detail로 복귀
2. 신규 일기 저장 후 캘린더에 안 보임 — HomeScreen VM(NavEntry-스코프)과 Editor VM(Activity-스코프)이 달라 invalidateCache 미적용 + loadMonth 재호출 없음
3. 화면 회전 시 앱 레이아웃이 가로로 전환됨 — AndroidManifest에 screenOrientation 미설정

## WHO
- 조이어리 앱 사용자 (일기 작성 직후 UX 흐름)

## RISK
- VM 스코프 통일 시 HomeScreen이 Activity VM을 공유 → 로그아웃 후 VM 상태 잔류 없는지 확인 필요
- screenOrientation 고정 시 태블릿 가로 사용 불가 (현재는 스마트폰 앱이므로 무관)

## SUCCESS
- 저장 완료 → 항상 Home(달력) 화면으로 이동
- 저장 완료 → 해당 월 캐시 즉시 무효화 + Firestore 재조회 → 새 일기 바로 표시
- 화면 회전 → 앱 레이아웃 세로 유지

## SCOPE (변경 파일)
- SC-01: AndroidManifest.xml — screenOrientation="portrait" 추가
- SC-02: NavGraph.kt — HomeScreen에 Activity-스코프 diaryViewModel 주입, onSaved → popBackStack(Home)
- SC-03: DiaryViewModel.kt — invalidateCache 내 loadMonth 재호출 추가
