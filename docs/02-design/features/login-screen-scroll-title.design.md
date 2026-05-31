# Design: login-screen-scroll-title

## Architecture: Option C — Pragmatic Balance

### 변경 사항

#### 1. 제목 변경
```kotlin
// Before
TopAppBar(title = { Text("내 일기장") })
// After
TopAppBar(title = { Text("조이어리") })
```

#### 2. 스크롤 패턴 (imePadding + verticalScroll)

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(padding)       // Scaffold insets
        .imePadding()           // 키보드 높이만큼 하단 패딩
        .verticalScroll(rememberScrollState())  // 스크롤 가능
        .padding(horizontal = 32.dp, vertical = 16.dp),  // 내부 패딩
    horizontalAlignment = Alignment.CenterHorizontally
)
```

**순서 설명:**
- `padding(padding)` → Scaffold topBar 높이 확보
- `imePadding()` → 키보드 등장 시 Column 하단이 키보드 위까지 줄어듦
- `verticalScroll(...)` → 줄어든 공간에서 스크롤 활성화
- `padding(horizontal, vertical)` → 내부 콘텐츠 여백

**verticalArrangement 제거 이유:**
`verticalScroll` 적용 시 Column은 무한 높이를 가지므로 `Arrangement.Center`가 시각적 효과 없음. 제거하여 콘텐츠가 상단부터 배치되고 스크롤로 탐색.

#### 3. 신규 import
```kotlin
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
```

[CP-3 Auto] Option C — Pragmatic Balance 선택됨
