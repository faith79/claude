# joyary-upgrade Design Document

> **Summary**: 조이어리 Android 앱 UI/UX 업그레이드 — 하늘색 테마, 달력 레이아웃 개선, 스와이프 날짜 이동, EXIF 이미지 회전 보정
>
> **Project**: claude / diary-app
> **Version**: 0.2.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-17
> **Status**: Draft
> **Planning Doc**: [joyary-upgrade.plan.md](../../01-plan/features/joyary-upgrade.plan.md)

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 파스텔 코랄 테마가 사용자 취향과 맞지 않고, 달력 UI와 탐색 UX 개선이 필요하다 |
| **WHO** | 조이어리 앱을 사용하는 기존 사용자 (emotion 기록 + 일기 작성) |
| **RISK** | Coil EXIF 처리, HorizontalPager 날짜 범위 처리, 달력 sticky 레이아웃 |
| **SUCCESS** | FR-01~FR-11 에뮬레이터 시각적 확인, 기존 CRUD 정상 유지 |
| **SCOPE** | UI 레이어만 변경 — Color.kt, Theme.kt, HomeScreen.kt, DiaryDetailScreen.kt, DiaryEditorScreen.kt |

---

## 1. Overview

### 1.1 Design Goals

1. 전체 컬러 팔레트를 하늘색(sky blue) 파스텔 계열로 일관되게 교체
2. 달력 셀의 감정 이모지 가시성 향상 (위치/크기 변경, 빈 날 표시)
3. 달력을 화면 상단에 고정하고 아래 영역만 스크롤 가능하게 변경
4. 일기 상세보기에서 스와이프로 연속 날짜 탐색 가능하게 개선
5. EXIF 회전값 적용으로 이미지 올바른 방향 표시

### 1.2 Design Principles

- **기존 패턴 재사용**: HorizontalPager, Coil, CircleShape 등 이미 검증된 패턴 활용
- **데이터 레이어 무변경**: UI 변경이 ViewModel/Repository에 영향을 주지 않음
- **한 파일 한 책임**: 함수 분리로 파일 내 가독성 확보 (신규 파일 생성 없음)

---

## 2. Architecture Options

### 2.0 Architecture Comparison

| Criteria | Option A: Minimal | Option B: Clean | Option C: Pragmatic |
|----------|:-:|:-:|:-:|
| **New Files** | 0 | 3 | 0 |
| **Modified Files** | 5 | 5 | 5 |
| **Complexity** | Low | High | Medium |
| **Maintainability** | Medium | High | High |
| **Effort** | Low | High | **Medium** |
| **Risk** | Low | Low | Low |

**Selected**: **Option C — Pragmatic** — 신규 파일 없이 기존 패턴 재사용, 함수 분리로 가독성 확보

### 2.1 Component Diagram

```
HomeScreen.kt
├── CalendarHeader (기존 유지)
├── CalendarGrid (날짜 색상 추가)
│   └── DayCell [변경]
│       ├── EmotionEmoji (위, 24sp)  ← 순서 변경 + 크기 2배
│       ├── EmptyCircle (일기 없는 날) ← 신규
│       └── DateText (아래, 토=파랑/일=빨강) ← 색상 추가
└── Column(sticky calendar) [변경]

DiaryDetailScreen.kt
├── HorizontalPager(날짜 ±365) [신규 추가]
│   ├── EntryPage(entry != null) → 기존 상세 UI
│   └── EmptyPage(entry == null) → '일기 작성' 버튼
└── AsyncImage + exifOrientation [변경]
```

### 2.2 Data Flow

```
HomeScreen: pagerState(월) → settledPage → DiaryViewModel.loadMonth()
                                  ↓ onDateClick
DiaryDetailScreen: pagerState(날짜) → settledPage → DiaryViewModel.loadDiaryByDate()
                                          ↓ entry == null
                                    EmptyDiaryPage → onAddDiary(date)
```

### 2.3 Dependencies

| Component | Depends On | Purpose |
|-----------|-----------|---------|
| `DayCell` | `DiaryEntry?`, `YearMonth`, `dayOfWeek` | 감정아이콘/빈 원/날짜 렌더링 |
| `DiaryDetailScreen` | `DiaryViewModel`, `HorizontalPager` | 날짜 스와이프 + 상세 표시 |
| `AsyncImage` | `Coil ImageRequest` | EXIF 회전값 적용 이미지 로딩 |

