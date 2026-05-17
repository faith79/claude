# JoiDiary Upgrade Planning Document

> **Summary**: 기존 Diary App을 "조이어리"로 리브랜딩하고 날씨 기록, Upsert 작성, 다중 이미지, 스와이프 달력, 파스텔 디자인 등을 추가하는 v0.2.0 업그레이드
>
> **Project**: claude / diary-app
> **Version**: 0.2.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-17
> **Status**: Draft
> **Base Plan**: diary-app.plan.md (v0.1.0)

---

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 기존 Diary App은 제목 입력이 번거롭고, 날씨 기록 불가, 이미지 1장 제한, 달력 UX 불편, 단조로운 디자인으로 일상 기록 앱으로서 완성도가 부족하다 |
| **Solution** | 앱 이름을 "조이어리"로 리브랜딩하고 날씨 탭·Upsert 작성 흐름·다중 이미지(최대 3장, 1MB 압축)·스와이프 달력·파스텔 디자인·상세 화면 개선을 한 번에 적용 |
| **Function/UX Effect** | 제목 없이 빠른 내용 기록, 날씨 감정 추적, 중복 작성 방지 Upsert, 이미지 최대 3장 첨부, 드래그로 자연스러운 달력 탐색, 피치/코랄 파스텔 시각 경험 |
| **Core Value** | 더 가볍고 따뜻하게 — 매일 쓰고 싶어지는 나만의 클라우드 일기 앱 |

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 기존 v0.1.0 기능의 UX 마찰을 제거하고, 날씨·이미지 강화로 일상 기록의 완성도를 높인다 |
| **WHO** | 기존 Diary App 사용자 — 더 빠르고 직관적인 일기 작성 경험을 원하는 Android 사용자 |
| **RISK** | 데이터 모델 변경(title 제거·weather 추가)으로 기존 Firestore 문서와 호환성 문제 발생 가능 |
| **SUCCESS** | 8개 변경 항목 모두 에뮬레이터 동작 확인, 기존 데이터(v0.1.0) 정상 표시 |
| **SCOPE** | Android 앱 레이어만 변경 — Firebase 스키마 하위 호환 유지, 신규 필드는 nullable로 추가 |

---

## 1. Overview

### 1.1 Purpose

기존 `diary-app` v0.1.0을 "조이어리(JoiDiary)"로 업그레이드.
주요 목표는 ① UX 마찰 감소(제목 제거, Upsert 흐름), ② 기록 풍부화(날씨, 다중 이미지), ③ 시각 개선(파스텔 디자인, 상세 화면 레이아웃)이다.

### 1.2 Background

v0.1.0은 기본 CRUD와 감정 태그까지 구현된 상태.
이번 v0.2.0은 기능 추가보다 **사용자 경험의 질적 향상**에 집중한다.

### 1.3 Related Documents

- Base Plan: `docs/01-plan/features/diary-app.plan.md`
- 기존 코드: `diary-app/app/src/main/java/com/example/diaryapp/`

---

## 2. Scope

### 2.1 In Scope (변경·추가 항목)

| # | 항목 | 변경 유형 |
|---|------|-----------|
| SC-01 | 앱 이름 "조이어리" 리브랜딩 | 수정 |
| SC-02 | 일기 작성 — title 필드 제거, content만 기록 | 수정 |
| SC-03 | 날씨 선택 탭 추가 (감정 태그 아래) | 신규 |
| SC-04 | 날짜 선택 후 신규 작성 Upsert 흐름 | 수정 |
| SC-05 | 이미지 최대 3장 업로드 + 1MB 압축 | 수정 |
| SC-06 | 달력 스와이프(드래그) 월 이동 | 신규 |
| SC-07 | 파스텔 톤(피치/코랄/연핑크) 디자인 적용 | 수정 |
| SC-08 | 상세 화면 — 이미지 좌우 여백, 내용 Card 박스 | 수정 |
| SC-09 | 일기 삭제 시 Firebase Storage 이미지 연동 삭제 | 신규 |
| SC-10 | 일기 수정 중 이미지 개별 삭제 시 Storage 즉시 삭제 | 신규 |
| SC-11 | 저장 중 전체화면 중앙 로딩 UI (Lottie 애니메이션) | 수정 |

### 2.2 Out of Scope

- Firestore 데이터 마이그레이션 스크립트 (하위 호환 nullable 처리로 대신)
- iOS 지원
- 날씨 API 자동 감지 (수동 선택만)
- 검색 화면 변경
- 알림 기능 변경

---

## 3. Requirements

### 3.1 Functional Requirements

