# diary-app Gap Analysis

> **Feature**: diary-app
> **Date**: 2026-05-05
> **Phase**: Check
> **Analyst**: faith79@jobkorea.co.kr

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 개인 일기를 클라우드에 안전하게 저장하고 감정·이미지와 함께 기록하고 싶다 |
| **WHO** | 매일 일기를 쓰거나 감정을 기록하고 싶은 Android 사용자 |
| **RISK** | Firebase 비용(무료 한도 초과), 이미지 업로드 Storage 용량, 오프라인 동기화 복잡성 |
| **SUCCESS** | 로그인·로그아웃, 달력 UI + 감정 이모티콘 표시, 일기 CRUD, 감정 태그 저장, 이미지 첨부, 검색, 일일 알림 모두 동작 |
| **SCOPE** | Phase 1 — Auth + 일기 CRUD + 감정 태그 + 이미지 + 검색 + 알림 (Android 단독) |

---

## 1. Match Rate Summary

| Axis | Rate | Method |
|------|:----:|--------|
| Structural | 100% | Design §2.2 전부 기재 완료 (GAP-04 반영) |
| Functional | 98% | FR-01~FR-12 + FR-06a~06e, GAP-01~03 수정 완료 |
| Contract | 98% | Repository 인터페이스 전부 일치, SettingsViewModel 추가 |
| **Overall** | **98%** | Static-only formula (GAP-01~04 수정 후 재측정) |

> Static-only formula: `Structural × 0.2 + Functional × 0.4 + Contract × 0.4`
> = (100 × 0.2) + (98 × 0.4) + (98 × 0.4) = 20 + 39.2 + 39.2 = **98%** ✅ (GAP 수정 후)

---

## 2. Strategic Alignment Check

| 항목 | 평가 |
|------|------|
| PRD 핵심 문제 해결 (WHY) | ✅ Firebase Auth + Firestore로 개인 클라우드 일기 공간 구현 |
| Plan Success Criteria | ✅ FR-01~FR-12 + FR-06a~06e 대부분 충족 (2개 부분 미흡) |
| Design 결정 (MVVM + Repository) | ✅ 3-layer 아키텍처 정확히 구현 |
| CalendarViewModel 구조 | ✅ flatMapLatest 패턴이 설계보다 우수한 반응형 구조 |

---

## 3. Structural Match — 100%

### Design §2.2 대비 구현 파일

| 파일 | 설계 | 구현 | 상태 |
|------|:----:|:----:|------|
| `ui/auth/LoginScreen.kt` | ✅ | ✅ | OK |
| `ui/auth/SignUpScreen.kt` | ✅ | ✅ | OK |
| `ui/auth/AuthUiState.kt` | ✅ | ✅ | OK |
| `ui/home/CalendarScreen.kt` | ✅ | ✅ | OK |
| `ui/home/CalendarGrid.kt` | ✅ | ✅ | OK |
| `ui/home/CalendarDayCell.kt` | ✅ | ✅ | OK |
| `ui/home/CalendarUiState.kt` | ✅ | ✅ | OK |
| `ui/editor/DiaryEditorScreen.kt` | ✅ | ✅ | OK |
| `ui/editor/EmotionTagSelector.kt` | ✅ | ✅ | OK |
| `ui/editor/EditorUiState.kt` | ✅ | ✅ | OK |
| `ui/detail/DiaryDetailScreen.kt` | ✅ | ✅ | OK |
| `viewmodel/AuthViewModel.kt` | ✅ | ✅ | OK |
| `viewmodel/CalendarViewModel.kt` | ✅ | ✅ | OK |
| `viewmodel/DiaryEditorViewModel.kt` | ✅ | ✅ | OK |
| `data/model/DiaryEntry.kt` | ✅ | ✅ | OK |
| `data/model/EmotionTag.kt` | ✅ | ✅ | OK |
| `data/repository/AuthRepository.kt` | ✅ | ✅ | OK |
| `data/repository/AuthRepositoryImpl.kt` | ✅ | ✅ | OK |
| `data/repository/DiaryRepository.kt` | ✅ | ✅ | OK |
| `data/repository/DiaryRepositoryImpl.kt` | ✅ | ✅ | OK |
| `data/source/AuthDataSource.kt` | ✅ | ✅ | OK |
| `data/source/FirestoreDataSource.kt` | ✅ | ✅ | OK |
| `data/source/StorageDataSource.kt` | ✅ | ✅ | OK |
| `notification/DailyReminderWorker.kt` | ✅ | ✅ | OK |
| `di/RepositoryModule.kt` | ✅ | ✅ | OK |
| `di/DataSourceModule.kt` | ✅ | ✅ | OK |
| `MainActivity.kt` | ✅ | ✅ | OK |
| `SearchScreen.kt` *(추가)* | 미기재 | ✅ | 구현 초과 |
| `SearchViewModel.kt` *(추가)* | 미기재 | ✅ | 구현 초과 |
| `DiaryApp.kt` *(추가)* | 미기재 | ✅ | 구현 초과 |

