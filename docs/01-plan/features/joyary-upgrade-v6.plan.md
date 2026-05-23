# joyary-upgrade-v6 Plan

> **Summary**: 이미지 100KB 제한 + 요일 표시 + 인메모리 캐시 + 에디터 키보드 스크롤 + 이미지 확대 레이어 + EXIF 회전 보정 + 검색 버튼 제거 + 알림 시간 정확히 동작
>
> **Project**: claude / diary-app
> **Version**: 0.6.0
> **Author**: faith79@jobkorea.co.kr
> **Date**: 2026-05-23
> **Status**: Draft

---

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 이미지 용량 과다(100KB 초과); 일기 상세에 요일 미표시; 매번 DB 호출로 느린 로딩; 키보드 출현 시 에디터 하단 내용 안 보임; 이미지 클릭해도 확대 불가; 업로드 이미지 EXIF 회전 불량; 달력에 불필요한 검색 버튼; 알림이 설정 시간에 발송되지 않음 |
| **Solution** | 이미지 압축 100KB 상한 + EXIF 보정; 날짜에 한글 요일 추가; 월별·날짜별 인메모리 캐시; imePadding으로 스크롤 확보; 이미지 탭 시 전체화면 오버레이; 검색 버튼 UI 제거; WorkManager initialDelay로 정확한 알림 시간 설정 |
| **Function/UX Effect** | 저장 용량 67% 추가 절감; 빠른 일기 조회로 달력·상세 로딩 제거; 에디터에서 키보드 위로 자유롭게 스크롤; 이미지 상세 확인 가능; 알림이 설정한 시간에 정확히 도착 |
| **Core Value** | "빠르고 쾌적한 조이어리" — 성능·UX의 마찰 요소 제거 |

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 실제 사용 중 체감되는 성능 저하(로딩, 알림 미동작)와 UI 불편(이미지 조작, 스크롤, 요일 부재)을 한 번에 해소 |
| **WHO** | 조이어리 기존 사용자 (사진 기록을 즐기고 매일 알림을 활용하는 사용자) |
| **RISK** | EXIF 처리 중 IOException 처리; WorkManager initialDelay 재계산 타이밍(설정 변경 시); 캐시 무효화 누락 시 구 데이터 노출 |
| **SUCCESS** | 이미지 ≤100KB 저장 + 회전 정상 + 달력/상세 캐시 로딩 + 에디터 키보드 스크롤 + 이미지 탭 확대 + 알림 정시 도착 |
| **SCOPE** | ImageCompressor, DiaryViewModel, DiaryDetailScreen, DiaryEditorScreen, HomeScreen, SettingsViewModel, DailyReminderWorker |

---

## 1. Overview

### 1.1 Purpose

v6는 8가지 독립된 UX/성능 개선을 묶어 처리한다:

1. **이미지 100KB 압축**: `maxSizeBytes` 307,200 → 102,400 + EXIF 회전 보정 (ExifInterface)
2. **날짜 + 요일 표시**: `DiaryDetailScreen` TopAppBar title에 한글 요일(토, 일, 월...) 병기
3. **인메모리 캐시**: `DiaryViewModel`에 `monthCache` + `entryCache` 추가, 저장/삭제 시 무효화
4. **에디터 키보드 스크롤**: `DiaryEditorScreen` Column에 `imePadding()` 추가
5. **이미지 확대 레이어**: `DiaryEntryContent` 이미지 탭 시 전체화면 오버레이 + X버튼
6. **EXIF 회전 보정**: 업로드 시 `ImageCompressor`에서 보정 처리
7. **검색 버튼 제거**: `HomeScreen` TopAppBar `Icons.Default.Search` 버튼 제거
8. **알림 시간 정확히 동작**: `scheduleReminder()`에 `setInitialDelay()` 계산 추가

### 1.2 Background

- v5에서 달력 6줄 고정·이미지 300KB·색상 커스터마이징 완료
- 일상 사용 중 발견된 성능·UI 불편 사항 8가지를 v6에서 일괄 개선