| ID | Requirement | Priority | 연관 SC |
|----|-------------|----------|---------|
| FR-01 | 앱 이름을 "조이어리"로 변경 (앱바, 스플래시, 패키지 표시명) | High | SC-01 |
| FR-02 | 일기 작성/수정 화면에서 title 입력 필드 제거 | High | SC-02 |
| FR-03 | DiaryEntry 모델에서 title 필드를 optional로 처리 (기존 데이터 호환) | High | SC-02 |
| FR-04 | 날씨 선택 UI — 감정 태그 아래에 이모지+텍스트 칩 5개 배치 | High | SC-03 |
| FR-05 | 날씨 항목: ☀️맑음 / ⛅구름조금 / ☁️흐림 / 🌧️비 / ❄️눈 | High | SC-03 |
| FR-06 | DiaryEntry 모델에 `weather: WeatherTag?` 필드 추가 | High | SC-03 |
| FR-07 | 달력에서 날짜 선택 후 "신규 작성" → 해당 날짜 일기 존재 시 수정 화면 오픈 | High | SC-04 |
| FR-08 | 해당 날짜 일기 없으면 기존대로 신규 작성 화면 오픈 | High | SC-04 |
| FR-09 | 이미지 선택 시 1MB 초과하면 클라이언트 사이드 리사이즈 압축 적용 | High | SC-05 |
| FR-10 | 이미지 최대 3장 선택/첨부/Storage 업로드 | High | SC-05 |
| FR-11 | DiaryEntry 모델 `imageUrl: String?` → `imageUrls: List<String>` 변경 | High | SC-05 |
| FR-12 | 달력 화면에 HorizontalPager 또는 detectHorizontalDragGestures 적용으로 스와이프 월 이동 | Medium | SC-06 |
| FR-13 | 앱 Color Theme을 피치/코랄/연핑크 파스텔 계열로 교체 | Medium | SC-07 |
| FR-14 | 일기 상세 화면 — 이미지에 좌우 16dp 패딩(여백) 적용 | Medium | SC-08 |
| FR-15 | 일기 상세 화면 — 내용 텍스트를 Card(RoundedCorner) 박스 안에 표시 | Medium | SC-08 |
| FR-16 | 일기 삭제 시 Firestore 문서 삭제와 함께 imageUrls 목록의 모든 Storage 파일 삭제 | High | SC-09 |
| FR-17 | 일기 수정 화면에서 이미지 개별 삭제(X 버튼) 시 해당 Storage URL 파일 즉시 삭제 | High | SC-10 |
| FR-18 | 저장/업로드 중 화면 중앙에 Lottie 로딩 애니메이션 오버레이 표시 (기존 선형 ProgressBar 대체) | Medium | SC-11 |
| FR-19 | 로딩 중 배경 딤 처리(scrim)로 사용자 입력 차단 | Medium | SC-11 |

### 3.2 Non-Functional Requirements

| Category | Criteria |
|----------|----------|
| **호환성** | 기존 v0.1.0 Firestore 문서 정상 표시 (title null 허용, imageUrls 없으면 imageUrl fallback) |
| **성능** | 이미지 압축 후 업로드 → Storage 비용 절감, 로딩 속도 향상 |
| **UX** | 스와이프 달력 — 드래그 제스처와 화살표 버튼 모두 동작 |
| **디자인** | Material 3 Dynamic Color 유지하면서 Seed Color를 파스텔 피치로 교체 |

---

## 4. Success Criteria

### 4.1 Definition of Done

- [ ] SC-01~SC-11 모든 항목 에뮬레이터 동작 확인
- [ ] 기존 v0.1.0으로 작성된 일기 — 제목 없이도 정상 표시
- [ ] 날씨 선택 후 Firestore 저장/불러오기 정상
- [ ] 이미지 3장 선택 → 압축 → 업로드 → 상세 화면 표시 정상
- [ ] 달력 스와이프로 월 이동 동작
- [ ] 파스텔 디자인 전체 화면 적용 확인
- [ ] Upsert 흐름 — 기존 일기 있는 날 "신규 작성" 시 수정 화면 전환
- [ ] 일기 삭제 시 Storage 이미지 전체 삭제 확인 (Firebase Console에서 검증)
- [ ] 수정 화면 이미지 삭제 시 Storage 즉시 삭제 확인
- [ ] 저장 중 Lottie 로딩 애니메이션 정상 표시 및 딤 처리 동작 확인

### 4.2 Quality Criteria

- [ ] 이미지 압축 후 1MB 이하인지 단위 확인 (Bitmap.compress 적용)
- [ ] 날씨 null인 기존 데이터 표시 시 날씨 영역 graceful hide
- [ ] imageUrls 없는 기존 데이터 → imageUrl(단일) fallback 처리
- [ ] 일기 삭제 시 Firestore 삭제 실패하면 Storage도 삭제하지 않음 (트랜잭션 순서 보장)
- [ ] Storage 삭제 실패 시 사용자에게 Snackbar 오류 안내

---

## 5. Data Model Changes

### 5.1 DiaryEntry 변경

```kotlin
// v0.1.0
data class DiaryEntry(
    val title: String = "",          // 제거 예정
    val imageUrl: String? = null,    // 교체 예정
    ...
)

// v0.2.0
data class DiaryEntry(
    // title 제거 (Firestore 기존 문서 호환 — 읽을 때 무시)
    val content: String = "",
    val weather: WeatherTag? = null, // 신규
    val imageUrls: List<String> = emptyList(), // imageUrl 대체 (신규)
    ...
)
```

