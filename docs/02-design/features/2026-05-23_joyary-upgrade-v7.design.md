# joyary-upgrade-v7 Design

> **Phase**: Design
> **Date**: 2026-05-23
> **Feature**: joyary-upgrade-v7
> **Architecture**: Option C — Pragmatic Balance

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 이미지 스와이프 탐색 미지원 + 캐시 무기한 보관 |
| **WHO** | 조이어리 일상 사용자 |
| **RISK** | Pager 상태와 overlay 닫기 충돌; TTL 만료 체크 오버헤드 |
| **SUCCESS** | 스와이프 동작 + 인디케이터 + 24h TTL + 자동 정리 |
| **SCOPE** | DiaryDetailScreen.kt, DiaryViewModel.kt |

---

## 1. Architecture Decision

**Option C (Pragmatic)**: 기존 상태 변수 타입만 변경 + 래퍼 data class 추가.
- `selectedImageUrl: String?` → `selectedImageIndex: Int?` (null = 닫힘)
- `CachedValue<T>` data class를 ViewModel 파일 내부 private으로 선언
- HorizontalPager는 기존 overlay Box 안에 교체 삽입

라이브러리 추가 없음. 기존 ExperimentalFoundationApi 재사용.

---

## 2. DiaryViewModel Changes

### 2.1 CachedValue 래퍼

```kotlin
private data class CachedValue<T>(val data: T, val cachedAt: Long = System.currentTimeMillis())
private val TTL_MS = 24L * 60 * 60 * 1000  // 24시간

private val monthCache = mutableMapOf<String, CachedValue<List<DiaryEntry>>>()
private val entryCache = mutableMapOf<String, CachedValue<DiaryEntry?>>()
```

### 2.2 캐시 히트 조건 변경

```kotlin
// 기존: monthCache[key]?.let { cached -> ... }
// 변경: monthCache[key]?.takeIf { it.isExpired().not() }?.let { cached -> ... }

private fun CachedValue<*>.isExpired() = System.currentTimeMillis() - cachedAt >= TTL_MS
```

### 2.3 만료 캐시 lazy eviction

캐시 미스 발생 시점에 해당 키 제거 (별도 순회 불필요).
만료 키 발견 시 `remove()` 후 재조회.

### 2.4 init 블록 — 전체 만료 엔트리 일괄 정리

```kotlin
init {
    cleanupExpiredCache()
}

private fun cleanupExpiredCache() {
    val now = System.currentTimeMillis()
    monthCache.entries.removeAll { now - it.value.cachedAt >= TTL_MS }
    entryCache.entries.removeAll { now - it.value.cachedAt >= TTL_MS }
}
```

---

## 3. DiaryDetailScreen Changes

### 3.1 상태 변수

```kotlin
// 기존
var selectedImageUrl by remember { mutableStateOf<String?>(null) }

// 변경
var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
```

`overlayImages`는 현재 `entry?.imageUrls ?: emptyList()` 에서 파생.

### 3.2 onImageClick 시그니처 변경

```kotlin
// DiaryEntryContent 파라미터
onImageClick: (Int) -> Unit  // index 전달

// 호출부
.clickable { onImageClick(index) }  // items(entry.imageUrls).forEachIndexed
```

`items(entry.imageUrls)` 대신 `itemsIndexed(entry.imageUrls)` 사용.

### 3.3 오버레이 HorizontalPager 구조

```kotlin
selectedImageIndex?.let { startIndex ->
    val pagerState = rememberPagerState(initialPage = startIndex) { overlayImages.size }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = overlayImages[page],
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { }  // 이미지 영역 클릭은 무시 (닫기 버튼으로만 닫기)
            )
        }
        
        // 닫기 버튼 (TopEnd)
        IconButton(
            onClick = { selectedImageIndex = null },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Close, "닫기", tint = Color.White)
        }
        
        // 이미지 수가 2장 이상일 때만 인디케이터 표시
        if (overlayImages.size > 1) {
            Text(
                text = "${pagerState.currentPage + 1} / ${overlayImages.size}",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}
```

**배경 클릭으로 닫기 제거**: 스와이프 제스처와 충돌하므로 X 버튼으로만 닫기.

---

## 4. Success Criteria Mapping

| SC | 구현 위치 |
|----|---------|
| SC-01 | DiaryDetailScreen — HorizontalPager in overlay |
| SC-02 | DiaryDetailScreen — Text indicator BottomCenter |
| SC-03 | DiaryViewModel — monthCache TTL check |
| SC-04 | DiaryViewModel — entryCache TTL check |
| SC-05 | DiaryViewModel — lazy eviction on miss |
| SC-06 | DiaryViewModel — init { cleanupExpiredCache() } |
| SC-07 | Both files — no removal of existing logic |
