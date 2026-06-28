# JoiDiary Upgrade — PDCA Completion Report

> **Feature**: joidiary-upgrade
> **Version**: v0.1.0 → v0.2.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-17
> **Status**: Completed
> **Match Rate**: 96% (post-iterate)

---

## 1. Executive Summary

### 1.1 Overview

| Perspective | Planned | Delivered |
|-------------|---------|-----------|
| **Problem** | 기존 Diary App의 UX 마찰 (제목 입력, 이미지 1장, 달력 버튼만, 단조로운 디자인) | 11개 항목 전체 해결 — 더 빠르고 따뜻한 일기 경험 제공 |
| **Solution** | "조이어리" 리브랜딩 + 날씨·이미지·스와이프·파스텔 디자인 통합 적용 | SC-01~SC-11 모두 구현. Upsert 흐름 + Storage 연동 삭제 + Lottie 로딩 포함 |
| **Function/UX Effect** | 제목 없이 빠른 기록, 날씨 추적, 이미지 3장, 스와이프 달력, 따뜻한 파스텔 시각 | 감정+날씨+이미지 3축 기록, HorizontalPager 스와이프, 파스텔 코랄/피치 테마 |
| **Core Value** | 매일 쓰고 싶어지는 나만의 클라우드 일기 앱 | v0.2.0 배포 가능 상태 달성 |

### 1.2 PDCA Journey

```
[Plan] ✅ → [Design] ✅ → [Do] ✅ → [Check] ✅ → [Act] ✅ → [Report] ✅
  5 세션     Option C      module-1    91% → 96%    2개 Gap
```

### 1.3 Value Delivered

| Metric | Target | Result |
|--------|--------|--------|
| SC 완료율 | 11/11 | **11/11 (100%)** |
| Match Rate | ≥ 90% | **96%** |
| 신규 파일 | 6개 | **6개** (WeatherTag, ImageCompressor, WeatherSelector, MultiImagePicker, LoadingOverlay, loading.json) |
| 수정 파일 | 10개 | **10+개** |
| Lottie 의존성 | lottie-compose:6.4.0 | **추가 완료** |
| 하위 호환 | v0.1.0 데이터 정상 표시 | **imageUrl fallback 구현** |

---

## 2. Success Criteria Final Status

### 2.1 Definition of Done

| 기준 | 상태 | 근거 |
|------|:----:|------|
| SC-01~SC-11 모든 항목 구현 | ✅ | 아래 §3 참조 |
| 기존 v0.1.0 일기 정상 표시 | ✅ | `FirestoreDataSource.kt:toDomain()` — imageUrl fallback, title 무시 |
| 날씨 선택 후 Firestore 저장/불러오기 | ✅ | `DiaryEntryDto.weather: String?` + `WeatherTag.valueOf()` |
| 이미지 3장 선택 → 압축 → 업로드 → 표시 | ✅ | `ImageCompressor` + `StorageDataSource.uploadImages()` + `MultiImagePicker` |
| 달력 스와이프로 월 이동 | ✅ | `HomeScreen.kt:HorizontalPager` + `settledPage` → `loadMonth` |
| 파스텔 디자인 전체 적용 | ✅ | `Color.kt`, `Theme.kt` — fixed pastel ColorScheme |
| Upsert 흐름 동작 | ✅ | Calendar cell ✅ + FAB Upsert check ✅ (iterate에서 수정) |
| 일기 삭제 시 Storage 이미지 삭제 | ✅ | `DiaryRepositoryImpl.deleteDiaryWithImages()` |
| 수정 화면 이미지 X → Storage 즉시 삭제 | ✅ | `DiaryEditorScreen:onRemoveExisting → removeImage()` |
| Lottie 로딩 애니메이션 + 딤 처리 | ✅ | `LoadingOverlay.kt` + `assets/loading.json` |

### 2.2 Quality Criteria

| 기준 | 상태 | 근거 |
|------|:----:|------|
| 이미지 압축 후 1MB 이하 | ✅ | `ImageCompressor` do-while quality loop (90→10) |
| 날씨 null 기존 데이터 graceful hide | ✅ | `DiaryDetailScreen` — `e.weather?.let { ... }` null-safe |
| imageUrls 빈 배열 + imageUrl fallback | ✅ | `toDomain()` — `imageUrls.ifEmpty { imageUrl?.let { listOf(it) } ?: emptyList() }` |
| Firestore 삭제 실패 → Storage 미삭제 | ✅ | `deleteDiaryWithImages` — deleteFirestore 성공 후에만 Storage 삭제 |
| Storage 삭제 실패 Snackbar 안내 | ✅ | `DiaryDetailScreen` — Snackbar 추가 (iterate에서 수정) |

