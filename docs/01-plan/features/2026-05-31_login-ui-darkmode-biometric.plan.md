# Plan: login-ui-darkmode-biometric

## Context Anchor
- **WHY**: 로그인 화면이 회원가입 화면과 UI 구조가 달라 일관성이 없고, 지문인증이 자동으로 뜨지 않아 사용성이 낮음
- **WHO**: 앱 사용자 (기존 로그인 사용자, 지문 등록 사용자)
- **RISK**: 지문 없이 첫 로그인 시 자격증명 없음 → 이메일/비밀번호 fallback 유지 필수
- **SUCCESS**: 로그인 화면이 다크 모드로 보이고, Scaffold 구조가 회원가입 화면과 동일하며, 지문인증이 자동 실행됨
- **SCOPE**: LoginScreen.kt, AuthViewModel.kt (변경 없음)

## Requirements
1. **로그인 UI 다크모드** — LoginScreen을 `DiaryAppTheme(darkTheme = true)`로 감싸 항상 다크 색상 체계 사용
2. **Scaffold 구조 통일** — SignUpScreen과 동일하게 `Scaffold` + `TopAppBar` 사용
3. **지문인증 default** — 화면 진입 시 `LaunchedEffect`로 biometric prompt 자동 실행 (저장된 자격증명 + 하드웨어 지원 시)

## Files to Change
- `diary-app/app/src/main/java/com/example/diaryapp/ui/auth/LoginScreen.kt`

## Auto-decisions
- [CP-1 Auto] 요구사항 확인됨
- [CP-2 Auto] 합리적 기본값 적용 (자격증명 없을 경우 이메일 폼 표시 유지)