### 1.3 Related Documents

- v5 Plan: `docs/01-plan/features/joyary-upgrade-v5.plan.md`

---

## 2. Scope

### 2.1 In Scope

- [ ] **이미지 100KB 압축** (`ImageCompressor` maxSizeBytes = 102,400)
- [ ] **EXIF 회전 보정** (`ImageCompressor` — ExifInterface 회전각 읽기 → bitmap 회전)
- [ ] **날짜 + 요일 표시** (`DiaryDetailScreen` DiaryPageContent TopAppBar title)
- [ ] **인메모리 캐시** (`DiaryViewModel` — monthCache, entryCache; 저장/삭제 시 무효화)
- [ ] **에디터 키보드 스크롤** (`DiaryEditorScreen` Column에 `imePadding()`)
- [ ] **이미지 확대 레이어** (`DiaryDetailScreen` DiaryEntryContent — ImageViewerDialog)
- [ ] **검색 버튼 제거** (`HomeScreen` TopAppBar Search IconButton 제거)
- [ ] **알림 시간 정확히 동작** (`SettingsViewModel.scheduleReminder()` — initialDelay 계산)

### 2.2 Out of Scope

- 검색 기능 자체 삭제 (코드 유지, 버튼만 제거)
- 알림 클릭 시 앱 열기 PendingIntent 추가 (기존 동작 유지)
- 이미지 확대 시 좌우 스와이프 (단일 이미지 오버레이만)
- 오프라인 지원 / Firestore 영속 캐시 (인메모리만)

---

## 3. Requirements

### 3.1 Functional Requirements

| ID | 요구사항 | 우선순위 |
|----|---------|---------|
| FR-01 | 이미지 저장 시 100KB(102,400 bytes) 이하로 압축 보장 | Must |
| FR-02 | 업로드 이미지 EXIF 회전각 읽어 bitmap 회전 후 압축 저장 | Must |
| FR-03 | 일기 상세보기 TopAppBar title: "YYYY-MM-DD (요일)" 형태 표시 | Must |
| FR-04 | DiaryViewModel에 월별 캐시(monthCache) 추가 — 동일 월 재요청 시 캐시 반환 | Must |
| FR-05 | DiaryViewModel에 날짜별 캐시(entryCache) 추가 — 동일 날짜 재요청 시 캐시 반환 | Must |
| FR-06 | 저장/수정/삭제 시 관련 캐시 항목 무효화 | Must |
| FR-07 | DiaryEditorScreen Column에 imePadding() 적용 — 키보드 출현 시 스크롤 가능 | Must |
| FR-08 | DiaryDetailScreen 이미지 탭 시 전체화면 오버레이로 확대 표시 | Must |
| FR-09 | 이미지 오버레이 우측 상단 X버튼 클릭 시 닫기 | Must |
| FR-10 | HomeScreen TopAppBar에서 검색(돋보기) 버튼 제거 | Must |
| FR-11 | SettingsViewModel.scheduleReminder()에 initialDelay 계산 추가 (다음 reminderHour:reminderMinute까지의 지연) | Must |

### 3.2 Non-Functional Requirements

| 카테고리 | 기준 |
|---------|------|
| 이미지 용량 | 저장 후 ≤ 100KB 보장 |
| 캐시 유효성 | 저장/수정/삭제 후 해당 캐시 항목 즉시 무효화 |
| 알림 정확도 | 설정 시간 ±1분 이내 발송 |
| 기존 회귀 없음 | v5 통합 테마, 로그인, 일기 CRUD 동작 유지 |
| 외부 라이브러리 | ExifInterface(androidX 기존 포함), 추가 의존성 없음 |

---

## 4. 파일 영향 범위

