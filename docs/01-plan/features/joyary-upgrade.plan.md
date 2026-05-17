# joyary-upgrade Planning Document

> **Summary**: 조이어리 Android 앱 UI/UX 업그레이드 — 하늘색 테마, 달력 레이아웃 개선, 상세 스와이프 이동, 이미지 회전 보정
>
> **Project**: claude / diary-app
> **Version**: 0.2.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-17
> **Status**: Ready

---

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 현재 앱의 파스텔 코랄/핑크 테마와 달력 UI가 사용자 취향에 맞지 않고, 상세보기에서 날짜 간 이동이 불편하며, 이미지가 회전되어 표시되는 문제가 있다 |
| **Solution** | 전체 테마를 하늘색 계열로 교체, 달력 셀 레이아웃 개선(아이콘 위/날짜 아래, 2배 크기, 빈 동그라미), 상세보기 스와이프 네비게이션 추가, EXIF 기반 이미지 회전 보정 |
| **Function/UX Effect** | 더 밝고 청량한 분위기, 달력 가독성 향상, 날짜 간 연속 탐색 가능, 이미지 정방향 표시로 전체적인 앱 품질 향상 |
| **Core Value** | 시각적으로 쾌적하고 날짜 탐색이 자연스러운 일기 앱 경험 제공 |

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 파스텔 코랄 테마가 사용자 취향과 맞지 않고, 달력 UI와 탐색 UX 개선이 필요하다 |
| **WHO** | 조이어리 앱을 사용하는 기존 사용자 (emotion 기록 + 일기 작성) |
| **RISK** | Coil 이미지 라이브러리의 EXIF 처리 설정, HorizontalPager 기반 스와이프 날짜 범위 처리, 달력 셀 높이 고정 레이아웃 조정 |
| **SUCCESS** | 11개 요구사항 모두 에뮬레이터에서 정상 동작, 이미지 올바른 방향 표시, 스와이프로 연속 날짜 이동 가능 |
| **SCOPE** | UI 레이어만 변경 (Color.kt, Theme.kt, HomeScreen.kt, DiaryDetailScreen.kt) — Firebase/데이터 레이어 무변경 |

---

## 1. Overview

### 1.1 Purpose

조이어리 앱의 시각적 완성도와 UX를 높이기 위한 UI 업그레이드.
데이터/비즈니스 로직은 변경하지 않고 UI 레이어만 개선한다.

### 1.2 Background

- 기존 파스텔 코랄/핑크 계열 테마를 사용자가 하늘색 계열로 교체 요청
- 달력 셀이 날짜(위) + 감정아이콘(아래) 순서로 구성되어 있어 감정아이콘이 작고 잘 안 보임
- 일기 상세보기에서 뒤로가기 후 다른 날짜 선택 방식이 번거로움
- 갤러리/카메라로 찍은 이미지가 EXIF 회전값 무시로 잘못된 방향으로 표시됨

### 1.3 Related Documents

- 이전 Plan: `docs/archive/2026-05/diary-app/diary-app.plan.md`
- 이전 Design: `docs/archive/2026-05/diary-app/diary-app.design.md`

---

## 2. Scope

### 2.1 In Scope

- [x] **FR-01** 전체 컬러 테마를 하늘색(sky blue) 파스텔 계열로 변경 (`Color.kt`, `Theme.kt`)
- [x] **FR-02** 달력을 화면 상단에 고정 (sticky header 방식)
- [x] **FR-03** 달력 세로 높이 확대 (셀 aspectRatio 또는 고정 높이 조정)
- [x] **FR-04** 달력 셀 감정 이모지 크기 2배 (현재 12sp → 24sp)
- [x] **FR-05** 달력 셀 레이아웃 변경: 감정아이콘(위) + 날짜(아래)
- [x] **FR-06** 일기 없는 날 / 감정 없는 날: 이모지 크기의 빈 동그라미 표시
- [x] **FR-07** 달력 배경색과 앱 배경색 구분 (서로 다른 색상)
- [x] **FR-08** 토요일 날짜 파랑색, 일요일 날짜 빨강색
- [x] **FR-09** 일기 상세보기에서 스와이프로 전날/다음날 이동 (HorizontalPager)
- [x] **FR-10** 일기 없는 날 상세보기: 빈 화면 + '일기 작성' 버튼 노출, 스와이프 이동 유지
- [x] **FR-11** 이미지 EXIF 회전값 적용하여 올바른 방향으로 표시

### 2.2 Out of Scope

- Firebase/Firestore 데이터 모델 변경
- 인증 로직 변경
- 새로운 기능 추가 (검색, 알림 등)
- iOS 지원
- Dark 모드 대응 (Light 모드만 우선)