---

## 3. SC 구현 상세

### 3.1 SC-01~SC-11 전체 검증

| SC | 내용 | 구현 파일 | 핵심 코드 |
|----|------|-----------|-----------|
| SC-01 | 앱 이름 "조이어리" | `strings.xml` | `app_name = "조이어리"` |
| SC-02 | title 제거 | `DiaryEditorScreen.kt` | title var 완전 제거, content만 TextField |
| SC-03 | 날씨 선택 탭 | `WeatherSelector.kt`, `WeatherTag.kt` | 5개 FilterChip, 재클릭 시 deselect |
| SC-04 | Upsert 흐름 | `HomeScreen.kt`, `NavGraph.kt` | FAB → `getEntryByDate()` → edit/new 분기 |
| SC-05 | 이미지 3장 + 1MB 압축 | `MultiImagePicker.kt`, `ImageCompressor.kt` | `GetMultipleContents`, quality do-while loop |
| SC-06 | 달력 스와이프 | `HomeScreen.kt` | `HorizontalPager`, `pageToYearMonth()`, `settledPage` |
| SC-07 | 파스텔 디자인 | `Color.kt`, `Theme.kt` | PastelCoral/Peach/Pink, fixed ColorScheme |
| SC-08 | 상세 화면 레이아웃 | `DiaryDetailScreen.kt` | `LazyRow(contentPadding=16dp)` + `Card` |
| SC-09 | 삭제 시 Storage 연동 | `DiaryRepositoryImpl.kt` | Firestore 먼저 → 성공 시 `deleteImages()` |
| SC-10 | 수정 중 이미지 삭제 | `DiaryEditorScreen.kt`, `DiaryViewModel.kt` | X 클릭 → `removeImage(url)` → Storage 즉시 |
| SC-11 | Lottie 로딩 오버레이 | `LoadingOverlay.kt`, `loading.json` | `AnimatedVisibility` + Lottie 100dp + 딤 0.45α |

---

## 4. Architecture Decision Record

### 4.1 Key Decisions & Outcomes

| 결정 | 채택 이유 | 결과 |
|------|-----------|------|
| **Option C (Pragmatic Balance)** 아키텍처 | 기존 MVVM 구조 최대 활용, 최소 신규 파일 | 6개 신규 / 10개 수정 — 계획 일치 |
| **HorizontalPager** (detectHorizontalDragGestures 대신) | 페이지 단위 자연스러운 스냅, 화살표 버튼과 공존 | 페이지 인덱스 ↔ YearMonth 매핑으로 깔끔하게 구현 |
| **ImageCompressor** 별도 클래스 분리 | ViewModel 책임 분리, 테스트 용이성 | `@Singleton`으로 Hilt 주입, quality loop 정상 동작 |
| **Firestore 삭제 → Storage 삭제 순서** | 데이터 정합성 — Firestore 실패 시 이미지 고아 방지 | `deleteDiaryWithImages` Result 체이닝으로 구현 |
| **이미지URL fallback** (`imageUrl` → `imageUrls[0]`) | v0.1.0 데이터 마이그레이션 없이 하위 호환 | `toDomain()` ifEmpty 분기로 완전 투명 처리 |
| **FAB Upsert를 HomeScreen에서 처리** | NavGraph에서 suspend 함수 호출 불가 | `rememberCoroutineScope` + `getEntryByDate()` 코루틴 패턴 |

---

## 5. Gap Analysis 결과 (Check Phase)

### 5.1 Initial Check (before iterate)

| 지표 | 점수 |
|------|:----:|
| Structural | 98% |
| Functional | 85% |
| Contract | 93% |
| **Overall** | **91%** |

### 5.2 Gaps Found & Fixed

| Gap | 심각도 | 원인 | 수정 |
|-----|:------:|------|------|
| SC-04 FAB Upsert 미적용 | Critical | FAB가 existingId 없이 바로 Editor로 이동 | `HomeScreen.kt` FAB에 `getEntryByDate()` 코루틴 추가 + `onEditDiary` 콜백 분리 |
| DiaryDetailScreen 삭제 오류 피드백 없음 | Important | `uiState.Error` 감지했으나 UI 표시 없음 | `SnackbarHostState` + `LaunchedEffect(uiState)` 에서 Error → Snackbar |

