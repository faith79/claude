# joyary-upgrade-v8 Design

> **Phase**: Design | **Date**: 2026-05-23 | **Architecture**: Option C — Pragmatic Balance

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 앱 재시작마다 Firestore 재조회 → 달력 로딩 느림; 100KB 이미지 → 느린 표시 |
| **WHO** | 매일 사용하는 조이어리 사용자 |
| **RISK** | 파일 I/O 예외 처리; 직렬화 오류; Coil API 변경 |
| **SUCCESS** | 앱 재시작 후 달력 즉시 표시; 이미지 ≤30KB; Coil 50MB 디스크캐시 |
| **SCOPE** | DiaryLocalCache.kt(신규), DiaryViewModel.kt, ImageCompressor.kt, DiaryApp.kt |

---

## 1. Architecture Options

### Option A — Minimal
- SharedPreferences로 JSON 직렬화 → 단순하지만 대용량 데이터 부적합
- Coil 기본값 유지 → 반복 다운로드 미해결

### Option B — Clean
- Room DB L2 캐시 → 강력하지만 마이그레이션·보일러플레이트 다량
- Kotlin Serialization → 별도 의존성 추가 필요

### Option C — Pragmatic Balance ✅ (선택)
- `cacheDir` 파일 기반 L2 캐시 (OS가 저장 부족 시 자동 정리, 24h TTL)
- `org.json` (Android 내장) → 의존성 추가 없음
- Coil `ImageLoaderFactory` → 앱 레벨 설정으로 전역 적용

**선택 이유**: 신규 의존성 없음, Room 마이그레이션 불필요, 기존 아키텍처 최소 변경

---

## 2. Component Map

```
DiaryApp.kt
  └─ ImageLoaderFactory        (Coil 50MB disk + 25% memory)

DiaryViewModel.kt
  ├─ memMonthCache: Map<String, List<DiaryEntry>>   (L1 — 인메모리, 세션 한정)
  ├─ memEntryCache: Map<String, DiaryEntry?>         (L1 — 인메모리, 세션 한정)
  └─ DiaryLocalCache                                 (L2 — 디스크 24h TTL)

DiaryLocalCache.kt  [NEW]
  ├─ cacheDir/joyary_cache/month_<key>.json
  ├─ cacheDir/joyary_cache/entry_<key>.json
  └─ TTL 체크: file.lastModified() + 24h

ImageCompressor.kt
  ├─ maxSizeBytes = 30_720L  (30KB)
  ├─ maxDimensions = [640, 480, 320, 160]
  └─ startQuality = 75
```

---

## 3. DiaryLocalCache.kt (신규)

### 3.1 위치
`data/util/DiaryLocalCache.kt`

### 3.2 클래스 구조
```kotlin
@Singleton
class DiaryLocalCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cacheRoot = File(context.cacheDir, "joyary_cache")
    private val TTL_MS = 24L * 60 * 60 * 1000

    // 파일명 안전화: userId, date에 포함된 특수문자 제거
    private fun safeKey(key: String) = key.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")

    // Month cache
    fun getMonth(key: String): List<DiaryEntry>?   // null = 캐시 없음 or 만료
    fun putMonth(key: String, entries: List<DiaryEntry>)
    fun removeMonth(key: String)

    // Entry cache
    fun getEntry(key: String): Pair<Boolean, DiaryEntry?>?  // null = 없음, Pair(true, null) = 캐시된 null
    fun putEntry(key: String, entry: DiaryEntry?)
    fun removeEntry(key: String)

    // Maintenance
    fun cleanupExpired()
}
```

### 3.3 TTL 체크
- 파일 없음 → null 반환
- `System.currentTimeMillis() - file.lastModified() >= TTL_MS` → 파일 삭제 후 null 반환
- 유효 → 파일 읽기 후 역직렬화

### 3.4 직렬화 (org.json)

**DiaryEntry → JSONObject:**
```
id, userId, content, date, emotion(name or null), weather(name or null),
imageUrls(JSONArray), createdAt(Long), updatedAt(Long)
```

**Month 파일:** `{ "entries": [ {...}, ... ] }`

**Entry 파일:** `{ "hasValue": true/false, "entry": {...} | null }`
- `hasValue: false` → 해당 날짜 일기 없음(null) 이 캐시됨
- `hasValue: true, entry: null` → 불가 상태 (방어)

---

## 4. DiaryViewModel.kt 변경