---

## 3. Requirements

### 3.1 Functional Requirements

| ID | Requirement | Priority | 영향 파일 | Status |
|----|-------------|----------|----------|--------|
| FR-01 | 전체 테마 하늘색 파스텔로 교체 | High | `Color.kt`, `Theme.kt` | Pending |
| FR-02 | 달력 화면 상단 고정 (스크롤 시 달력 유지) | High | `HomeScreen.kt` | Pending |
| FR-03 | 달력 세로 높이 확대 | High | `HomeScreen.kt` (DayCell) | Pending |
| FR-04 | 감정 이모지 크기 2배 (12sp → 24sp) | High | `HomeScreen.kt` (DayCell) | Pending |
| FR-05 | 감정아이콘 위, 날짜 아래 순서로 변경 | High | `HomeScreen.kt` (DayCell) | Pending |
| FR-06 | 일기/감정 없는 날 → 빈 동그라미 표시 | Medium | `HomeScreen.kt` (DayCell) | Pending |
| FR-07 | 달력 배경색 ≠ 앱 배경색 | Medium | `HomeScreen.kt`, `Color.kt` | Pending |
| FR-08 | 토요일 파랑, 일요일 빨강 날짜 텍스트 | Medium | `HomeScreen.kt` (CalendarGrid) | Pending |
| FR-09 | 상세보기 HorizontalPager 스와이프 날짜 이동 | High | `DiaryDetailScreen.kt`, `NavGraph.kt` | Pending |
| FR-10 | 일기 없는 날 상세: 작성 버튼 + 스와이프 가능 | High | `DiaryDetailScreen.kt` | Pending |
| FR-11 | 이미지 EXIF 회전 보정 | Medium | `DiaryDetailScreen.kt`, `DiaryEditorScreen.kt` | Pending |

### 3.2 Non-Functional Requirements

| Category | Criteria | Measurement Method |
|----------|----------|-------------------|
| Performance | 달력 렌더링 지연 없음 (기존 대비 동등) | 육안 확인 |
| UX | 스와이프 애니메이션 부드러움 | 에뮬레이터/실기기 확인 |
| Compatibility | Android 8.0 (API 26) 이상 유지 | `minSdk = 26` |

---

## 4. Success Criteria

### 4.1 Definition of Done

- [ ] FR-01~FR-11 모두 에뮬레이터에서 시각적으로 확인
- [ ] 하늘색 파스텔 테마가 TopAppBar, FAB, 달력, 버튼에 일관되게 적용됨
- [ ] 달력이 스크롤 시에도 화면 상단에 고정됨
- [ ] 달력 셀: 감정 이모지(위, 24sp) + 날짜(아래) 순서 확인
- [ ] 일기 없는 날 셀에 빈 동그라미(24sp 기준 원) 표시
- [ ] 토요일 날짜 텍스트 파랑, 일요일 빨강
- [ ] 상세보기에서 스와이프로 전날/다음날 이동
- [ ] 일기 없는 날 상세보기: '일기 작성' 버튼 표시
- [ ] 이미지 올바른 방향으로 표시

### 4.2 Quality Criteria

- [ ] 기존 CRUD 기능(작성/수정/삭제) 정상 동작 유지
- [ ] 달력 월 이동(화살표/스와이프) 정상 동작 유지
- [ ] Coil 이미지 로딩 실패 시 placeholder 유지

---

## 5. Risks and Mitigation

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Coil EXIF 처리: `crossfade`와 `exifOrientation` 동시 설정 충돌 | Medium | Medium | `ImageRequest.Builder`에 `exifOrientation(true)` 명시 + 테스트 |
| 달력 sticky 처리 시 LazyColumn 내부 `stickyHeader` 적용 어려움 | Medium | Medium | `Column + weight` 레이아웃으로 달력 고정, 나머지 영역 스크롤 |
| HorizontalPager 날짜 범위 처리 (첫날/마지막날 경계) | Low | Low | 날짜 ±365일 범위 설정, 경계에서 스와이프 비활성화 |
| 기존 HomeScreen의 HorizontalPager(월 스와이프)와 상세 HorizontalPager(날짜 스와이프) 충돌 | Low | Low | 상세보기는 별도 Composable에서 독립된 pagerState 사용 |

---

## 6. Impact Analysis

### 6.1 Changed Resources

