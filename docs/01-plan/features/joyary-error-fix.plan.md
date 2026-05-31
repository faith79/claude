# Plan: joyary-error-fix

## Context Anchor
- **WHY**: joyary-login-biometric 세션에서 도입한 생체인증 코드에 컴파일/런타임 오류 2개
- **WHO**: 조이어리 앱 사용자 — 빌드 자체가 실패하므로 모든 사용자 영향
- **RISK**: 수정 과정에서 생체인증 동작 로직을 건드리지 않아야 함
- **SUCCESS**: `./gradlew assembleDebug` 성공 + 생체인증 기능 정상 동작
- **SCOPE**: 2개 파일만 수정 (libs.versions.toml, MainActivity.kt)

## Bugs Found

### BUG-01: security-crypto 버전 불일치 (컴파일 오류)
- **원인**: `security-crypto:1.0.0` 지정했으나 코드에서 `MasterKey.Builder` API 사용
- `MasterKey.Builder`는 `security-crypto:1.1.0-alpha03+` 에서만 제공
- **수정**: `securityCrypto = "1.0.0"` → `"1.1.0-alpha06"`

### BUG-02: FragmentActivity 캐스팅 오류 (런타임 ClassCastException)
- **원인**: `MainActivity : ComponentActivity()` 이지만 LoginScreen에서 `context as FragmentActivity` 캐스팅
- `ComponentActivity`는 `FragmentActivity`의 상위 클래스 (반대 방향 캐스팅 불가)
- **수정**: `MainActivity : ComponentActivity()` → `MainActivity : FragmentActivity()`
  - 현재 테마 `android:Theme.Material.Light.NoActionBar` 는 AppCompat 불필요 → `FragmentActivity` 안전
  - `FragmentActivity`는 hilt-navigation-compose 의 transitive dep 으로 이미 클래스패스 존재

## Files to Change
| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | `securityCrypto "1.0.0"` → `"1.1.0-alpha06"` |
| `MainActivity.kt` | extends `ComponentActivity` → `FragmentActivity`, import 수정 |

## [CP-1 Auto] 요구사항 확인됨
## [CP-2 Auto] 합리적 기본값 — 최소 변경, 기존 기능 유지
