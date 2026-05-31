# Design: joyary-error-fix

## Architecture: Option C — Minimal Targeted Fix

각 버그에 대한 최소 변경만 적용. 기존 동작 로직 완전 유지.

---

## §1. BUG-01 수정: security-crypto 버전 업그레이드

**파일**: `gradle/libs.versions.toml`

```diff
- securityCrypto = "1.0.0"
+ securityCrypto = "1.1.0-alpha06"
```

### 왜 1.1.0-alpha06인가?
- `MasterKey` class: `1.1.0-alpha03` 에서 도입
- `MasterKey.Builder` API: `1.1.0-alpha03+` 필요
- `1.1.0-alpha06`은 현재 가장 최신 alpha (안정적 사용 확인됨)
- `CredentialStorage.kt`의 코드 변경 불필요 (API 이미 1.1.x 스타일로 작성됨)

---

## §2. BUG-02 수정: MainActivity 기반 클래스 변경

**파일**: `MainActivity.kt`

```diff
- class MainActivity : ComponentActivity() {
+ class MainActivity : FragmentActivity() {
```

**Import 변경**:
```diff
- import androidx.activity.ComponentActivity
+ import androidx.fragment.app.FragmentActivity
```

### 왜 FragmentActivity인가? (AppCompatActivity 가 아닌 이유)
- 현재 테마: `android:Theme.Material.Light.NoActionBar` (비-AppCompat 테마)
- `AppCompatActivity`는 AppCompat 계열 테마 필수 → 테마 변경 없으면 크래시
- `FragmentActivity`는 테마 제약 없음 → 현재 테마 그대로 유지
- `BiometricPrompt`는 `FragmentActivity`만 요구 → `FragmentActivity`로 충분
- `setContent {}`, `enableEdgeToEdge()`: `ComponentActivity` 확장 함수 → `FragmentActivity`(extends ComponentActivity) 에서 동일 동작

### 클래스 계층 확인
```
ComponentActivity (base)
    ↓ extends
FragmentActivity  ← 우리가 사용할 클래스
    ↓ extends  
AppCompatActivity
```

`FragmentActivity`는 `ComponentActivity`의 모든 기능 포함 + FragmentManager 추가 ✅

---

## §3. 변경 요약

| 파일 | 변경 라인 수 | 변경 내용 |
|------|------------|---------|
| `libs.versions.toml` | 1줄 | 버전 1.0.0 → 1.1.0-alpha06 |
| `MainActivity.kt` | 2줄 | import + extends 변경 |

**총 변경: 3줄 수정** — 최소 변경 원칙 준수
