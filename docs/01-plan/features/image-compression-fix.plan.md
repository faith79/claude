# image-compression-fix Plan

> **Phase**: Plan
> **Date**: 2026-05-23
> **Feature**: image-compression-fix (버그픽스)
> **Author**: faith79@jobkorea.co.kr

---

## Executive Summary

| Perspective | Content |
|-------------|---------|
| **Problem** | `ImageCompressor.kt` 해상도 미축소 + quality 루프 오프바이원 버그로 고화소 사진이 100KB를 초과해 저장됨 |
| **Solution** | `inJustDecodeBounds`로 원본 크기 확인 후 단계별 최대 해상도(1280→800→480→240px)로 축소; quality 루프 조건을 `>= 10`으로 수정 |
| **Function/UX Effect** | 어떤 해상도의 사진이든 100KB 이하로 안정적 저장; 화질 저하 최소화 |
| **Core Value** | 100KB 제한 실제 동작 보장 |

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 고화소 스마트폰 사진이 quality 감소만으로 100KB 도달 불가 |
| **WHO** | 사진을 찍어 업로드하는 모든 조이어리 사용자 |
| **RISK** | 극단적 저해상도 축소 시 화질 과다 저하 → 단계별 시도로 최적 화질 유지 |
| **SUCCESS** | 어떤 사진이든 최종 저장 bytes ≤ 102_400 |
| **SCOPE** | `data/util/ImageCompressor.kt` (1파일) |

---

## Root Cause Analysis

### Bug 1 — 해상도 미축소
```kotlin
// 현재 (문제)
val options = BitmapFactory.Options().apply { inSampleSize = 1 }
// inSampleSize = 1 → 원본 해상도 그대로 디코딩 (4000×3000 = 12MP)
// quality=80 JPEG at 4000×3000 ≈ 1~3MB → 100KB 절대 불가
```

### Bug 2 — quality 루프 오프바이원
```kotlin
// 현재 (문제)
} while (output.size > maxSizeBytes && quality > 10)
// quality=20 → quality 10으로 감소 → 10 > 10 = false → 루프 종료
// 실제 최소 quality는 20 (10이 아님)

// 수정
} while (output.size > maxSizeBytes && quality >= 10)
```

---

## 2. Success Criteria

| ID | 기준 |
|----|------|
| SC-01 | 고화소 사진(4K+)도 최종 저장 size ≤ 102_400 bytes |
| SC-02 | 저해상도 사진은 최초 maxDim=1280 시도에서 바로 통과 (화질 보존) |
| SC-03 | quality 루프가 10까지 포함해서 시도 |
| SC-04 | EXIF 회전 보정 유지 (v6 기능 회귀 없음) |
| SC-05 | `calculateInSampleSize` 헬퍼 함수로 inSampleSize 계산 분리 |

---

## 3. 수정 전략

단계별 maxDimension을 줄여가며 시도:
```
maxDim=1280 → quality 85~10 시도 → 성공이면 반환
maxDim=800  → quality 85~10 시도 → 성공이면 반환
maxDim=480  → quality 85~10 시도 → 성공이면 반환
maxDim=240  → quality 85~10 시도 → 성공이면 반환
fallback: ByteArray(0) (극단적 케이스, 정상 사진에서는 도달 불가)
```