| Resource | Type | Change Description |
|----------|------|--------------------|
| `Color.kt` | UI Theme | 코랄/핑크 → 하늘색 파스텔 팔레트 전체 교체 |
| `Theme.kt` | UI Theme | ColorScheme 참조 색상 업데이트 |
| `HomeScreen.kt` | UI Composable | DayCell 레이아웃, CalendarGrid 날짜 색상, 달력 고정 레이아웃 |
| `DiaryDetailScreen.kt` | UI Composable | HorizontalPager 스와이프, 빈 날 UI, EXIF 이미지 처리 |
| `DiaryEditorScreen.kt` | UI Composable | 이미지 표시 시 EXIF 회전 적용 (미리보기) |

### 6.2 Current Consumers

| Resource | Operation | Code Path | Impact |
|----------|-----------|-----------|--------|
| `Color.kt` | READ | `Theme.kt` → `PastelLightColorScheme` | Breaking (색상 전체 교체) |
| `Theme.kt` | READ | `DiaryApp.kt` → `DiaryAppTheme` | None (API 불변) |
| `DayCell` | READ | `CalendarGrid` → `items()` | Needs verification |
| `DiaryDetailScreen` | READ | `NavGraph.kt` → `composable(Screen.DiaryDetail)` | 스와이프 도입으로 파라미터 확장 가능성 |

### 6.3 Verification

- [ ] 기존 HomeScreen 월 스와이프 정상 동작 확인
- [ ] DiaryDetailScreen 수정/삭제 기능 이상 없음 확인
- [ ] NavGraph에서 DiaryDetailScreen 호출 파라미터 호환성 확인

---

## 7. Architecture Considerations

### 7.1 Project Level Selection

| Level | Characteristics | Selected |
|-------|-----------------|:--------:|
| **Starter** | 단순 구조 | ☐ |
| **Dynamic** | Feature 기반 모듈, BaaS 연동 | ✅ |
| **Enterprise** | 레이어 분리, DI, 마이크로서비스 | ☐ |

### 7.2 Key Architectural Decisions

| Decision | Options | Selected | Rationale |
|----------|---------|----------|-----------|
| 달력 고정 방식 | `stickyHeader` / `Column+weight` / `Box` | `Column(Modifier.weight)` 내 달력 고정 | `stickyHeader`는 `LazyColumn` 내부에서만 동작하므로 현재 구조에 맞게 Column 레이아웃으로 달력 영역 고정 |
| 상세 날짜 스와이프 | `HorizontalPager` / `SwipeToDismiss` | `HorizontalPager` | 기존 HomeScreen과 동일한 패턴, 애니메이션 자연스러움 |
| 이미지 EXIF 처리 | `Coil exifOrientation` / `ExifInterface 수동` | `Coil ImageRequest.Builder exifOrientation` | Coil 2.x 기본 지원, 추가 라이브러리 불필요 |
| 빈 날 동그라미 | `Canvas` / `Box+Border` / `Box+background(CircleShape)` | `Box + border(CircleShape)` | 기존 CircleShape 사용 패턴 일관성 |

### 7.3 변경 파일 구조

```
diary-app/app/src/main/java/com/example/diaryapp/
├── ui/
│   ├── theme/
│   │   ├── Color.kt          ← 하늘색 파스텔 팔레트로 전체 교체
│   │   └── Theme.kt          ← ColorScheme 참조 업데이트
│   ├── home/
│   │   └── HomeScreen.kt     ← DayCell 레이아웃, 달력 고정, 날짜 색상
│   └── diary/
│       ├── DiaryDetailScreen.kt  ← HorizontalPager 스와이프, EXIF 이미지
│       └── DiaryEditorScreen.kt  ← 이미지 미리보기 EXIF 처리
```

---

## 8. Convention Prerequisites

### 8.1 Existing Project Conventions

- [x] Kotlin 코딩 컨벤션 적용 중
- [x] Material Design 3 가이드라인 준수 (`MaterialTheme.colorScheme`)
- [x] Jetpack Compose 선언형 UI
- [x] Coil (`AsyncImage`) 이미지 로딩

### 8.2 Conventions to Define/Verify

| Category | To Define | Priority |
|----------|-----------|:--------:|
| 색상 명명 | `SkyBlue` prefix로 통일 | High |
| 달력 셀 높이 | `aspectRatio` vs 고정 `height` | Medium |

---

## 9. Next Steps

1. [ ] Design 문서 작성 → `/pdca design joyary-upgrade`
2. [ ] 구현 시작 → `/pdca do joyary-upgrade`
3. [ ] Gap 분석 → `/pdca analyze joyary-upgrade`

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-17 | Initial draft — UI/UX 업그레이드 11개 요구사항 | faith79@jobkorea.co.kr |
