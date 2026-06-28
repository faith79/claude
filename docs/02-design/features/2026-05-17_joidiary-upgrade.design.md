# JoiDiary Upgrade Design Document

> **Summary**: 기존 Diary App v0.1.0을 "조이어리" v0.2.0으로 업그레이드 — 날씨, 다중 이미지, Upsert, 스와이프 달력, 파스텔 디자인, Storage 연동 삭제, Lottie 로딩
>
> **Project**: claude / diary-app
> **Version**: 0.2.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-17
> **Status**: Draft
> **Planning Doc**: [joidiary-upgrade.plan.md](../01-plan/features/joidiary-upgrade.plan.md)
> **Architecture**: Option C — Pragmatic Balance

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 기존 v0.1.0 기능의 UX 마찰을 제거하고, 날씨·이미지 강화로 일상 기록의 완성도를 높인다 |
| **WHO** | 기존 Diary App 사용자 — 더 빠르고 직관적인 일기 작성 경험을 원하는 Android 사용자 |
| **RISK** | 데이터 모델 변경(title 제거·weather 추가)으로 기존 Firestore 문서와 호환성 문제 발생 가능 |
| **SUCCESS** | SC-01~SC-11 모두 에뮬레이터 동작 확인, 기존 데이터(v0.1.0) 정상 표시 |
| **SCOPE** | Android 앱 레이어만 변경 — Firebase 스키마 하위 호환 유지, 신규 필드는 nullable로 추가 |

---

## 1. Overview

### 1.1 Design Goals

- 기존 MVVM + Hilt + Firebase 구조를 그대로 유지하면서 최소 리팩토링으로 11개 변경 항목 적용
- 신규 책임(이미지 압축, 날씨 선택 UI, 로딩 오버레이)만 별도 파일로 분리
- Firestore 기존 문서와 100% 하위 호환 유지

### 1.2 Design Principles

- **기존 패턴 준수**: StateFlow + ViewModel + Repository 레이어 유지
- **최소 신규 파일**: 책임이 명확한 것만 분리 (6개 신규, 10개 수정)
- **하위 호환 우선**: null-safe fallback으로 v0.1.0 데이터 안전 처리

---

## 2. Architecture

### 2.0 Architecture Comparison (Selected: Option C)

| 기준 | Option A | Option B | **Option C ✅** |
|------|:--------:|:--------:|:---------------:|
| 신규 파일 | 3 | 10+ | **6** |
| 수정 파일 | 11 | 8 | **10** |
| ViewModel 부담 | 높음 | 낮음 | **중간** |
| 기존 구조 호환 | 높음 | 낮음 | **높음** |
| 구현 리스크 | 낮음 | 높음 | **낮음** |

**선택 근거**: 기존 v0.1.0 패턴을 최대한 살리면서 복잡한 신규 책임(이미지 압축, 다중 삭제)만 분리

### 2.1 Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    조이어리 v0.2.0                           │
├────────────────────────┬────────────────────────────────────┤
│   UI Layer (Compose)   │         Data Layer                 │
│                        │                                    │
│  HomeScreen            │  DiaryViewModel                   │
│   └─ CalendarSwipe     │   ├─ upsertCheck()                 │
│  DiaryEditorScreen     │   ├─ compressAndUpload()           │
│   ├─ WeatherSelector   │   ├─ deleteWithStorage()           │
│   ├─ MultiImagePicker  │   └─ loadingState: StateFlow       │
│   └─ LoadingOverlay    │                                    │
│  DiaryDetailScreen     │  DiaryRepository                  │
│  Theme (Color/Type)    │   └─ DiaryRepositoryImpl          │
│                        │                                    │
│                        │  StorageDataSource                 │
│                        │   ├─ uploadImages(list)            │
│                        │   ├─ deleteImage(url)              │
│                        │   └─ deleteImages(urls)            │
│                        │                                    │
│                        │  ImageCompressor                   │
│                        │   └─ compress(uri): ByteArray      │
├────────────────────────┴────────────────────────────────────┤
│                  Firebase BaaS                              │
│          Auth        Firestore       Storage                │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Data Flow

