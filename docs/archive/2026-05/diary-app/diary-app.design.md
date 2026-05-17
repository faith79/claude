# diary-app Design Document

> **Summary**: Kotlin + Jetpack Compose + MVVM + Repository 패턴 기반 Android 일기 앱 설계
>
> **Project**: claude
> **Version**: 0.1.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-05
> **Status**: Draft
> **Planning Doc**: [diary-app.plan.md](../../01-plan/features/diary-app.plan.md)

---

## Context Anchor

> Copied from Plan document. Ensures strategic context survives Design→Do handoff.

| Key | Value |
|-----|-------|
| **WHY** | 개인 일기를 클라우드에 안전하게 저장하고 감정·이미지와 함께 기록하고 싶다 |
| **WHO** | 매일 일기를 쓰거나 감정을 기록하고 싶은 Android 사용자 |
| **RISK** | Firebase 비용(무료 한도 초과), 이미지 업로드 Storage 용량, 오프라인 동기화 복잡성 |
| **SUCCESS** | 로그인·로그아웃, 달력 UI + 감정 이모티콘 표시, 일기 CRUD, 감정 태그 저장, 이미지 첨부, 검색, 일일 알림 모두 동작 |
| **SCOPE** | Phase 1 — Auth + 일기 CRUD + 감정 태그 + 이미지 + 검색 + 알림 (Android 단독) |

---

## 1. Overview

### 1.1 Design Goals

- MVVM + Repository 패턴으로 UI/비즈니스 로직/데이터 레이어 명확히 분리
- Monthly Calendar UI를 CalendarViewModel로 독립 상태 관리
- Hilt DI로 DataSource → Repository → ViewModel 의존성 주입
- StateFlow + Compose로 반응형 UI 구성
- Firebase Auth/Firestore/Storage 각각을 별도 DataSource로 분리

### 1.2 Design Principles

- **단일 책임**: 각 클래스는 하나의 역할만 담당
- **인터페이스 분리**: Repository는 interface로 선언, Impl로 구현 (테스트 가능)
- **상태 불변성**: UiState는 sealed class / data class로 표현
- **Compose 친화**: StateFlow → collectAsStateWithLifecycle() 패턴 사용

---

## 2. Architecture

### 2.0 Selected: Option C — MVVM + Repository

| 기준 | 결정 | 근거 |
|------|------|------|
| Architecture | MVVM + Repository | Plan 선택사항, Compose와 궁합 |
| State | StateFlow + UiState sealed class | Compose 구독 용이 |
| DI | Hilt | Android 공식 권장 |
| Image Loading | Coil | Compose 친화적 |
| Notification | WorkManager | 배터리 최적화 |

### 2.1 레이어 구성

```
┌─────────────────────────────────────────────┐
│               UI Layer (Compose)             │
│  LoginScreen / SignUpScreen                  │
│  CalendarScreen (Monthly)                    │
│  DiaryEditorScreen / DiaryDetailScreen       │
└──────────────────┬──────────────────────────┘
                   │ collectAsStateWithLifecycle
┌──────────────────▼──────────────────────────┐
│            ViewModel Layer                   │
│  AuthViewModel                               │
│  CalendarViewModel  ← 달력 월 상태 관리        │
│  DiaryEditorViewModel                        │
└──────────────────┬──────────────────────────┘
                   │ suspend / Flow
┌──────────────────▼──────────────────────────┐
│           Repository Layer                   │
│  AuthRepository (interface + impl)           │
│  DiaryRepository (interface + impl)          │
└──────┬──────────────────┬───────────────────┘
       │                  │
┌──────▼───────┐  ┌───────▼──────────────────┐
│ AuthDataSource│  │  FirestoreDataSource      │
│ (Firebase Auth│  │  StorageDataSource        │
│  SDK 래핑)    │  │  (Firebase SDK 래핑)      │
└──────────────┘  └──────────────────────────┘
```

### 2.2 패키지 구조

