# Design: login-ui-darkmode-biometric

## Architecture Options

### Option A — Minimal
- `isSystemInDarkTheme()` override 방식으로 다크 강제 적용
- Scaffold 추가 없이 Column 유지

### Option B — Clean
- 별도 LoginTheme composable로 추출
- Scaffold 추가

### Option C — Pragmatic Balance (Selected)
- LoginScreen을 `DiaryAppTheme(darkTheme = true)`로 감싸 항상 다크 모드 강제
- `Scaffold` + `TopAppBar` 구조로 변경 (SignUpScreen과 동일)
- TopAppBar title = "내 일기장" (로그인 화면 제목)
- 기존 Column 내용 → Scaffold content block 안으로 이동
- 지문인증: `LaunchedEffect(showBiometric)` 로 화면 진입 시 자동 실행

## Implementation Spec

### LoginScreen.kt 변경사항

```
@OptIn(ExperimentalMaterial3Api::class)
fun LoginScreen(...) {
  DiaryAppTheme(darkTheme = true) {      // ① 다크 모드 강제
    Scaffold(
      topBar = {
        TopAppBar(title = { Text("내 일기장") })  // ② SignUpScreen과 동일 구조
      }
    ) { padding ->
      Column(padding 적용) {
        ...기존 입력 필드 및 버튼...
      }
    }
  }
  
  // ③ 지문인증 default: 화면 진입 시 자동 실행
  LaunchedEffect(showBiometric) {
    if (showBiometric) biometricPrompt.authenticate(promptInfo)
  }
}
```

### 지문 버튼 배치
- `showBiometric` 조건 유지 (canUseBiometric && hasBiometricCredentials)
- 자격증명 없으면 이메일/비밀번호 폼으로 진행 (fallback 보장)
- 버튼은 로그인 버튼 아래 OutlinedButton으로 유지

## Files Changed
- `ui/auth/LoginScreen.kt` — 다크 테마 래핑 + Scaffold 구조 + LaunchedEffect 자동 실행
