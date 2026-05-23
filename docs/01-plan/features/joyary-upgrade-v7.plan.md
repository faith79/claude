# joyary-upgrade-v7 Plan

> **Phase**: Plan
> **Date**: 2026-05-23
> **Feature**: joyary-upgrade-v7
> **Author**: faith79@jobkorea.co.kr

---

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | 이미지 전체화면 오버레이에서 이미지가 1장씩만 표시돼 스와이프 탐색 불가; 기존 캐시에 만료 기간이 없어 오래된 데이터가 무한 보관됨 |
| **Solution** | 오버레이에 HorizontalPager 적용해 이미지 스와이프 탐색; 캐시에 24시간 TTL 추가 + 만료 엔트리 자동 삭제 |
| **Function/UX Effect** | 이미지 여러 장을 한 손으로 스와이프 탐색; 하루가 지나면 캐시가 자동 정리돼 메모리 낭비 없음 |
| **Core Value** | "빠르고 쾌적한 조이어리" — 이미지 탐색 편의성 향상 + 메모리 건강성 확보 |

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 이미지 여러 장 스와이프 탐색 미지원 + 캐시 무기한 보관으로 메모리 누수 가능성 |
| **WHO** | 조이어리 일상 사용자 (사진을 여러 장 기록하는 사용자) |
| **RISK** | HorizontalPager 페이지 상태와 overlay 닫기 간 충돌; TTL 만료 체크 성능 오버헤드 |
| **SUCCESS** | 이미지 스와이프 동작 + 페이지 인디케이터 표시 + 24h TTL 적용 + 만료 캐시 자동 정리 |
| **SCOPE** | DiaryDetailScreen.kt, DiaryViewModel.kt |

---

## 1. Requirements

### 1.1 Functional Requirements

| ID | 요구사항 | 우선순위 |
|----|---------|---------|
| FR-01 | 이미지 전체화면 오버레이에서 HorizontalPager로 이미지 좌우 스와이프 탐색 | Must |
| FR-02 | 오버레이 하단에 현재 이미지 인덱스 표시 (예: "2 / 5") | Must |
| FR-03 | 달력 월별 캐시(monthCache)에 24시간 TTL 적용 | Must |
| FR-04 | 일기 상세 캐시(entryCache)에 24시간 TTL 적용 | Must |
| FR-05 | 캐시 조회 시 만료 엔트리 자동 제거 (lazy eviction) | Must |
| FR-06 | ViewModel init 시 전체 만료 캐시 일괄 정리 | Should |

### 1.2 Non-Functional Requirements

| ID | 요구사항 |
|----|---------|
| NFR-01 | TTL 체크는 O(1) 연산 — 성능 영향 없음 |
| NFR-02 | v6 기능 회귀 없음 (이미지 업로드, EXIF, 알림, 검색 제거 등) |

---

## 2. Success Criteria

| ID | 기준 |
|----|------|
| SC-01 | 오버레이에서 이미지 2장 이상일 때 좌우 스와이프로 이미지 전환 |
| SC-02 | 오버레이 하단 "N / M" 인디케이터 표시 |
| SC-03 | monthCache 엔트리 24h 경과 시 미스 처리 → 재호출 |
| SC-04 | entryCache 엔트리 24h 경과 시 미스 처리 → 재호출 |
| SC-05 | 만료 엔트리 lazy 제거 (캐시 조회 시점) |
| SC-06 | ViewModel 생성 시 기존 만료 엔트리 일괄 삭제 |
| SC-07 | v6 기능 회귀 없음 |

---

## 3. Technical Design Sketch

### 3.1 이미지 스와이프 오버레이 (FR-01, FR-02)

**변경 전:**
```kotlin
var selectedImageUrl by remember { mutableStateOf<String?>(null) }
// onImageClick = { url -> selectedImageUrl = url }
// overlay: Box { AsyncImage(url) }
```

**변경 후:**
```kotlin
var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
val overlayImages = entry?.imageUrls ?: emptyList()
// onImageClick = { index -> selectedImageIndex = index }
// overlay: HorizontalPager + page indicator
```

### 3.2 캐시 TTL (FR-03, FR-04, FR-05)

```kotlin
private data class CachedValue<T>(val data: T, val cachedAt: Long = System.currentTimeMillis())

private val TTL_MS = 24 * 60 * 60 * 1000L  // 24시간

private fun <T> CachedValue<T>.isValid() = System.currentTimeMillis() - cachedAt < TTL_MS

// monthCache: Map<String, CachedValue<List<DiaryEntry>>>
// entryCache: Map<String, CachedValue<DiaryEntry?>>
```

---

## 4. Scope

| 파일 | 변경 내용 |
|------|---------|
| `ui/diary/DiaryDetailScreen.kt` | selectedImageUrl → selectedImageIndex; HorizontalPager 오버레이; 페이지 인디케이터 |
| `viewmodel/DiaryViewModel.kt` | CachedValue<T> 래퍼 추가; TTL 체크 + lazy eviction; init 블록 정리 |