**일기 저장 흐름 (신규)**
```
사용자 입력(content+emotion+weather+images)
  → DiaryEditorScreen
  → DiaryViewModel.saveDiary()
    → ImageCompressor.compress(uri) × N장  [1MB 초과 시]
    → StorageDataSource.uploadImages(byteArrays)  → Firebase Storage
    → DiaryRepositoryImpl.saveDiary(entry)         → Firestore
  → LoadingOverlay 표시/숨김 (isLoading StateFlow)
```

**일기 삭제 흐름 (신규)**
```
삭제 확인 다이얼로그 OK
  → DiaryViewModel.deleteDiary(entry)
    → DiaryRepositoryImpl.delete(entry.id)   → Firestore 삭제
    → [성공 시] StorageDataSource.deleteImages(entry.imageUrls)
    → [실패 시] Snackbar 오류 안내
```

**Upsert 흐름 (신규)**
```
HomeScreen 날짜 선택 → "신규 작성" 클릭
  → DiaryViewModel.checkDateEntry(date)
    → [일기 있음] NavController → DiaryEditorScreen(mode=EDIT, entry)
    → [일기 없음] NavController → DiaryEditorScreen(mode=CREATE, date)
```

### 2.3 Dependencies

| 컴포넌트 | 의존 | 목적 |
|---------|------|------|
| `DiaryEditorScreen` | `WeatherSelector`, `MultiImagePicker`, `LoadingOverlay` | UI 조합 |
| `DiaryViewModel` | `DiaryRepository`, `StorageDataSource`, `ImageCompressor` | 비즈니스 로직 |
| `StorageDataSource` | Firebase Storage SDK | 업로드/삭제 |
| `ImageCompressor` | Android `Bitmap` API | 1MB 압축 |
| `WeatherSelector` | `WeatherTag` enum | 날씨 UI |

---

## 3. Data Model

### 3.1 DiaryEntry (v0.2.0)

```kotlin
data class DiaryEntry(
    val id: String = "",
    val userId: String = "",
    // title 제거 (v0.1.0 Firestore 문서 호환 — 앱에서 무시)
    val content: String = "",
    val date: String = "",              // "yyyy-MM-dd"
    val emotion: EmotionTag? = null,
    val weather: WeatherTag? = null,    // 신규 — null이면 UI에서 미표시
    val imageUrls: List<String> = emptyList(), // 신규 — 최대 3장
    // 하위호환: Firestore에 imageUrl 필드가 있으면 imageUrls[0]으로 읽기
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
```

### 3.2 WeatherTag (신규)

```kotlin
enum class WeatherTag(val emoji: String, val label: String) {
    SUNNY("☀️", "맑음"),
    PARTLY_CLOUDY("⛅", "구름조금"),
    CLOUDY("☁️", "흐림"),
    RAINY("🌧️", "비"),
    SNOWY("❄️", "눈")
}
```

### 3.3 Firestore Document Schema (v0.2.0)

| 필드 | 타입 | 필수 | 변경 |
|------|------|:----:|------|
| `id` | String | ✅ | 유지 |
| `userId` | String | ✅ | 유지 |
| `content` | String | ✅ | 유지 |
| `date` | String | ✅ | 유지 |
| `emotion` | String? | - | 유지 |
| `weather` | String? | - | **신규** (nullable) |
| `imageUrls` | Array<String> | - | **신규** (기본값 []) |
| `createdAt` | Long | ✅ | 유지 |
| `updatedAt` | Long | ✅ | 유지 |
| `title` | String? | - | 읽기 무시 (레거시) |
| `imageUrl` | String? | - | 읽기 fallback (레거시) |

### 3.4 하위 호환 Firestore 매핑