| 파일 | 변경 유형 | 주요 내용 |
|------|---------|---------|
| `data/util/ImageCompressor.kt` | 수정 | maxSizeBytes → 102,400; EXIF 회전 보정 추가 |
| `ui/diary/DiaryDetailScreen.kt` | 수정 | TopAppBar title에 요일 추가; 이미지 탭 → ImageViewerDialog |
| `ui/diary/DiaryEditorScreen.kt` | 수정 | Column에 imePadding() 추가 |
| `ui/home/HomeScreen.kt` | 수정 | TopAppBar Search IconButton 제거 |
| `viewmodel/DiaryViewModel.kt` | 수정 | monthCache + entryCache; loadMonth/loadDiaryByDate 캐시 조회; 저장/삭제 시 무효화 |
| `viewmodel/SettingsViewModel.kt` | 수정 | scheduleReminder()에 initialDelay 계산 (LocalDateTime 기반) |

**신규 0개 / 수정 6개**

---

## 5. Technical Design

### 5.1 이미지 100KB + EXIF 회전 보정 (ImageCompressor)

```kotlin
private val maxSizeBytes = 102_400L  // 100 × 1024 bytes

fun compress(uri: Uri): ByteArray {
    // 1. EXIF 회전각 읽기
    val rotation = context.contentResolver.openInputStream(uri)?.use { stream ->
        val exif = ExifInterface(stream)
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } ?: 0f

    // 2. 원본 bitmap 디코딩
    val options = BitmapFactory.Options().apply { inSampleSize = 1 }
    var bitmap = context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    } ?: return ByteArray(0)

    // 3. 회전 적용
    if (rotation != 0f) {
        val matrix = Matrix().apply { postRotate(rotation) }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // 4. 해상도 축소 후 quality 감소
    var quality = 90
    var output: ByteArray
    do {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        output = stream.toByteArray()
        quality -= 10
    } while (output.size > maxSizeBytes && quality > 10)

    bitmap.recycle()
    return output
}
```

### 5.2 날짜 + 요일 (DiaryDetailScreen)

```kotlin
private fun formatDateWithDay(dateStr: String): String {
    val date = LocalDate.parse(dateStr)
    val dayNames = listOf("월", "화", "수", "목", "금", "토", "일")
    val dayName = dayNames[date.dayOfWeek.value - 1]
    return "$dateStr ($dayName)"
}
// TopAppBar title = { Text(formatDateWithDay(date)) }
```

### 5.3 인메모리 캐시 (DiaryViewModel)

```kotlin
// Key: "userId_yearMonth" (예: "abc123_2026-05")
private val monthCache = mutableMapOf<String, List<DiaryEntry>>()
// Key: "userId_date" (예: "abc123_2026-05-23")
private val entryCache = mutableMapOf<String, DiaryEntry?>()

fun loadMonth(userId: String, yearMonth: YearMonth) {
    val key = "${userId}_${yearMonth}"
    monthCache[key]?.let { cached ->
        _diaries.value = cached
        return
    }
    // Firestore 호출 후 캐시 저장
}

fun loadDiaryByDate(userId: String, date: String) {
    val key = "${userId}_${date}"
    if (entryCache.containsKey(key)) {
        _selectedEntry.value = entryCache[key]
        return
    }
    // Firestore 호출 후 캐시 저장
}

// 저장/수정/삭제 성공 시 관련 캐시 무효화
private fun invalidateCache(userId: String, date: String) {
    val yearMonth = date.substring(0, 7)  // "YYYY-MM"
    monthCache.remove("${userId}_${yearMonth}")
    entryCache.remove("${userId}_${date}")
}
```

### 5.4 에디터 키보드 스크롤 (DiaryEditorScreen)

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .imePadding()               // ← 추가: 키보드 높이만큼 패딩 확보
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
)
```

### 5.5 이미지 확대 레이어 (DiaryDetailScreen)

```kotlin
var selectedImageUrl by remember { mutableStateOf<String?>(null) }

// AsyncImage에 클릭 추가
Modifier.clickable { selectedImageUrl = url }