```
app/src/main/java/com/example/diaryapp/
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   ├── SignUpScreen.kt
│   │   └── AuthUiState.kt
│   ├── home/
│   │   ├── CalendarScreen.kt       ← Monthly Calendar 메인 화면
│   │   ├── CalendarGrid.kt         ← 달력 그리드 Composable
│   │   ├── CalendarDayCell.kt      ← 날짜 셀 (감정 이모티콘 포함)
│   │   └── CalendarUiState.kt
│   ├── editor/
│   │   ├── DiaryEditorScreen.kt
│   │   ├── EmotionTagSelector.kt   ← 감정 태그 선택 Composable
│   │   └── EditorUiState.kt
│   ├── detail/
│   │   └── DiaryDetailScreen.kt
│   ├── search/
│   │   └── SearchScreen.kt            ← 검색 화면 (GAP-04 추가)
│   └── settings/
│       └── SettingsScreen.kt          ← 알림 시간 설정 화면 (GAP-02 추가)
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── CalendarViewModel.kt
│   ├── DiaryEditorViewModel.kt
│   ├── SearchViewModel.kt             ← 검색 상태 관리 (GAP-04 추가)
│   └── SettingsViewModel.kt           ← 알림 설정 상태 관리 (GAP-02 추가)
├── data/
│   ├── model/
│   │   ├── DiaryEntry.kt           ← Firestore 매핑 데이터 클래스
│   │   └── EmotionTag.kt           ← enum (HAPPY/SAD/ANGRY/CALM/EXCITED)
│   ├── repository/
│   │   ├── AuthRepository.kt       ← interface
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── DiaryRepository.kt      ← interface
│   │   └── DiaryRepositoryImpl.kt
│   └── source/
│       ├── AuthDataSource.kt
│       ├── FirestoreDataSource.kt
│       └── StorageDataSource.kt
├── notification/
│   ├── DailyReminderWorker.kt
│   └── NotificationPreferences.kt     ← SharedPreferences 래퍼 (GAP-02 추가)
├── di/
│   ├── RepositoryModule.kt
│   ├── DataSourceModule.kt
│   └── NotificationModule.kt          ← NotificationPreferences 제공 (GAP-02 추가)
├── DiaryApp.kt                        ← Application 클래스 (Hilt + WorkManager)
└── MainActivity.kt
```

---

## 3. Data Model

### 3.1 DiaryEntry

```kotlin
data class DiaryEntry(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",          // "yyyy-MM-dd" 형식
    val emotion: EmotionTag? = null,
    val imageUrl: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
```

### 3.2 EmotionTag

```kotlin
enum class EmotionTag(val emoji: String, val label: String) {
    HAPPY("😊", "행복"),
    SAD("😢", "슬픔"),
    ANGRY("😠", "분노"),
    CALM("😌", "평온"),
    EXCITED("🥰", "설렘"),
    ANXIOUS("😰", "불안"),
    TIRED("😴", "피곤")
}
```

### 3.3 Firestore 컬렉션 구조

```
diaries/
  {diaryId}/
    userId: String
    title: String
    content: String
    date: String         ← "2026-05-05" (인덱스 필드)
    emotion: String?     ← EmotionTag.name
    imageUrl: String?
    createdAt: Timestamp
    updatedAt: Timestamp
```

**Security Rules:**
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /diaries/{diaryId} {
      allow read, write: if request.auth != null
                         && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null
                    && request.auth.uid == request.resource.data.userId;
    }
  }
}
```

### 3.4 Firebase Storage 구조

```
images/
  {userId}/
    {diaryId}.jpg      ← 이미지 압축 후 업로드 (최대 1MB)
```

---

## 4. Screen Design

### 4.1 네비게이션 흐름

```
SplashScreen
    │
    ├── [미로그인] → LoginScreen ──── SignUpScreen
    │
    └── [로그인됨] → CalendarScreen (Home)
                        │
                        ├── [날짜 탭] → DiaryDetailScreen (일기 있음)
                        │              └── [수정] → DiaryEditorScreen
                        │
                        ├── [날짜 탭] → DiaryEditorScreen (일기 없음, 신규 작성)
                        │
                        └── [검색 아이콘] → SearchScreen