```kotlin
// DiaryRepositoryImpl.kt — Firestore 문서 읽기 시
fun DocumentSnapshot.toDiaryEntry(): DiaryEntry {
    val legacyImageUrl = getString("imageUrl")
    val imageUrls = get("imageUrls") as? List<String> ?: emptyList()

    return DiaryEntry(
        id = id,
        content = getString("content") ?: "",
        // title 필드는 읽더라도 사용하지 않음
        weather = getString("weather")?.let { WeatherTag.valueOf(it) },
        imageUrls = imageUrls.ifEmpty {
            legacyImageUrl?.let { listOf(it) } ?: emptyList()
        },
        ...
    )
}
```

---

## 4. 주요 컴포넌트 설계

### 4.1 ImageCompressor (신규)

**파일**: `data/util/ImageCompressor.kt`

```kotlin
class ImageCompressor @Inject constructor(
    private val context: Context
) {
    // 1MB = 1_048_576 bytes
    fun compress(uri: Uri, maxSizeBytes: Long = 1_048_576): ByteArray {
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        var quality = 90
        var output: ByteArray
        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            output = stream.toByteArray()
            quality -= 10
        } while (output.size > maxSizeBytes && quality > 10)
        return output
    }
}
```

### 4.2 StorageDataSource (수정)

**신규 메서드 추가**:

```kotlin
// 다중 업로드
suspend fun uploadImages(userId: String, byteArrays: List<ByteArray>): List<String>

// 단일 삭제 (URL 기반)
suspend fun deleteImage(imageUrl: String): Result<Unit>

// 다중 삭제 (URL 목록 기반)
suspend fun deleteImages(imageUrls: List<String>): Result<Unit> {
    return imageUrls.map { deleteImage(it) }
        .firstOrNull { it.isFailure } ?: Result.success(Unit)
}
```

### 4.3 DiaryViewModel (수정)

**신규 상태 및 메서드**:

```kotlin
// 로딩 상태
private val _isLoading = MutableStateFlow(false)
val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

// Upsert 체크
suspend fun checkAndNavigate(date: String): DiaryEntry?

// 저장 (압축 + 업로드 통합)
fun saveDiary(entry: DiaryEntry, newImageUris: List<Uri>, removedImageUrls: List<String>)

// 삭제 (Firestore + Storage 연동)
fun deleteDiary(entry: DiaryEntry)

// 수정 중 이미지 개별 삭제
fun removeImage(imageUrl: String)
```

### 4.4 WeatherSelector (신규 Composable)

**파일**: `ui/components/WeatherSelector.kt`

```
┌──────────────────────────────────────────────┐
│  날씨                                         │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐         │
│  │ ☀️ │ │ ⛅ │ │ ☁️ │ │🌧️ │ │ ❄️ │         │
│  │맑음│ │구름│ │흐림│ │ 비 │ │ 눈 │         │
│  └────┘ └────┘ └────┘ └────┘ └────┘         │
│    선택된 칩은 파스텔 배경으로 강조            │
└──────────────────────────────────────────────┘
```

```kotlin
@Composable
fun WeatherSelector(
    selected: WeatherTag?,
    onSelect: (WeatherTag) -> Unit,
    modifier: Modifier = Modifier
)
```

### 4.5 MultiImagePicker (신규 Composable)

**파일**: `ui/components/MultiImagePicker.kt`

```
┌─────────────────────────────────────────────┐
│  사진 (최대 3장)                             │
│  ┌──────┐  ┌──────┐  ┌──────┐              │
│  │ 사진1 │  │ 사진2 │  │  +   │              │
│  │  [X] │  │  [X] │  │ 추가  │              │
│  └──────┘  └──────┘  └──────┘              │
│  (3장 선택 시 "+" 버튼 숨김)                 │
└─────────────────────────────────────────────┘
```