// 오버레이
selectedImageUrl?.let { url ->
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable { selectedImageUrl = null }
    ) {
        AsyncImage(
            model = url,
            contentDescription = "확대 이미지",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = { selectedImageUrl = null },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Close, "닫기", tint = Color.White)
        }
    }
}
```

### 5.6 알림 시간 정확히 동작 (SettingsViewModel)

```kotlin
private fun scheduleReminder() {
    val hour = notificationPreferences.reminderHour
    val minute = notificationPreferences.reminderMinute
    val now = LocalDateTime.now()
    val target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    val nextTarget = if (target.isAfter(now)) target else target.plusDays(1)
    val initialDelay = ChronoUnit.MILLIS.between(now, nextTarget)

    val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .build()
    workManager.enqueueUniquePeriodicWork(
        "daily_reminder",
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    )
}
```

---

## 6. Success Criteria

| # | 기준 | 검증 방법 |
|---|------|---------|
| SC-01 | 사진 업로드 후 저장된 이미지가 100KB 이하 | 로그 또는 Firebase Storage 파일 크기 확인 |
| SC-02 | 90도 회전 이미지 업로드 후 정상 방향으로 표시 | 에뮬레이터/실기기 확인 |
| SC-03 | 일기 상세 TopAppBar에 "2026-05-23 (토)" 형태 표시 | 에뮬레이터 시각 확인 |
| SC-04 | 같은 달 재방문 시 로딩 인디케이터 없이 즉시 표시 | 에뮬레이터 확인 |
| SC-05 | 같은 날짜 상세 재방문 시 즉시 표시 | 에뮬레이터 확인 |
| SC-06 | 일기 저장/삭제 후 달력 + 상세 캐시가 갱신됨 | 저장 후 재방문 시 최신 데이터 확인 |
| SC-07 | 에디터에서 키보드 출현 시 스크롤로 하단 내용 확인 가능 | 에뮬레이터 키보드 표시 확인 |
| SC-08 | 일기 상세에서 이미지 탭 시 전체화면 오버레이 표시 | 에뮬레이터 탭 확인 |
| SC-09 | 오버레이 X버튼 탭 시 이미지 닫힘 | 에뮬레이터 확인 |
| SC-10 | 달력 TopAppBar에 검색(돋보기) 버튼 없음 | 에뮬레이터 UI 확인 |
| SC-11 | 알림 설정 후 지정 시간에 알림 수신 | 실기기 또는 에뮬레이터 시간 조작 확인 |
| SC-12 | v5 통합 테마, 로그인, 일기 CRUD 회귀 없음 | 기능 테스트 |

---

## 7. Risks & Mitigation

| Risk | 심각도 | 대응 |
|------|--------|------|
| ExifInterface IOException (스트림 두 번 열기) | Medium | contentResolver.openInputStream 두 번 호출 — 안전하게 use 블록으로 각각 처리 |
| 캐시 무효화 누락 시 구 데이터 표시 | Medium | saveDiary/deleteDiary 성공 onSuccess 콜백에서만 invalidateCache 호출 |
| WorkManager initialDelay 계산 오류 (자정 경계) | Low | nextTarget = if (target.isAfter(now)) target else target.plusDays(1) 로 처리 |
| 100KB 이하 달성 불가 이미지 (극단적 고해상도) | Low | quality=10 도달 시 그대로 저장 (기존 동작 유지); 필요 시 inSampleSize 2배 추가 가능 |
| 이미지 오버레이 중 시스템 Back 버튼 | Low | BackHandler로 selectedImageUrl 초기화 처리 |

---

## 8. Next Steps

1. [ ] `/pdca do joyary-upgrade-v6` — 구현 시작
2. [ ] `/pdca analyze joyary-upgrade-v6` — Gap Analysis
3. [ ] `/pdca report joyary-upgrade-v6` — 완료 보고서

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 0.1 | 2026-05-23 | Initial draft (8가지 UX/성능 개선) | faith79@jobkorea.co.kr |
