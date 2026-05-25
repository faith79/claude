# Design: diary-bg-color-sync

## Architecture: Option C — Pragmatic Balance

### DiaryEditorScreen.kt 변경 전/후

**변경 전:**
```kotlin
// Design Ref: joyary-diary-style-fix §SC-01 — containerColor 제거, 시스템 테마 따라감
Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(topBar = { ... }) { padding ->
        val diaryBg = LocalThemeColors.current.diaryBg
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(diaryBg)  // ← Content 영역만 적용
                .padding(padding)
                ...
        )
    }
}
```

**변경 후:**
```kotlin
val diaryBg = LocalThemeColors.current.diaryBg  // ← Scaffold 이전으로 이동
Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = diaryBg,               // ← 읽기화면과 동일 패턴
        topBar = { ... }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // .background(diaryBg) 제거 — containerColor가 대신 처리
                .padding(padding)
                ...
        )
    }
}
```

### 읽기화면과의 일치 확인
```kotlin
// DiaryDetailScreen.kt (기준)
Scaffold(containerColor = diaryBg, ...) { ... }

// DiaryEditorScreen.kt (변경 후)
Scaffold(containerColor = diaryBg, ...) { ... }
```

동일 패턴 → 앱바 포함 전체 배경색 일치.

## 변경 파일
| File | 변경 유형 |
|------|-----------|
| `ui/diary/DiaryEditorScreen.kt` | 수정 |