---

## 3. Data Model

데이터 모델 변경 없음. 기존 `DiaryEntry`, `EmotionTag` 그대로 사용.

```kotlin
// 기존 모델 참조 (변경 없음)
data class DiaryEntry(
    val id: String,
    val date: String,       // "yyyy-MM-dd"
    val content: String,
    val emotion: EmotionTag?,
    val weather: WeatherTag?,
    val imageUrls: List<String>
)
```

---

## 4. 색상 팔레트 설계 (FR-01, FR-07)

### 4.1 하늘색 파스텔 팔레트

```kotlin
// Color.kt — 전체 교체
val SkyBlue          = Color(0xFF7EC8E3)  // Primary (Light) — 메인 하늘색
val SkyBlueLight     = Color(0xFFB3E5FC)  // PrimaryContainer — 연한 하늘색
val SkyBluePale      = Color(0xFFE1F5FE)  // SecondaryContainer
val SkyMint          = Color(0xFFA8DADC)  // Secondary — 민트 포인트
val SkyLavender      = Color(0xFFD4E6F1)  // Tertiary

// 배경색 (앱 배경 ≠ 달력 배경 — FR-07)
val SkyBackground    = Color(0xFFF0F8FF)  // 앱 배경 — AliceBlue
val SkySurface       = Color(0xFFFFFFFF)  // 달력 배경 — 흰색 (구분)
val SkyCalendarBg    = Color(0xFFE8F4FD)  // 달력 카드 배경 — 아주 연한 하늘

val SkyOnPrimary     = Color(0xFFFFFFFF)
val SkyDeepBlue      = Color(0xFF1565C0)  // OnPrimaryContainer
val SkyOnSurface     = Color(0xFF1A2A3A)  // 텍스트
val SkyError         = Color(0xFFE57373)

// 날짜 특수 색상 (FR-08)
val DateSaturday     = Color(0xFF1565C0)  // 토요일 파랑
val DateSunday       = Color(0xFFD32F2F)  // 일요일 빨강

// Dark 모드
val SkyDarkPrimary   = Color(0xFF81D4FA)
val SkyDarkContainer = Color(0xFF01579B)
val SkyDarkSurface   = Color(0xFF1A2535)
val SkyDarkBackground = Color(0xFF121C28)
```

### 4.2 달력 배경 vs 앱 배경 (FR-07)

```
앱 배경 (SkyBackground = #F0F8FF) — AliceBlue 연한 하늘
  └── 달력 Card 배경 (SkyCalendarBg = #E8F4FD) — 더 진한 하늘 (구분감)
        └── 달력 셀 Today (SkyBlueLight = #B3E5FC) — primaryContainer
```

---

## 5. UI/UX Design

### 5.1 HomeScreen 레이아웃 (FR-02, FR-03, FR-07)

```
┌─────────────────────────────────┐
│  TopAppBar (하늘색)               │  ← 고정
├─────────────────────────────────┤
│  Calendar Card (SkyCalendarBg)  │  ← 고정 (weight 없이 고정 높이)
│  ┌──────────────────────────┐   │
│  │  ◀ 2026년 5월 ▶          │   │
│  │  일 월 화 수 목 금 토      │   │
│  │  [셀][셀][셀]...(6행)     │   │  ← 세로 확대 (aspectRatio → height 60dp)
│  └──────────────────────────┘   │
├─────────────────────────────────┤
│  (검색결과 또는 여백)             │  ← 스크롤 가능
└─────────────────────────────────┘
```

**달력 고정 방식**: `Column { CalendarSection(); ScrollableContent() }`
- CalendarSection: `Card + Column(CalendarHeader + CalendarGrid)` — 고정
- ScrollableContent: `Box(Modifier.weight(1f).verticalScroll())` — 스크롤

### 5.2 DayCell 레이아웃 변경 (FR-04, FR-05, FR-06)

**현재 (Before)**:
```
Column {
  Text(day, 13sp)          ← 날짜 위
  Text(emotion.emoji, 12sp) ← 이모지 아래 (작음)
}
```

