# Report: login-screen-scroll-title

**완료일**: 2026-05-31  
**Match Rate**: 100% ✅  
**변경 파일**: 1개 | **변경 라인**: 5줄

---

## 수정 내용

### CHANGE-01: 로그인 화면 제목 변경

**파일**: `ui/auth/LoginScreen.kt:101`

| | Before | After |
|--|--------|-------|
| TopAppBar 제목 | `"내 일기장"` | `"조이어리"` |

---

### CHANGE-02: 키보드 대응 스크롤 추가

**파일**: `ui/auth/LoginScreen.kt`

**추가된 import:**
```kotlin
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
```

**Column modifier 변경:**
```kotlin
// Before
modifier = Modifier
    .fillMaxSize()
    .padding(padding)
    .padding(horizontal = 32.dp, vertical = 16.dp),
verticalArrangement = Arrangement.Center,

// After
modifier = Modifier
    .fillMaxSize()
    .padding(padding)
    .imePadding()
    .verticalScroll(rememberScrollState())
    .padding(horizontal = 32.dp, vertical = 16.dp),
```

**패딩 체인 설명:**
1. `.padding(padding)` — Scaffold TopAppBar 높이 보존
2. `.imePadding()` — 키보드 등장 시 하단 공간 확보
3. `.verticalScroll(rememberScrollState())` — 좁아진 공간에서 스크롤 활성화
4. `.padding(horizontal/vertical)` — 내부 콘텐츠 여백

`verticalArrangement = Arrangement.Center` 제거 — `verticalScroll` 적용 시 Column 높이가 무한대가 되어 Center 배치가 무의미함.

---

## 기존 기능 보존 확인

| 기능 | 상태 |
|------|------|
| 지문 로그인 (BiometricPrompt) | ✅ 유지 |
| 다크 모드 강제 적용 | ✅ 유지 |
| 이메일/비밀번호 로그인 | ✅ 유지 |
| 회원가입 이동 버튼 | ✅ 유지 |
| 오류 메시지 표시 | ✅ 유지 |