### 5.2 WeatherTag 신규 추가

```kotlin
enum class WeatherTag(val emoji: String, val label: String) {
    SUNNY("☀️", "맑음"),
    PARTLY_CLOUDY("⛅", "구름조금"),
    CLOUDY("☁️", "흐림"),
    RAINY("🌧️", "비"),
    SNOWY("❄️", "눈")
}
```

### 5.3 하위 호환 전략

| 필드 | 전략 |
|------|------|
| `title` | Firestore 읽을 때 무시 (앱에서 표시 안 함) |
| `imageUrl` (구버전) | imageUrls가 비어있으면 imageUrl을 imageUrls[0]으로 fallback |
| `weather` | null이면 날씨 영역 미표시 |

---

## 6. Risks and Mitigation

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| imageUrl → imageUrls 마이그레이션 실수 | High | Medium | Repository 레이어에서 fallback 로직 명시 |
| 이미지 압축 품질 저하 | Medium | Low | Bitmap.compress quality=85 기본값 적용 |
| HorizontalPager 스와이프와 달력 내부 터치 이벤트 충돌 | Medium | Medium | nestedScroll 또는 gestureDetector 우선순위 조정 |
| 기존 Firestore 문서 title 필드 표시 혼선 | Low | Low | 앱 레이어에서 title 필드 완전 무시 처리 |
| Storage 삭제 실패로 고아(orphan) 이미지 파일 잔존 | Medium | Medium | 삭제 실패 로그 기록 + Snackbar 안내, 추후 수동 정리 가이드 제공 |
| Lottie 라이브러리 추가로 APK 용량 증가 | Low | Low | `lottie-compose` 경량 사용, 애니메이션 JSON 파일 최소화 |

---

## 7. Architecture Considerations

### 7.1 변경이 필요한 파일

| 레이어 | 파일 | 변경 내용 |
|--------|------|-----------|
| Model | `DiaryEntry.kt` | title 제거, imageUrls 추가, weather 추가 |
| Model | `WeatherTag.kt` | 신규 생성 |
| Repository | `DiaryRepository.kt` / `DiaryRepositoryImpl.kt` | imageUrl fallback, weather 필드 처리 |
| UI | `DiaryEditorScreen.kt` | title 제거, 날씨 탭 추가, 이미지 3장 처리 |
| UI | `DiaryDetailScreen.kt` | 이미지 여백, 내용 Card, imageUrls 다중 표시 |
| UI | `HomeScreen.kt` | 달력 스와이프 제스처 추가 |
| UI | `theme/Color.kt` | 파스텔 피치/코랄 색상으로 교체 |
| UI | `theme/Theme.kt` | Seed Color 교체 |
| ViewModel | `DiaryViewModel.kt` | 이미지 압축 로직, Upsert 날짜 체크, 삭제 시 Storage 연동 삭제, 로딩 상태 관리 |
| Data | `StorageDataSource.kt` | 다중 이미지 업로드 + 단일/다중 이미지 삭제 지원 |
| App | `AndroidManifest.xml` | 앱 이름 "조이어리" 변경 |
| Res | `strings.xml` | app_name = "조이어리" |
| UI | `LoadingOverlay.kt` (신규) | Lottie 애니메이션 + 딤 배경 전체화면 오버레이 컴포저블 |
| Deps | `build.gradle.kts` | `lottie-compose` 의존성 추가 |
| Assets | `loading.json` (신규) | Lottie 로딩 애니메이션 JSON 파일 |

### 7.2 구현 순서 (의존성 기준)

```
1. Model 변경 (DiaryEntry, WeatherTag 신규)
2. Repository 변경 (하위 호환 fallback)
3. StorageDataSource 다중 이미지 업로드 + 삭제
4. ViewModel 변경 (압축, Upsert 체크, Storage 연동 삭제, 로딩 상태)
5. Theme 변경 (Color, Theme)
6. UI — LoadingOverlay 컴포저블 신규 생성
7. UI — EditorScreen (title 제거, 날씨 탭, 이미지 3장, 이미지 삭제, Lottie 로딩)
8. UI — DetailScreen (여백, Card)
9. UI — HomeScreen (스와이프 달력)
10. 앱 이름 변경 (Manifest, strings)
```

---

## 8. Convention Prerequisites

기존 v0.1.0 컨벤션 유지:
- MVVM + StateFlow + Hilt
- Kotlin Coroutines + Flow
- Material 3 + Jetpack Compose
- Firebase BaaS (Auth + Firestore + Storage)

---

## 9. Next Steps

1. [ ] Design 문서 작성 → `/pdca design joidiary-upgrade`
2. [ ] 구현 → `/pdca do joidiary-upgrade`
3. [ ] Gap 분석 → `/pdca analyze joidiary-upgrade`

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-17 | Initial draft (v0.2.0 업그레이드 계획) | faith79@jobkorea.co.kr |
| 0.2 | 2026-05-17 | SC-09~11 추가 (Storage 연동 삭제, Lottie 로딩) | faith79@jobkorea.co.kr |
