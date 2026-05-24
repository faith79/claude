# Design: skill-test-run

## Architecture: Option C — Pragmatic Balance

### FR-01/02/03: 앱 정보 섹션
- `SettingsScreen.kt` 수정만으로 완결
- `LocalContext.current` → `packageManager.getPackageInfo()` 로 버전 읽기
- 기존 "계정" 섹션 아래에 "앱 정보" Card 추가
- 표시: "조이어리 v{versionName} ({versionCode})"