```kotlin
@Composable
fun MultiImagePicker(
    imageUrls: List<String>,       // 기존 업로드된 URL
    newImageUris: List<Uri>,       // 새로 선택한 Uri
    onAddImages: (List<Uri>) -> Unit,
    onRemoveExisting: (String) -> Unit,  // Storage 삭제 트리거
    onRemoveNew: (Uri) -> Unit,
    maxCount: Int = 3
)
```

### 4.6 LoadingOverlay (신규 Composable)

**파일**: `ui/components/LoadingOverlay.kt`

```
┌─────────────────────────────────────────────┐
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   │  ← 반투명 딤 배경 (scrim)
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   │
│  ░░░░░░░░░░┌─────────────┐░░░░░░░░░░░░░░   │
│  ░░░░░░░░░░│  [Lottie]   │░░░░░░░░░░░░░░   │  ← 중앙 카드
│  ░░░░░░░░░░│  저장 중... │░░░░░░░░░░░░░░   │
│  ░░░░░░░░░░└─────────────┘░░░░░░░░░░░░░░   │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   │
└─────────────────────────────────────────────┘
```

```kotlin
@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    message: String = "저장 중...",
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))) {
        Card(modifier = Modifier.align(Alignment.Center)) {
            LottieAnimation(composition = ..., iterations = LottieConstants.IterateForever)
            Text(message)
        }
    }
}
```