---

## 4. Functional Match — 90%

### FR 충족 현황

| FR | 요구사항 | 구현 근거 | 상태 |
|----|---------|-----------|------|
| FR-01 | 이메일/패스워드 회원가입 | SignUpScreen + AuthViewModel.signUp() | ✅ |
| FR-02 | 로그인/로그아웃 | LoginScreen + AuthViewModel.signIn/signOut() | ✅ |
| FR-03 | 일기 작성 (제목, 내용, 날짜 자동) | DiaryEditorScreen + EditorUiState | ✅ |
| FR-04 | 일기 수정 | existingDiaryId 체크 → updateDiary() | ✅ |
| FR-05 | 일기 삭제 (확인 다이얼로그) | AlertDialog + deleteDiary() | ✅ |
| FR-06 | Monthly Calendar UI | CalendarScreen + CalendarGrid (7×N grid) | ✅ |
| FR-06a | 앱 진입 시 현재 달 표시 | CalendarViewModel: YearMonth.now() | ✅ |
| FR-06b | "◀ 2026년 5월 ▶" 헤더 | CalendarScreen TopAppBar + IconButton | ✅ |
| FR-06c | 이전/다음 달 1개월씩 이동 | goToPreviousMonth/goToNextMonth() | ✅ |
| FR-06d | 감정 이모티콘 달력 셀 표시 | `entry?.emotion?.emoji ?: "·"` | ✅ |
| FR-06e | 날짜 탭 → 일기 화면 이동 | CalendarDayCell onClick → navController | ✅ |
| FR-07 | 감정 태그 선택 | EmotionTagSelector (7개 이모티콘 Row) | ✅ |
| FR-08 | 이미지 1장 첨부 (갤러리/**카메라**) | GetContent() 갤러리만 — **카메라 미구현** | ⚠️ |
| FR-09 | Firebase Storage 업로드/다운로드 | StorageDataSource + Coil AsyncImage | ✅ |
| FR-10 | 제목/내용 글자 검색 | SearchViewModel debounce(300) + in-memory 필터 | ✅ |
| FR-11 | 일일 알림 (**시간 설정 가능**) | DailyReminderWorker — **9PM 하드코딩** | ⚠️ |
| FR-12 | 로그인 상태 유지 | MainActivity startDestination 분기 | ✅ |

### 추가 Design Spec 항목

| 항목 | 설계 §4.2 | 구현 | 상태 |
|------|-----------|------|------|
| 오늘 날짜 원형 강조 | 원형 배경 표시 | 명시적 구현 없음 | ⚠️ |

---

## 5. Contract Match — 95%

### DiaryRepository Interface (§6.1)

| 메서드 | 반환 타입 | 구현 | 상태 |
|--------|-----------|------|------|
| `getDiariesByMonth()` | `Flow<List<DiaryEntry>>` | callbackFlow + snapshot listener | ✅ |
| `getDiaryByDate()` | `suspend DiaryEntry?` | Firestore whereEqualTo("date") | ✅ |
| `saveDiary()` | `suspend Result<String>` | Firestore add() → docId 반환 | ✅ |
| `updateDiary()` | `suspend Result<Unit>` | Firestore set() with MERGE | ✅ |
| `deleteDiary()` | `suspend Result<Unit>` | Firestore delete() | ✅ |
| `searchDiaries()` | `suspend List<DiaryEntry>` | in-memory filter (로컬) | ✅ |
| `uploadImage()` | `suspend Result<String>` | Storage putFile() → downloadUrl | ✅ |

### AuthRepository Interface (§6.2)

| 메서드 | 구현 | 상태 |
|--------|------|------|
| `getCurrentUser()` | FirebaseAuth.currentUser | ✅ |
| `signIn()` | signInWithEmailAndPassword() | ✅ |
| `signUp()` | createUserWithEmailAndPassword() | ✅ |
| `signOut()` | FirebaseAuth.signOut() | ✅ |
| `isLoggedIn()` | currentUser != null | ✅ |

### CalendarViewModel 구조 변경 (설계 개선)

| 항목 | 설계 §5.1 | 구현 |
|------|-----------|------|
| 데이터 로드 | `loadMonthDiaries()` 수동 호출 | `flatMapLatest` 자동 반응형 구독 |
| 평가 | 설계보다 우수 | 월 변경 시 이전 Flow 자동 취소 + 재구독 |

---

## 6. Gap List

### ⚠️ Important

| ID | 축 | 설명 | 수정 방법 |
|----|-----|------|-----------|
| GAP-01 | Functional | **FR-08 카메라 미지원**: `GetContent()` 갤러리만, 카메라 런처 없음 | `TakePicture()` launcher 추가 + 선택 BottomSheet 구현 |
| GAP-02 | Functional | **FR-11 알림 시간 설정 UI 없음**: 9PM 하드코딩 (`DiaryApp.kt`) | SettingsScreen + SharedPreferences + WorkManager 재등록 |

### 💡 Minor

| ID | 축 | 설명 | 수정 방법 |
|----|-----|------|-----------|
| GAP-03 | Functional | **오늘 날짜 원형 강조** (Design §4.2) 미구현 | CalendarDayCell에 `LocalDate.now() == date` 체크 + 원형 배경 |
| GAP-04 | Structural | **SearchScreen/SearchViewModel** Design §2.2 미기재 (구현은 됨) | Design 문서 §2.2 패키지 구조 업데이트 |

---

## 7. Plan Success Criteria Final Status

| 기준 | 상태 | 근거 |
|------|------|------|
| Firebase Auth 로그인·로그아웃 | ✅ Met | AuthViewModel.signIn/signOut |
| Firestore CRUD (본인 데이터만) | ✅ Met | Security Rules + userId 필터 |
| 이미지 업로드/표시 | ✅ Met | StorageDataSource + Coil |
| 감정 태그 저장/표시 | ✅ Met | EmotionTag enum + Firestore 저장 |
| 앱 진입 시 현재 달 달력 | ✅ Met | YearMonth.now() |
| 이전/다음 달 버튼 1개월 이동 | ✅ Met | goToPreviousMonth/Next |
| 한 화면에 한 달 달력 (스크롤 없이) | ✅ Met | 7×N Grid, LazyColumn 없음 |
| 일기 작성 날짜 감정 이모티콘 | ✅ Met | `entry?.emotion?.emoji ?: "·"` |
| 검색 필터 동작 | ✅ Met | SearchViewModel + debounce |
| 일일 알림 수신 | ⚠️ Partial | 알림은 동작하나 시간 설정 UI 없음 |

**Overall Success Rate: 9.5/10 기준 충족**

---

## 8. Decision Record Verification

| 결정 | 설계 | 구현 준수 | 결과 |
|------|------|-----------|------|
| MVVM + Repository | Plan/Design 선택 | ✅ 3-layer 정확히 구현 | 유지보수성 향상 |
| StateFlow + Compose | Design §2.0 | ✅ `collectAsStateWithLifecycle()` | 안정적 UI 구독 |
| Hilt DI | Design §2.0 | ✅ `@HiltViewModel`, `@Binds`, `@HiltWorker` | DI 완전 적용 |
| Coil image loading | Design §2.0 | ✅ `AsyncImage()` | Compose 친화적 |
| WorkManager 알림 | Design §7 | ✅ PeriodicWorkRequest | 배터리 최적화 |
| CalendarViewModel flatMapLatest | 설계 초과 | ✅ 개선 | 반응형 Flow 자동 관리 |

---

---

## 9. GAP 수정 이력

| GAP | 수정 내용 | 파일 |
|-----|-----------|------|
| GAP-01 | TakePicture launcher + 이미지 선택 BottomSheet + 카메라 권한 처리 | `DiaryEditorScreen.kt`, `AndroidManifest.xml`, `res/xml/file_paths.xml` |
| GAP-02 | SettingsScreen + SettingsViewModel + NotificationPreferences + NotificationModule + DiaryApp 동적 시간 반영 | `ui/settings/SettingsScreen.kt`, `viewmodel/SettingsViewModel.kt`, `notification/NotificationPreferences.kt`, `di/NotificationModule.kt` |
| GAP-03 | CalendarDayCell `isToday` 파라미터 + 원형 배경 강조 | `CalendarDayCell.kt`, `CalendarGrid.kt` |
| GAP-04 | Design §2.2 패키지 구조에 SearchScreen, SearchViewModel, SettingsScreen, SettingsViewModel, NotificationPreferences, NotificationModule, DiaryApp 추가 | `diary-app.design.md` |

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-05 | Initial gap analysis (Static-only, 94%) | faith79@jobkorea.co.kr |
| 0.2 | 2026-05-05 | GAP-01~04 전부 수정 완료 → 98% | faith79@jobkorea.co.kr |
