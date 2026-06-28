# Report: joyary-error-fix

**완료일**: 2026-05-31  
**Match Rate**: 100% ✅  
**변경 파일**: 2개 | **변경 라인**: 3줄

---

## 수정된 버그

### BUG-01: security-crypto 버전 불일치 (컴파일 오류)

**파일**: `gradle/libs.versions.toml`

| | Before | After |
|--|--------|-------|
| securityCrypto | `"1.0.0"` | `"1.1.0-alpha06"` |

**원인**: `CredentialStorage.kt`에서 `MasterKey.Builder` 사용 → `security-crypto:1.0.0`에는 없는 API → 빌드 실패.  
**수정**: 버전을 `1.1.0-alpha06`으로 업그레이드 → `MasterKey.Builder` 사용 가능.

---

### BUG-02: FragmentActivity 캐스팅 오류 (런타임 크래시)

**파일**: `MainActivity.kt`

| | Before | After |
|--|--------|-------|
| 기반 클래스 | `ComponentActivity` | `FragmentActivity` |
| import | `androidx.activity.ComponentActivity` | `androidx.fragment.app.FragmentActivity` |

**원인**:
- `BiometricPrompt` 생성자는 `FragmentActivity` 필수
- `LoginScreen.kt`에서 `LocalContext.current as FragmentActivity` 캐스팅
- `ComponentActivity`는 `FragmentActivity`의 **부모** 클래스 → 다운캐스팅 불가 → `ClassCastException`

**수정**: `MainActivity`가 `FragmentActivity`를 상속 → `IS-A FragmentActivity` 성립 → 캐스팅 성공.

**왜 `AppCompatActivity`가 아닌가**: 현재 테마가 `android:Theme.Material.Light.NoActionBar` (비-AppCompat) → `AppCompatActivity` 사용 시 크래시. `FragmentActivity`는 테마 제약 없음.

---

## 기존 기능 보존 확인

| 기능 | 상태 |
|------|------|
| 강제 로그인 (signOutImmediate in onCreate) | ✅ 유지 |
| 항상 Login 화면 시작 | ✅ 유지 |
| 지문 로그인 (BiometricPrompt) | ✅ 이제 정상 동작 |
| enableEdgeToEdge() | ✅ FragmentActivity → ComponentActivity 확장함수 동작 |
| Hilt @AndroidEntryPoint | ✅ FragmentActivity 지원 |
| EncryptedSharedPreferences | ✅ 1.1.0-alpha06에서 정상 동작 |