**변경 후 (After)**:
```
Column(height = 60dp) {
  // 위: 이모지 영역 (24sp, 항상 공간 확보)
  if (emotion != null)
    Text(emotion.emoji, 24sp)          ← 이모지 위 (2배)
  else
    EmptyCircle(size = 28dp)           ← 빈 동그라미 (일기 없는 날)

  // 아래: 날짜
  Text(day, 13sp, color = dateColor)  ← 날짜 아래 (토=파랑/일=빨강)
}
```

**EmptyCircle 구현**:
```kotlin
Box(
    Modifier
        .size(28.dp)
        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
)
```

**날짜 색상 (FR-08)**:
```kotlin
val dateColor = when (dayOfWeek) {
    DayOfWeek.SATURDAY -> DateSaturday  // 파랑
    DayOfWeek.SUNDAY   -> DateSunday    // 빨강
    else               -> MaterialTheme.colorScheme.onSurface
}
```

### 5.3 DiaryDetailScreen — 스와이프 날짜 이동 (FR-09, FR-10)

**구조 변경**:
```kotlin
// 변경 전: 단일 Scaffold
DiaryDetailScreen(date) { ... }

// 변경 후: HorizontalPager 감싸기
DiaryDetailScreen(date) {
    val dateRange = (-365..365)
    val initialPage = 365  // 현재 날짜가 중앙
    HorizontalPager(state = pagerState) { offset ->
        val targetDate = LocalDate.parse(date).plusDays(offset - 365L)
        val entry = diaryMap[targetDate]
        if (entry != null)
            DiaryEntryPage(entry)   // 기존 상세 UI
        else
            EmptyDiaryPage(targetDate, onAddDiary)  // 빈 화면 + 작성 버튼
    }
}
```

**EmptyDiaryPage 레이아웃 (FR-10)**:
```
┌─────────────────────────────────┐
│  TopAppBar (날짜 표시)            │
├─────────────────────────────────┤
│                                  │
│       (빈 공간)                  │
│                                  │
│   📝 아직 일기가 없어요            │
│                                  │
│   [+ 일기 쓰기]  ← FilledButton  │
│                                  │
│       (빈 공간)                  │
└─────────────────────────────────┘
```

### 5.4 Page UI Checklist

#### HomeScreen — 달력