**Lottie 애니메이션 추천**: `assets/loading.json`
- 사용처: [LottieFiles - Loading](https://lottiefiles.com/search?q=loading&category=animations) 에서 파스텔 톤 심플 로딩 선택
- 파일 크기 30KB 이하 권장

---

## 5. UI/UX Design

### 5.1 파스텔 디자인 토큰 (SC-07)

**파일**: `ui/theme/Color.kt`

```kotlin
// 파스텔 피치/코랄/연핑크 계열
val PastelPeach      = Color(0xFFFFCBA4)  // Primary
val PastelCoral      = Color(0xFFFF8B7E)  // Primary variant
val PastelPink       = Color(0xFFFFC5D0)  // Secondary
val PastelLavender   = Color(0xFFE8D5F5)  // Tertiary
val PastelSurface    = Color(0xFFFFF5F0)  // Surface
val PastelBackground = Color(0xFFFFFAF8)  // Background
val OnPastelPrimary  = Color(0xFF5A2A1A)  // 텍스트 (어두운 브라운)
val PastelError      = Color(0xFFE57373)  // Error

// Material 3 ColorScheme Seed = PastelPeach
val LightColorScheme = lightColorScheme(
    primary = PastelCoral,
    onPrimary = Color.White,
    primaryContainer = PastelPeach,
    secondary = PastelPink,
    surface = PastelSurface,
    background = PastelBackground,
    ...
)
```

### 5.2 달력 스와이프 구현 (SC-06)

**HomeScreen.kt — HorizontalPager 방식**:

```kotlin
// 현재: 화살표 버튼으로만 월 이동
// 변경: HorizontalPager + 화살표 버튼 모두 지원

val pagerState = rememberPagerState(initialPage = 500) // 500 = 현재 월 중심
HorizontalPager(
    count = 1000,
    state = pagerState,
    userScrollEnabled = true
) { page ->
    val monthOffset = page - 500
    CalendarContent(targetMonth = currentMonth.plusMonths(monthOffset.toLong()))
}
// 화살표 클릭 시 pagerState.animateScrollToPage(pagerState.currentPage ± 1)
```

### 5.3 Page UI Checklist

#### HomeScreen (달력 홈)

- [ ] 월/년 표시 헤더 텍스트
- [ ] 이전달 화살표 버튼 (`<`)
- [ ] 다음달 화살표 버튼 (`>`)
- [ ] 수평 스와이프로 월 이동 (HorizontalPager)
- [ ] 날짜 셀 — 일기 있는 날 감정 이모지 표시
- [ ] 날짜 셀 — 오늘 날짜 강조 표시
- [ ] 선택된 날짜 강조 표시
- [ ] "신규 작성" FAB 또는 버튼
- [ ] Upsert: 선택 날짜에 일기 있으면 수정 화면으로 이동

#### DiaryEditorScreen (작성/수정)

- [ ] 앱바 — "조이어리" 또는 날짜 제목
- [ ] 내용(content) TextField (title 없음)
- [ ] 감정 태그 선택 칩 (7개)
- [ ] 날씨 선택 칩 (☀️/⛅/☁️/🌧️/❄️) — 감정 태그 아래
- [ ] 이미지 선택 영역 — 현재 이미지 썸네일 + X 버튼
- [ ] 이미지 추가 버튼 (최대 3장 미만일 때만 표시)
- [ ] 저장 버튼
- [ ] LoadingOverlay — 저장 중 Lottie + 딤 배경

#### DiaryDetailScreen (상세)

- [ ] 날짜 표시
- [ ] 감정 태그 이모지+텍스트
- [ ] 날씨 태그 이모지+텍스트 (null이면 미표시)
- [ ] 이미지 — 좌우 16dp 여백 적용 (HorizontalPager or LazyRow)
- [ ] 내용 — Card(RoundedCornerShape(12dp)) 박스 안에 표시
- [ ] 수정 버튼
- [ ] 삭제 버튼 + 확인 다이얼로그

---

## 6. Error Handling

| 상황 | 처리 방법 |
|------|-----------|
| Firestore 저장 실패 | Snackbar "저장에 실패했습니다" + isLoading=false |
| Storage 업로드 실패 | Snackbar "이미지 업로드 실패" + 저장 롤백 |
| Firestore 삭제 실패 | Snackbar "삭제에 실패했습니다" (Storage 삭제 미진행) |
| Storage 삭제 실패 | Snackbar "이미지 정리 실패 (일기는 삭제됨)" + 로그 기록 |
| 이미지 압축 실패 | 원본 그대로 업로드 시도 (용량 경고 로그) |
| 날씨 null (레거시 데이터) | 날씨 영역 미표시 (graceful hide) |
| imageUrls 빈 배열 + imageUrl 존재 | imageUrls = [imageUrl] fallback |

---

## 7. Security Considerations

- Firebase Firestore Security Rules: `userId == request.auth.uid` 유지 (변경 없음)
- Storage Rules: 이미지 경로 `images/{userId}/` — 본인 폴더만 접근 가능
- Storage 삭제: URL에서 Storage 경로 추출 시 본인 userId 경로인지 검증

---

## 8. Test Plan

### 8.1 Test Scope

| 타입 | 대상 | 도구 | 단계 |
|------|------|------|------|
| L1: 단위 테스트 | `ImageCompressor`, `DiaryRepositoryImpl` fallback 로직 | JUnit | Do |
| L2: 통합 테스트 | ViewModel Upsert/삭제 흐름 | JUnit + Hilt | Do |
| L3: UI 테스트 | EditorScreen, DetailScreen 컴포저블 | Compose UI Test | Do |

### 8.2 핵심 테스트 시나리오

| # | 시나리오 | 기대 결과 |
|---|----------|-----------|
| T-01 | 이미지 2MB 입력 → 압축 → 1MB 이하 출력 | `output.size <= 1_048_576` |
| T-02 | v0.1.0 Firestore 문서 읽기 (imageUrl 필드) | `imageUrls == [imageUrl]` |
| T-03 | 날씨 null 문서 읽기 | `weather == null`, 날씨 UI 미표시 |
| T-04 | 날짜 선택 — 일기 있음 → Upsert | 수정 화면 navigate |
| T-05 | 날짜 선택 — 일기 없음 | 신규 작성 화면 navigate |
| T-06 | 일기 삭제 — Firestore 성공 → Storage 삭제 | 이미지 파일 삭제 확인 |
| T-07 | 일기 삭제 — Firestore 실패 | Storage 삭제 미실행 |
| T-08 | 수정 화면 이미지 X 클릭 | Storage 즉시 삭제 + UI에서 제거 |
| T-09 | 3장 이미지 선택 | "+" 버튼 숨김 |
| T-10 | 저장 클릭 → isLoading=true | LoadingOverlay 표시 |

---

## 9. Clean Architecture (Android MVVM)

### 9.1 Layer Structure

| 레이어 | 책임 | 위치 |
|--------|------|------|
| **Presentation** | Composable UI, ViewModel | `ui/`, `viewmodel/` |
| **Domain** | Entity, Enum | `data/model/` |
| **Data** | Repository, DataSource | `data/repository/`, `data/source/` |
| **Util** | ImageCompressor | `data/util/` |

### 9.2 This Feature's Layer Assignment

| 컴포넌트 | 레이어 | 경로 |
|---------|--------|------|
| `DiaryEditorScreen.kt` | Presentation | `ui/diary/` |
| `WeatherSelector.kt` | Presentation | `ui/components/` |
| `MultiImagePicker.kt` | Presentation | `ui/components/` |
| `LoadingOverlay.kt` | Presentation | `ui/components/` |
| `HomeScreen.kt` | Presentation | `ui/home/` |
| `DiaryDetailScreen.kt` | Presentation | `ui/diary/` |
| `DiaryViewModel.kt` | Presentation | `viewmodel/` |
| `DiaryEntry.kt` | Domain | `data/model/` |
| `WeatherTag.kt` | Domain | `data/model/` |
| `DiaryRepositoryImpl.kt` | Data | `data/repository/` |
| `StorageDataSource.kt` | Data | `data/source/` |
| `ImageCompressor.kt` | Util | `data/util/` |

---

## 10. Coding Convention Reference

기존 v0.1.0 컨벤션 유지:

| 항목 | 규칙 | 예시 |
|------|------|------|
| Composable | PascalCase | `WeatherSelector`, `LoadingOverlay` |
| ViewModel 함수 | camelCase | `saveDiary()`, `deleteDiary()` |
| StateFlow 이름 | `_` prefix + 공개 val | `_isLoading` / `isLoading` |
| Hilt 주입 | `@Inject constructor` | `class ImageCompressor @Inject constructor(...)` |
| 파일 위치 | 기능별 패키지 | `ui/components/`, `data/util/` |

---

## 11. Implementation Guide

### 11.1 File Structure

```
diary-app/app/src/main/java/com/example/diaryapp/
├── data/
│   ├── model/
│   │   ├── DiaryEntry.kt          ← 수정 (title 제거, weather/imageUrls 추가)
│   │   ├── EmotionTag.kt          ← 유지
│   │   └── WeatherTag.kt          ← 신규
│   ├── repository/
│   │   ├── DiaryRepository.kt     ← 수정 (인터페이스 메서드 추가)
│   │   └── DiaryRepositoryImpl.kt ← 수정 (fallback 로직, 삭제 연동)
│   ├── source/
│   │   ├── FirestoreDataSource.kt ← 수정 (날씨, imageUrls 필드 처리)
│   │   └── StorageDataSource.kt   ← 수정 (uploadImages, deleteImage, deleteImages)
│   └── util/
│       └── ImageCompressor.kt     ← 신규
├── ui/
│   ├── components/
│   │   ├── WeatherSelector.kt     ← 신규
│   │   ├── MultiImagePicker.kt    ← 신규
│   │   └── LoadingOverlay.kt      ← 신규
│   ├── diary/
│   │   ├── DiaryEditorScreen.kt   ← 수정 (title 제거, 날씨/이미지 추가, 로딩)
│   │   └── DiaryDetailScreen.kt   ← 수정 (여백, Card, imageUrls, 날씨 표시)
│   ├── home/
│   │   └── HomeScreen.kt          ← 수정 (HorizontalPager 스와이프)
│   └── theme/
│       ├── Color.kt               ← 수정 (파스텔 팔레트)
│       └── Theme.kt               ← 수정 (LightColorScheme seed 교체)
├── viewmodel/
│   └── DiaryViewModel.kt          ← 수정 (압축, Upsert, 삭제, 로딩상태)
└── di/
    └── DataSourceModule.kt        ← 수정 (ImageCompressor Hilt 바인딩)

diary-app/app/src/main/assets/
└── loading.json                   ← 신규 (Lottie 애니메이션)

diary-app/app/build.gradle.kts    ← 수정 (lottie-compose 의존성)
diary-app/app/src/main/res/values/strings.xml ← 수정 (app_name = "조이어리")
diary-app/app/src/main/AndroidManifest.xml    ← 수정 (label 확인)
```

### 11.2 Implementation Order

```
M1. 기반 모델 변경
    1. [ ] WeatherTag.kt 신규 생성
    2. [ ] DiaryEntry.kt 수정 (title 제거, weather, imageUrls)

M2. 데이터 레이어 확장
    3. [ ] StorageDataSource.kt — uploadImages(), deleteImage(), deleteImages()
    4. [ ] ImageCompressor.kt 신규 생성
    5. [ ] FirestoreDataSource.kt — weather, imageUrls 필드 처리
    6. [ ] DiaryRepositoryImpl.kt — fallback 로직 + 삭제 연동
    7. [ ] DataSourceModule.kt — ImageCompressor Hilt 바인딩

M3. ViewModel 로직 확장
    8. [ ] DiaryViewModel.kt — isLoading, Upsert check, saveDiary, deleteDiary, removeImage

M4. 공통 UI 컴포넌트
    9. [ ] LoadingOverlay.kt 신규 생성 (lottie-compose 추가 후)
   10. [ ] WeatherSelector.kt 신규 생성
   11. [ ] MultiImagePicker.kt 신규 생성

M5. 테마 변경
   12. [ ] Color.kt — 파스텔 팔레트 교체
   13. [ ] Theme.kt — LightColorScheme 업데이트

M6. 화면 수정
   14. [ ] DiaryEditorScreen.kt — title 제거, 날씨/이미지/로딩 추가
   15. [ ] DiaryDetailScreen.kt — 여백, Card, imageUrls, 날씨
   16. [ ] HomeScreen.kt — HorizontalPager 스와이프

M7. 앱 설정
   17. [ ] strings.xml — app_name = "조이어리"
   18. [ ] build.gradle.kts — lottie-compose 의존성
   19. [ ] assets/loading.json — Lottie 파일 배치
```

### 11.3 Session Guide

#### Module Map

| 모듈 | Scope Key | 설명 | 예상 턴 |
|------|-----------|------|:-------:|
| 기반 모델 + 데이터 레이어 | `module-1` | M1+M2+M3 — Model, DataSource, Repository, ViewModel | 20-25 |
| UI 컴포넌트 + 테마 | `module-2` | M4+M5 — LoadingOverlay, WeatherSelector, MultiImagePicker, Color | 15-20 |
| 화면 수정 + 설정 | `module-3` | M6+M7 — EditorScreen, DetailScreen, HomeScreen, 앱 이름 | 20-25 |

#### Recommended Session Plan

| 세션 | 단계 | Scope | 예상 턴 |
|------|------|-------|:-------:|
| Session 1 | Plan + Design | 전체 | 35-40 |
| Session 2 | Do | `--scope module-1` | 25-30 |
| Session 3 | Do | `--scope module-2` | 20-25 |
| Session 4 | Do | `--scope module-3` | 25-30 |
| Session 5 | Check + Report | 전체 | 20-25 |

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-17 | Initial draft — Option C 선택, SC-01~SC-11 설계 | faith79@jobkorea.co.kr |