### 5.3 Final Check (post-iterate)

| 지표 | 점수 |
|------|:----:|
| Structural | 98% |
| Functional | 97% |
| Contract | 93% |
| **Overall** | **96%** |

---

## 6. 구현된 파일 전체 목록

### 6.1 신규 생성 (6개)

| 파일 | 역할 |
|------|------|
| `data/model/WeatherTag.kt` | 날씨 5종 enum (emoji + label) |
| `data/util/ImageCompressor.kt` | 1MB do-while 품질 압축 |
| `ui/components/WeatherSelector.kt` | FilterChip 날씨 선택 행 |
| `ui/components/MultiImagePicker.kt` | 썸네일 + X버튼 + 추가버튼 (최대 3장) |
| `ui/components/LoadingOverlay.kt` | 전체화면 딤 + Lottie 카드 |
| `assets/loading.json` | 코랄 색 회전 arc 로딩 애니메이션 |

### 6.2 주요 수정 (10개)

| 파일 | 변경 내용 |
|------|-----------|
| `data/model/DiaryEntry.kt` | title 제거, `weather: WeatherTag?`, `imageUrls: List<String>` |
| `data/source/FirestoreDataSource.kt` | weather/imageUrls 필드, legacy imageUrl fallback |
| `data/source/StorageDataSource.kt` | `uploadImages()`, `deleteImage()`, `deleteImages()` |
| `data/repository/DiaryRepository.kt` | 인터페이스 메서드 추가 |
| `data/repository/DiaryRepositoryImpl.kt` | `deleteDiaryWithImages()` 순서 보장 |
| `viewmodel/DiaryViewModel.kt` | `isLoading`, `getEntryByDate()`, `removeImage()`, 압축 업로드 통합 |
| `ui/home/HomeScreen.kt` | `HorizontalPager` + FAB Upsert + `onEditDiary` 콜백 |
| `ui/diary/DiaryEditorScreen.kt` | title 제거, WeatherSelector, MultiImagePicker, LoadingOverlay |
| `ui/diary/DiaryDetailScreen.kt` | LazyRow 이미지 여백, Card 내용, 날씨 칩, Snackbar |
| `ui/theme/Color.kt` + `Theme.kt` | 파스텔 고정 팔레트, dynamic color 제거 |
| `navigation/NavGraph.kt` | `onEditDiary` 콜백 추가 |
| `res/values/strings.xml` | app_name = "조이어리" |
| `app/build.gradle.kts` | lottie-compose:6.4.0 의존성 |

---

## 7. 잔여 사항 (Future Work)

| 항목 | 우선순위 | 설명 |
|------|:--------:|------|
| loading.json Lottie 교체 | Medium | 현재 hand-crafted JSON — [LottieFiles](https://lottiefiles.com/)에서 파스텔 톤 애니메이션으로 교체 권장 |
| 이미지 리사이즈 (해상도) | Low | 현재 quality loop만 적용 — 해상도 다운스케일 추가 시 압축 효율 향상 |
| Storage 고아 이미지 정리 | Low | 삭제 실패 시 남는 파일 — Cloud Functions로 주기적 정리 고려 |
| 유닛 테스트 | Medium | `ImageCompressor`, `DiaryRepositoryImpl` fallback 로직 테스트 작성 |

---

## 8. Lessons Learned

| 교훈 | 내용 |
|------|------|
| **Upsert 체크 위치** | NavGraph는 suspend 함수 호출 불가 → Composable 레이어(HomeScreen)에서 coroutineScope로 처리 |
| **HorizontalPager 페이지 설계** | 절대 YearMonth 기반 페이지(BASE_YEAR=2000) 설계가 상대 offset 방식보다 안정적 |
| **하위 호환 fallback** | `imageUrl → imageUrls` 전환 시 Repository DTO에서 ifEmpty 분기로 투명하게 처리 |
| **Storage 삭제 순서** | Firestore 삭제 성공 후 Storage 삭제 — `Result.isFailure` 조기 반환 패턴 효과적 |
| **Lottie 에셋 주의** | `LottieCompositionSpec.Asset("loading.json")` — assets 폴더 배치 필수 (res가 아닌 assets/) |

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0 | 2026-05-17 | PDCA Cycle 완료 보고서 작성 | faith79@jobkorea.co.kr |