- [ ] TopAppBar: "조이어리" 타이틀 + 검색 아이콘 + 설정 아이콘 (하늘색 테마)
- [ ] 달력 Card: 앱 배경과 다른 색상 (SkyCalendarBg)
- [ ] CalendarHeader: "◀ YYYY년 M월 ▶" 형태
- [ ] 요일 헤더: 일~토 7개 텍스트
- [ ] DayCell: 감정 이모지(위, 24sp) + 날짜(아래, 13sp)
- [ ] DayCell: 일기 없는 날 빈 동그라미(28dp, border) + 날짜
- [ ] DayCell: 토요일 날짜 파랑색(#1565C0)
- [ ] DayCell: 일요일 날짜 빨강색(#D32F2F)
- [ ] DayCell: 오늘 날짜 primaryContainer 배경
- [ ] FAB: + 버튼 (하늘색)

#### DiaryDetailScreen — 스와이프 상세

- [ ] TopAppBar: 날짜 타이틀 + 뒤로가기 + 수정/삭제 버튼 (일기 있을 때만)
- [ ] 이미지: EXIF 회전 보정된 올바른 방향 표시
- [ ] 감정/날씨 AssistChip 행
- [ ] 내용 Card
- [ ] 스와이프 제스처로 전날/다음날 이동
- [ ] 일기 없는 날: "아직 일기가 없어요" 문구 + "일기 쓰기" 버튼

---

## 6. Error Handling

| Scenario | Handling |
|----------|---------|
| 날짜 범위 초과 (±365일 밖) | 스와이프 비활성화 (pager 경계) |
| 이미지 EXIF 읽기 실패 | Coil 기본 처리 (원본 표시) |
| 일기 로딩 중 스와이프 | CircularProgressIndicator 표시 후 로드 완료 시 UI 갱신 |

---

## 7. Security Considerations

변경 없음 — UI 레이어만 수정, 데이터 접근 권한 로직 유지.

---

## 8. Test Plan

### 8.1 Test Scope

| Type | Target | Tool | Phase |
|------|--------|------|-------|
| L1: 시각 확인 | 색상, 레이아웃 | 에뮬레이터 육안 | Do |
| L2: 인터랙션 | 스와이프, 탭, 버튼 | 에뮬레이터 조작 | Do |
| L3: 회귀 | 기존 CRUD 정상 동작 | 에뮬레이터 + 조작 | Check |

### 8.2 L1: 시각 확인 시나리오

| # | 항목 | 확인 방법 | 예상 결과 |
|---|------|----------|---------|
| 1 | 전체 테마 색상 | 앱 실행 후 홈화면 확인 | TopAppBar, FAB, 버튼이 하늘색 계열 |
| 2 | 달력 배경 vs 앱 배경 | 홈화면에서 달력 영역 확인 | 달력 카드와 하단 배경이 다른 색 |
| 3 | DayCell 이모지 위치/크기 | 일기 작성한 날짜 셀 확인 | 이모지가 위(24sp), 날짜가 아래 |
| 4 | 빈 동그라미 | 일기 없는 날짜 셀 확인 | 원 테두리만 있는 동그라미 표시 |
| 5 | 토/일 색상 | 달력 토요일/일요일 날짜 확인 | 토=파랑, 일=빨강 |

### 8.3 L2: 인터랙션 시나리오

| # | 항목 | 조작 | 예상 결과 |
|---|------|------|---------|
| 1 | 상세 스와이프 전날 | 상세화면에서 오른쪽으로 스와이프 | 전날 일기 또는 빈 화면으로 이동 |
| 2 | 상세 스와이프 다음날 | 상세화면에서 왼쪽으로 스와이프 | 다음날 일기 또는 빈 화면으로 이동 |
| 3 | 빈 날 작성 버튼 | 일기 없는 날 상세에서 '일기 쓰기' 탭 | DiaryEditorScreen으로 이동 |
| 4 | 이미지 방향 | 카메라로 찍은 이미지 포함 일기 상세 | 이미지가 올바른 방향으로 표시 |

### 8.4 L3: 회귀 시나리오

| # | 항목 | 조작 | 예상 결과 |
|---|------|------|---------|
| 1 | 일기 작성 | FAB 탭 → 내용 입력 → 저장 | Firestore 저장 후 달력 셀 이모지 표시 |
| 2 | 일기 수정 | 상세화면 수정 아이콘 탭 | 에디터 이동, 수정 후 저장 정상 |
| 3 | 일기 삭제 | 삭제 다이얼로그 확인 | Firestore 삭제 후 달력 셀 초기화 |
| 4 | 월 이동 | 달력 화살표 / 스와이프 | 해당 월 데이터 정상 로드 |

---

## 9. Clean Architecture

### 9.1 Layer Assignment

| Component | Layer | Location |
|-----------|-------|----------|
| `DayCell` | Presentation | `HomeScreen.kt` (private fun) |
| `EmptyDiaryPage` | Presentation | `DiaryDetailScreen.kt` (private fun) |
| `Color.kt`, `Theme.kt` | Presentation (Theme) | `ui/theme/` |
| `DiaryViewModel` | Application | `viewmodel/` (변경 없음) |
| `DiaryRepository` | Infrastructure | `data/repository/` (변경 없음) |

---

## 10. Coding Convention Reference

### 10.1 이 기능의 컨벤션

| Item | Convention |
|------|-----------|
| 색상 명명 | `Sky` prefix (SkyBlue, SkyBackground, SkyCalendarBg) |
| 특수 날짜 색상 | `Date` prefix (DateSaturday, DateSunday) |
| private composable | 파일 하단에 `private fun` 으로 정의 |
| Coil 이미지 | `ImageRequest.Builder`에 `crossfade(true)` + `size(Size.ORIGINAL)` |

---

## 11. Implementation Guide

### 11.1 File Structure (변경 파일만)

```
diary-app/app/src/main/java/com/example/diaryapp/
├── ui/
│   ├── theme/
│   │   ├── Color.kt          ← [전체 교체] 하늘색 팔레트
│   │   └── Theme.kt          ← [수정] ColorScheme 참조 업데이트
│   ├── home/
│   │   └── HomeScreen.kt     ← [수정] 달력 고정 레이아웃, DayCell 리팩터링
│   └── diary/
│       ├── DiaryDetailScreen.kt  ← [수정] HorizontalPager + EXIF + EmptyPage
│       └── DiaryEditorScreen.kt  ← [수정] 이미지 미리보기 EXIF 처리
```

### 11.2 Implementation Order

1. [ ] **Module 1** — Color.kt + Theme.kt 교체 (테마 기반 먼저)
2. [ ] **Module 2** — HomeScreen.kt DayCell 리팩터링 (이모지 위치/크기/색상/빈 원)
3. [ ] **Module 3** — HomeScreen.kt 달력 고정 레이아웃
4. [ ] **Module 4** — DiaryDetailScreen.kt HorizontalPager 스와이프 + EmptyDiaryPage
5. [ ] **Module 5** — DiaryDetailScreen.kt + DiaryEditorScreen.kt EXIF 이미지 보정

### 11.3 Session Guide

#### Module Map

| Module | Scope Key | 대상 파일 | 예상 소요 |
|--------|-----------|----------|:--------:|
| 테마 교체 | `module-1` | Color.kt, Theme.kt | 1-2 turn |
| DayCell 리팩터링 | `module-2` | HomeScreen.kt | 2-3 turn |
| 달력 레이아웃 고정 | `module-3` | HomeScreen.kt | 1-2 turn |
| 상세 스와이프 | `module-4` | DiaryDetailScreen.kt | 3-4 turn |
| EXIF 이미지 | `module-5` | DiaryDetailScreen.kt, DiaryEditorScreen.kt | 1-2 turn |

#### Recommended Session Plan

| Session | Phase | Scope | 예상 턴 |
|---------|-------|-------|:------:|
| Session 1 | Plan + Design | 전체 | 완료 |
| Session 2 | Do | `--scope module-1,module-2,module-3` | 20-30 |
| Session 3 | Do | `--scope module-4,module-5` | 20-30 |
| Session 4 | Check + Report | 전체 | 15-20 |

---

## Key Implementation Notes

### Coil EXIF 회전 보정 (FR-11)

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(url)
        .crossfade(true)
        .size(Size.ORIGINAL)
        .build(),
    contentDescription = "일기 이미지",
    // Coil 2.x는 기본적으로 EXIF 회전을 적용함
    // 추가 설정 불필요 — ImageRequest가 자동 처리
)
```

> Coil 2.x는 `BitmapDecoder`가 `ExifOrientationPolicy.RESPECT_PERFORMANCE`를 기본 적용.
> `Size.ORIGINAL`을 명시하면 다운샘플링 없이 원본 EXIF 정보 기반 회전이 정확하게 적용됨.

### HorizontalPager 날짜 범위 (FR-09)

```kotlin
// DiaryDetailScreen에서 초기 날짜 기준 ±365일 (총 731페이지)
val BASE_DATE = LocalDate.parse(initialDate)
val TOTAL_PAGES = 731
val INITIAL_PAGE = 365

val pagerState = rememberPagerState(initialPage = INITIAL_PAGE) { TOTAL_PAGES }

// 페이지 → 날짜 변환
fun pageToDate(page: Int): LocalDate = BASE_DATE.plusDays((page - 365).toLong())
```

### 달력 고정 레이아웃 (FR-02)

```kotlin
Column(Modifier.fillMaxSize().padding(padding)) {
    // 달력 영역 — 고정 (weight 없음)
    Card(
        colors = CardDefaults.cardColors(containerColor = SkyCalendarBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalPager(state = pagerState) { page ->
            Column {
                CalendarHeader(...)
                CalendarGrid(...)
            }
        }
    }

    // 나머지 영역 — weight로 남은 공간 채움
    Box(Modifier.weight(1f)) {
        // 검색결과 또는 빈 공간
    }
}
```

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-17 | Initial draft — Option C Pragmatic 선택 | faith79@jobkorea.co.kr |
