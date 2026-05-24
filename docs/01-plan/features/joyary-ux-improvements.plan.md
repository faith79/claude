# Plan: joyary-ux-improvements

## WHY
사용자 경험 개선: 알림 UI 개선, 월간 탐색 속도 향상, 에디터 배경색 커스터마이징, 색상 템플릿 확장

## WHO
조이어리 앱 사용자, 앱 관리자(설정 화면)

## SCOPE
- FR-01: 알림 아이콘 → 앱 아이콘(ic_launcher_foreground)으로 변경
- FR-02: 알림 클릭 시 MainActivity 실행 (PendingIntent)
- FR-03: 이전/다음 달 데이터 미리 불러오기 (prefetchMonth)
- FR-04: 글쓰기/수정 화면 배경색 설정화면에서 지정 가능
- FR-05: 색상 템플릿 10개 → 20개

## SUCCESS
- 알림 아이콘이 앱 아이콘으로 표시됨
- 알림 클릭 시 앱 열림
- 이전/다음 달 이동 시 즉시 표시 (캐시 선로딩)
- 설정 > 글쓰기 배경색 팔레트 노출, 에디터에 반영
- 설정 화면 색상 템플릿 20개 표시