```

### 4.2 CalendarScreen — Monthly Calendar UI

```
┌─────────────────────────────────┐
│  [≡]   2026년 5월   [◀] [▶]  [🔍]│  ← 상단 헤더
├─────┬─────┬─────┬─────┬─────┬──┤
│ 일  │ 월  │ 화  │ 수  │ 목  │금 │토│  ← 요일 헤더
├─────┼─────┼─────┼─────┼─────┼──┤
│     │     │     │     │  1  │ 2│3 │
│     │     │     │     │ 😊  │  │  │
├─────┼─────┼─────┼─────┼─────┼──┤
│  4  │  5  │  6  │  7  │  8  │ 9│10│
│     │ 😢  │  ·  │     │     │😌│  │
├─────┼─────┼─────┼─────┼─────┼──┤
│  ...                            │
└─────────────────────────────────┘
```

- **헤더**: "◀ 2026년 5월 ▶" — 이전/다음 달 이동
- **기본**: 앱 진입 시 현재 달 표시 (`YearMonth.now()`)
- **월 이동**: 버튼 탭 시 1개월씩 이동, 한 화면에 한 달만 표시
- **날짜 셀**: 일기 있으면 EmotionTag.emoji 표시, 없으면 "·" 표시
- **오늘 강조**: 오늘 날짜 셀에 원형 배경 표시
- **탭 동작**: 셀 탭 → 해당 날짜 DiaryDetailScreen or DiaryEditorScreen

### 4.3 DiaryEditorScreen

```
┌─────────────────────────────────┐
│  [←]     일기 작성     [저장]    │
├─────────────────────────────────┤
│  📅 2026년 5월 5일 (자동 설정)   │
├─────────────────────────────────┤
│  😊 😢 😠 😌 🥰 😰 😴           │  ← 감정 태그 선택
├─────────────────────────────────┤
│  제목 입력...                    │
├─────────────────────────────────┤
│  오늘 하루를 기록하세요...        │
│  (여러 줄 텍스트 필드)            │
├─────────────────────────────────┤
│  [📷 사진 추가]  [현재 이미지]    │
└─────────────────────────────────┘
```

### 4.4 SearchScreen

```
┌─────────────────────────────────┐
│  [←]  🔍 검색어 입력...          │
├─────────────────────────────────┤
│  2026-05-05  😊  오늘 일기 제목  │
│  2026-05-03  😢  3일 일기 제목   │
│  ...                             │
└─────────────────────────────────┘
```
로컬 필터: 제목 + 내용에서 검색어 포함 여부 확인 (Firestore 쿼리 아님)

---

## 5. ViewModel Design

### 5.1 CalendarViewModel

```kotlin
data class CalendarUiState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val diaryMap: Map<String, DiaryEntry?> = emptyMap(),  // "yyyy-MM-dd" → DiaryEntry
    val isLoading: Boolean = false,
    val error: String? = null
)

class CalendarViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    fun goToPreviousMonth()   // currentYearMonth - 1
    fun goToNextMonth()       // currentYearMonth + 1
    fun loadMonthDiaries()    // Firestore에서 현재 월 일기 목록 로드
}
```

### 5.2 AuthViewModel

```kotlin
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
```

### 5.3 DiaryEditorViewModel

```kotlin
data class EditorUiState(
    val title: String = "",
    val content: String = "",
    val emotion: EmotionTag? = null,
    val imageUri: Uri? = null,
    val imageUrl: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
```

---

## 6. Repository Interface

### 6.1 DiaryRepository

```kotlin
interface DiaryRepository {
    fun getDiariesByMonth(userId: String, yearMonth: YearMonth): Flow<List<DiaryEntry>>
    suspend fun getDiaryByDate(userId: String, date: String): DiaryEntry?
    suspend fun saveDiary(entry: DiaryEntry): Result<String>
    suspend fun updateDiary(entry: DiaryEntry): Result<Unit>
    suspend fun deleteDiary(diaryId: String): Result<Unit>
    suspend fun searchDiaries(userId: String, query: String): List<DiaryEntry>
    suspend fun uploadImage(userId: String, diaryId: String, uri: Uri): Result<String>
}
```

### 6.2 AuthRepository

```kotlin
interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun signUp(email: String, password: String): Result<FirebaseUser>
    suspend fun signOut()
    fun isLoggedIn(): Boolean
}
```

---

## 7. Notification Design

### 7.1 DailyReminderWorker

```kotlin
class DailyReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        // NotificationManager로 "오늘 일기를 작성하세요!" 알림 발송
        return Result.success()
    }
}
```

**스케줄 등록 (PeriodicWorkRequest)**:
```kotlin
val dailyWork = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
    .setInitialDelay(/* 사용자 설정 시간까지 대기 */)
    .build()
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "daily_reminder",
    ExistingPeriodicWorkPolicy.KEEP,
    dailyWork
)
```

---

## 8. Gradle Dependencies

```kotlin
// build.gradle.kts (app)
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // Coil (이미지 로딩)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // WorkManager (알림)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
}
```

---

## 9. Test Plan

### 9.1 Unit Tests

| 대상 | 테스트 항목 |
|------|------------|
| CalendarViewModel | goToPreviousMonth() / goToNextMonth() 월 계산 정확성 |
| CalendarViewModel | loadMonthDiaries() → UiState.diaryMap 매핑 |
| DiaryRepositoryImpl | saveDiary() Firestore 저장 확인 (Firebase Emulator) |
| AuthRepositoryImpl | signIn() 성공/실패 케이스 |

### 9.2 UI Tests (Compose)

| 화면 | 테스트 항목 |
|------|------------|
| CalendarScreen | 이전/다음 달 버튼 탭 → 헤더 월 변경 확인 |
| CalendarScreen | 날짜 셀에 감정 이모티콘 표시 확인 |
| CalendarScreen | 날짜 탭 → EditorScreen 이동 확인 |
| DiaryEditorScreen | 감정 태그 선택 → UI 반영 |
| DiaryEditorScreen | 저장 버튼 → CalendarScreen 복귀 |
| LoginScreen | 로그인 성공 → CalendarScreen 이동 |

### 9.3 Integration Tests

| 항목 | 방법 |
|------|------|
| Firestore CRUD | Firebase Emulator Suite 사용 |
| Auth Flow | Firebase Auth Emulator |
| Storage Upload | Firebase Storage Emulator |

---

## 10. Security Checklist

- [ ] Firestore Rules: `request.auth.uid == resource.data.userId`
- [ ] Storage Rules: `request.auth != null && request.auth.uid == userId`
- [ ] 이미지 업로드 전 압축 (max 1MB)
- [ ] 이메일 형식 validation (Android 단에서도)
- [ ] 비밀번호 최소 6자 validation

---

## 11. Implementation Guide

### 11.1 Implementation Order

1. **Module 1 — Project Setup** (Android Studio 프로젝트 + Gradle 의존성 + Hilt 설정)
2. **Module 2 — Data Layer** (DiaryEntry, EmotionTag, DataSource, Repository)
3. **Module 3 — Auth** (LoginScreen, SignUpScreen, AuthViewModel, Firebase Auth)
4. **Module 4 — Calendar UI** (CalendarScreen, CalendarGrid, CalendarDayCell, CalendarViewModel)
5. **Module 5 — Diary CRUD** (DiaryEditorScreen, DiaryDetailScreen, DiaryEditorViewModel)
6. **Module 6 — Image & Search** (Storage 업로드, SearchScreen)
7. **Module 7 — Notification** (DailyReminderWorker, 알림 설정 UI)

### 11.2 Key Implementation Notes

- `CalendarGrid`: `java.time.YearMonth`로 해당 월의 1일 요일과 총 일수 계산
- `CalendarDayCell`: `diaryMap["yyyy-MM-dd"]?.emotion?.emoji ?: "·"` 로 이모티콘 표시
- `goToPreviousMonth()`: `currentYearMonth.minusMonths(1)` → Flow 재구독
- 이미지: `ActivityResultContracts.GetContent()` 로 갤러리 선택, `Compressor` 라이브러리로 압축

### 11.3 Session Guide

| Session | Modules | 예상 시간 |
|---------|---------|----------|
| Session 1 | Module 1 + 2 (Setup + Data Layer) | 2h |
| Session 2 | Module 3 (Auth) | 1.5h |
| Session 3 | Module 4 (Calendar UI) | 2.5h |
| Session 4 | Module 5 (CRUD) | 2h |
| Session 5 | Module 6 + 7 (Image + Notification) | 2h |

```
/pdca do diary-app --scope module-1,module-2
/pdca do diary-app --scope module-3
/pdca do diary-app --scope module-4
/pdca do diary-app --scope module-5
/pdca do diary-app --scope module-6,module-7
```

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-05 | Initial design (Option C — MVVM + Repository) | faith79@jobkorea.co.kr |
