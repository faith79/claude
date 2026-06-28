# Design: joyary-diary-style-fix (v2)

## Architecture: Option C — Pragmatic Balance

외부 라이브러리 없이 Compose 기본 API만으로 두 이슈를 최소 변경으로 해결.

---

## SC-01: DiaryEditorScreen 배경색 수정

### 원인 분석
```
DiaryDetailScreen (OUTER Scaffold)  → containerColor = diaryBg (크림, 밝음)
  └── DiaryPageContent (INNER Scaffold) → containerColor 없음 → MaterialTheme.colorScheme.background
        다크모드: Color(0xFF121C28) 표시 ← 사용자가 보는 어두운 배경

DiaryEditorScreen (단일 Scaffold) → containerColor = diaryBg (크림, 밝음) ← 문제
  다크모드에서도 크림색 유지 → 상세보기와 불일치
```

### 해결책
`DiaryEditorScreen.kt` 에서:
1. `val diaryBg = LocalThemeColors.current.diaryBg` 삭제
2. `containerColor = diaryBg,` 삭제 → Scaffold가 MaterialTheme.colorScheme.background 자동 사용
3. OutlinedTextField의 `colors = OutlinedTextFieldDefaults.colors(...)` 블록 삭제 → 텍스트 색상 테마 자동 적용
4. 미사용 import 정리: `LocalThemeColors`, `Color`

---

## SC-02: SettingsScreen 스크롤바

### 구조

```kotlin
Scaffold { padding ->
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .drawWithContent {           // 뷰포트 좌표계에서 스크롤바 렌더링
                drawContent()
                val max = scrollState.maxValue
                if (max > 0) {
                    val ratio   = size.height / (size.height + max)
                    val thumbH  = size.height * ratio
                    val thumbY  = (size.height - thumbH) * (scrollState.value.toFloat() / max)
                    val alpha   = if (scrollState.isScrollInProgress) 0.7f else 0.3f
                    drawRect(
                        color = Color.Gray,
                        alpha = alpha,
                        topLeft = Offset(size.width - 4.dp.toPx(), thumbY),
                        size = Size(4.dp.toPx(), thumbH)
                    )
                }
            }
            .verticalScroll(scrollState) // drawWithContent 이후 배치 → 콘텐츠와 같이 안 스크롤됨
            .padding(16.dp)
    ) {
        // 기존 콘텐츠 그대로
    }
}
```

### 필요 import 추가 (SettingsScreen.kt)
```kotlin
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
```

---

## 변경 파일 요약

| 파일 | 변경 유형 | 라인 수 |
|------|-----------|---------|
| DiaryEditorScreen.kt | 삭제 (3라인) + import 정리 | -5 |
| SettingsScreen.kt | 추가 (스크롤 + 스크롤바) | +12 |
