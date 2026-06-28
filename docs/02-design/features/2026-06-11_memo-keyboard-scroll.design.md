# Design: memo-keyboard-scroll (Option C — Pragmatic Balance)

## Architecture Decision
`Modifier.imePadding()` + `verticalScroll` 조합 사용.
- `imePadding()`을 `verticalScroll` **앞**에 배치 → 스크롤 가능 영역이 키보드 높이만큼 줄어들고, 내부는 스크롤 가능

## Changes

### MemoEditorScreen (line 188–194)
```kotlin
// BEFORE
modifier = Modifier
    .fillMaxSize()
    .padding(padding)
    .verticalScroll(rememberScrollState())
    .padding(horizontal = 16.dp)
    .padding(bottom = 32.dp)

// AFTER
modifier = Modifier
    .fillMaxSize()
    .padding(padding)
    .imePadding()                          // ← 추가
    .verticalScroll(rememberScrollState())
    .padding(horizontal = 16.dp)
    .padding(bottom = 32.dp)
```

### MemoDetailScreen — TEXT 타입 Column (line 361–366)
```kotlin
// BEFORE
modifier = Modifier
    .fillMaxSize()
    .padding(padding)
    .verticalScroll(rememberScrollState())
    .padding(horizontal = 16.dp, vertical = 12.dp)

// AFTER
modifier = Modifier
    .fillMaxSize()
    .padding(padding)
    .imePadding()                          // ← 추가
    .verticalScroll(rememberScrollState())
    .padding(horizontal = 16.dp, vertical = 12.dp)
```

## Import
`imePadding()` is in `androidx.compose.foundation.layout.*` — already imported.