### 4.1 제거할 코드
```kotlin
// 제거:
private data class CachedValue<T>(val data: T, val cachedAt: Long = ...)
private val TTL_MS = ...
private fun CachedValue<*>.isExpired() = ...
private val monthCache = mutableMapOf<String, CachedValue<...>>()
private val entryCache = mutableMapOf<String, CachedValue<...>>()
init { cleanupExpiredCache() }
private fun cleanupExpiredCache() { ... }
```

### 4.2 추가할 코드
```kotlin
// L1 메모리 캐시 (세션 내 즉시 조회)
private val memMonthCache = mutableMapOf<String, List<DiaryEntry>>()
private val memEntryCache = mutableMapOf<String, DiaryEntry?>()

// L2 주입 (생성자)
private val localCache: DiaryLocalCache

init {
    localCache.cleanupExpired()  // 시작 시 만료 파일 삭제
}
```

### 4.3 loadMonth 2단 캐시 로직
```
L1 hit? → _diaries.value = memMonthCache[key]; return
L2 hit? → memMonthCache[key] = list; _diaries.value = list; return
Firestore → collect → memMonthCache[key] = list; localCache.putMonth(key, list); _diaries.value = list
```

### 4.4 loadDiaryByDate 2단 캐시 로직
```
L1 hit? → _selectedEntry.value = memEntryCache[key]; return
L2 hit? → memEntryCache[key] = entry; _selectedEntry.value = entry; return
Firestore → result → memEntryCache[key] = result; localCache.putEntry(key, result); _selectedEntry.value = result
```

### 4.5 invalidateCache 변경
```kotlin
private fun invalidateCache(userId: String, date: String) {
    val yearMonth = date.substring(0, 7)
    val monthKey = "${userId}_${yearMonth}"
    val entryKey = "${userId}_${date}"
    // L1
    memMonthCache.remove(monthKey)
    memEntryCache.remove(entryKey)
    // L2
    localCache.removeMonth(monthKey)
    localCache.removeEntry(entryKey)
}
```

---

## 5. ImageCompressor.kt 변경

| 항목 | 현재값 | 변경값 |
|------|--------|--------|
| `maxSizeBytes` | `102_400L` (100KB) | `30_720L` (30KB) |
| `maxDimensions` | `listOf(1280, 800, 480, 240)` | `listOf(640, 480, 320, 160)` |
| `startQuality` | `85` | `75` |

로직 구조 (`calculateInSampleSize`, EXIF 회전, quality 루프 `>= 10`) 는 유지.

---

## 6. DiaryApp.kt 변경

### 6.1 Coil ImageLoaderFactory (2.6.0 API)
```kotlin
@HiltAndroidApp
class DiaryApp : Application(), Configuration.Provider, ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "coil_images"))
                    .maxSizeBytes(50L * 1024 * 1024)  // 50MB
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
```

### 6.2 필요한 imports
```kotlin
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import java.io.File
```

---

## 7. Hilt DI 변경

`DiaryLocalCache`는 `@Singleton` + `@Inject constructor` 이므로 별도 Module 불필요.

`DiaryViewModel` 생성자에 `DiaryLocalCache` 추가:
```kotlin
@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val imageCompressor: ImageCompressor,
    private val localCache: DiaryLocalCache      // 추가
) : ViewModel()
```

---

## 8. 파일 목록

| 파일 | 변경 유형 |
|------|----------|
| `data/util/DiaryLocalCache.kt` | 신규 생성 |
| `viewmodel/DiaryViewModel.kt` | 수정 (생성자, 캐시 로직) |
| `data/util/ImageCompressor.kt` | 수정 (3개 상수) |
| `DiaryApp.kt` | 수정 (ImageLoaderFactory 구현) |

---

## 9. Success Criteria 매핑

| SC | 설계 근거 |
|----|---------|
| SC-01 | DiaryLocalCache.kt 생성 — §3 |
| SC-02 | loadMonth L1→L2→Firestore — §4.3 |
| SC-03 | loadDiaryByDate L1→L2→Firestore — §4.4 |
| SC-04 | invalidateCache L1+L2 동시 — §4.5 |
| SC-05 | `init { localCache.cleanupExpired() }` — §4.2 |
| SC-06 | maxSizeBytes = 30_720L — §5 |
| SC-07 | maxDimensions [640,480,320,160] + quality=75 — §5 |
| SC-08 | Coil DiskCache 50MB — §6.1 |
| SC-09 | Coil MemoryCache 25% — §6.1 |
| SC-10 | v7 기능 회귀 없음 — HorizontalPager/TTL 구조 유지 |
