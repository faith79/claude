# Plan: joyary-app-improvements

## Context Anchor
- **WHY**: UX 개선 5건 — 레이아웃, 테마 연동, 알림 신뢰성, 텍스트 가독성
- **WHO**: 조이어리 앱 사용자 (Android)
- **RISK**: 알림 권한 요청 흐름 변경 시 기존 enabled 상태 불일치 가능
- **SUCCESS**: 달력 top 고정, 배경색 자동 연동, 알림 정시 수신, 텍스트 검정 고정
- **SCOPE**: HomeScreen, SettingsScreen, MainActivity, DailyReminderWorker, DiaryEditorScreen

[CP-1 Auto] 요구사항 확인됨 → 계속 진행
[CP-2 Auto] 명확화 질문 생략 → 합리적 기본값 적용

## Requirements

### FR-01: 달력 top 정렬 + FAB '+' 아이콘만
- `LargeTopAppBar` → `TopAppBar` (scrollBehavior/nestedScroll 제거)
- `ExtendedFloatingActionButton` → `FloatingActionButton` (icon only)
- **Files**: `HomeScreen.kt`

### FR-02: 글쓰기 배경색 = 색상테마 자동 연동
- SettingsScreen 에서 "글쓰기 배경색" ColorPaletteRow 제거
- MainActivity 에서 `settingsViewModel.diaryBgColor` 대신 `template.themeColors.appBg` 직접 사용
- **Files**: `SettingsScreen.kt`, `MainActivity.kt`

### FR-03: 알림 실제 작동 (POST_NOTIFICATIONS 런타임 권한)
- Android 13+ 는 POST_NOTIFICATIONS 런타임 권한 필수
- Switch 토글 시 권한 미부여 → 권한 요청 먼저, 승인 후 enable
- **Files**: `SettingsScreen.kt`

### FR-04: 알림 아이콘 = 앱 아이콘 (모노크롬)
- `ic_notification.xml` 생성 (별 모양, 흰색 단색)
- DailyReminderWorker setSmallIcon 교체
- **Files**: `drawable/ic_notification.xml`, `DailyReminderWorker.kt`

### FR-05: 알림 클릭 → 앱 열기
- 이미 구현됨 (pendingIntent + setContentIntent + setAutoCancel)
- 검증 후 유지

### FR-06: 일기 쓰기/수정 텍스트 컬러 검정 고정
- `OutlinedTextField.colors` 파라미터로 `focusedTextColor`, `unfocusedTextColor` = `Color.Black`
- **Files**: `DiaryEditorScreen.kt`

## Success Criteria
- SC-01: HomeScreen 달력이 TopAppBar 바로 아래 시작 (LargeTopAppBar 제거)
- SC-02: FAB 가 '+' 아이콘만 노출
- SC-03: 테마 변경 시 글쓰기 배경색 자동 반영
- SC-04: Android 13+ 에서 알림 토글 시 권한 다이얼로그 표시
- SC-05: DailyReminderWorker 가 ic_notification 아이콘 사용
- SC-06: DiaryEditorScreen 텍스트 입력 색상 = Color.Black
