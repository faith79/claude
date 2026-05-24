# Design: joey-auto-push-fix

## Architecture: Option C — Pragmatic Balance

### FR-01: 저작권 텍스트
- `SettingsScreen.kt` 앱 정보 Card에 "© 2026 조이어리" 한 줄 추가

### FR-02: 버전 텍스트 탭 → 클립보드 복사
- `ClipboardManager` 사용 (LocalContext → getSystemService)
- 복사 완료 시 Toast 또는 `snackbarHostState.showSnackbar` (간단하게 Toast)
- `Row(modifier = Modifier.clickable { copy() })`
