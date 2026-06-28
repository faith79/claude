# joyary-upgrade-v5 Gap Analysis

> **Feature**: joyary-upgrade-v5
> **Date**: 2026-05-17
> **Analyst**: Claude Code (Check Phase)
> **Match Rate**: 100%

---

## Context Anchor

| Key | Value |
|-----|-------|
| **WHY** | 달력 높이 불일치 UX 이질감; 이미지 용량 과다; 일기 배경/평일색 커스터마이징 불가 |
| **WHO** | 조이어리 기존 사용자 (UI 일관성 + 색상 커스터마이징) |
| **RISK** | ThemeColors 필드 추가 시 AppThemeTemplate 충돌; LazyVerticalGrid 빈 셀 처리 |
| **SUCCESS** | 모든 달 동일 높이 + ≤300KB 이미지 + 설정에서 색상 즉시 반영 |
| **SCOPE** | HomeScreen(달력), ImageCompressor, ThemeColors/Preferences/ViewModel/Screen(색상), DiaryEditor/Detail(배경) |

---

## 1. Strategic Alignment Check

| 검증 항목 | 결과 | 비고 |
|---------|------|------|
| 달력 높이 불일치 해결 | ✅ | `while (size < 42) add(null)` — 모든 달 42셀 고정 |
| 이미지 용량 과다 해결 | ✅ | `maxSizeBytes = 307_200L` (300KB) |
| 일기 배경색 통일 | ✅ | DiaryEditor + DiaryDetail 모두 `diaryBg` 사용 |
| 색상 커스터마이징 가능 | ✅ | SettingsScreen에 ColorPaletteRow 2개 추가 |
| AppThemeTemplate 무수정 | ✅ | ThemeColors 기본값 파라미터 → 10개 인스턴스 무수정 |

---

## 2. Plan Success Criteria 검증

| SC | 기준 | 상태 | 증거 |
|----|------|------|------|
| SC-01 | 4월↔5월 스와이프 시 달력 높이 동일 | ✅ Met | `HomeScreen.kt:281` — `while (size < 42) add(null)` |
| SC-02 | 사진 저장 후 ≤300KB | ✅ Met | `ImageCompressor.kt:18` — `maxSizeBytes = 307_200L` |
| SC-03 | 일기 작성·보기 화면 배경 동일 | ✅ Met | `DiaryEditorScreen.kt:80,83`, `DiaryDetailScreen.kt:86,88` — 동일 `diaryBg` |
| SC-04 | 설정 → 일기 배경색 즉시 반영 | ✅ Met | StateFlow → `MainActivity.kt:33,38-41` copy() → recomposition |
| SC-05 | 설정 → 평일 글씨색 즉시 반영 | ✅ Met | StateFlow → `HomeScreen.kt:274,322` weekdayColor 소비 |
| SC-06 | 재실행 후 선택 색상 유지 | ✅ Met | `ThemePreferences.kt:40-47` SharedPreferences 저장 |
| SC-07 | 초기화 → 크림+진회색 복원 | ✅ Met | `SettingsScreen.kt:150-151` — `resetThemeTemplate() + resetDiaryColors()` |
| SC-08 | v4 테마/알림/CRUD 회귀 없음 | ✅ Met | ThemeColors 기본값 파라미터 → AppThemeTemplate.kt 무수정; 알림 코드 미변경 |

**Success Rate: 8/8 (100%)**

---

## 3. Static Gap Analysis

### 3.1 Structural Match — 100%

| 파일 | 설계 | 구현 | 상태 |
|------|------|------|------|
| `ui/theme/LocalThemeColors.kt` | `diaryBg`, `weekdayColor` 필드 추가 | ✅ 구현됨 | Match |
| `ui/theme/Color.kt` | `DiaryBgPalette`, `WeekdayColorPalette` object | ✅ 구현됨 | Match |
| `notification/ThemePreferences.kt` | `diaryBgColor`, `weekdayColor` 프로퍼티 + reset | ✅ 구현됨 | Match |
| `viewmodel/SettingsViewModel.kt` | StateFlow 2개 + setter 2개 + resetDiaryColors | ✅ 구현됨 | Match |
| `MainActivity.kt` | `diaryBg`, `weekday` collect + `copy()` | ✅ 구현됨 | Match |
| `ui/settings/SettingsScreen.kt` | ColorPaletteRow 2개 + ColorPaletteRow composable | ✅ 구현됨 | Match |
| `ui/home/HomeScreen.kt` | 42셀 패딩 + DayCell weekdayColor | ✅ 구현됨 | Match |
| `ui/diary/DiaryEditorScreen.kt` | containerColor = diaryBg | ✅ 구현됨 | Match |
| `ui/diary/DiaryDetailScreen.kt` | containerColor = diaryBg | ✅ 구현됨 | Match |
| `data/util/ImageCompressor.kt` | maxSizeBytes = 307_200L | ✅ 구현됨 | Match |

**Structural: 10/10 = 100%**

### 3.2 Functional Depth — 100%

#### SettingsScreen Page UI Checklist

| 항목 | 설계 | 구현 | 상태 |
|------|------|------|------|
| "일기 배경색" 레이블 + 10색 팔레트 | FR-05 | `ColorPaletteRow(label="일기 배경색", DiaryBgPalette)` | ✅ Match |
| "평일 글씨색" 레이블 + 10색 팔레트 | FR-06 | `ColorPaletteRow(label="평일 글씨색", WeekdayColorPalette)` | ✅ Match |
| 선택 색상 체크마크 표시 | FR-05/06 | `ThemeCircleCard(isSelected = color == selectedColor)` → Check icon | ✅ Match |
| 초기화 버튼 → 크림+진회색 복원 | FR-08 | `resetThemeTemplate() + resetDiaryColors()` | ✅ Match |
| 기존 ThemeTemplateSelector 유지 | SC-08 | 미변경, 여전히 렌더링 | ✅ Match |

#### HomeScreen Checklist

| 항목 | 설계 | 구현 | 상태 |
|------|------|------|------|
| 42셀 고정 (6줄) | FR-01 | `while (size < 42) add(null)` — `HomeScreen.kt:281` | ✅ Match |
| 빈 셀 = 투명 Box | FR-01 | `if (day == null) Box(Modifier.height(60.dp))` — 기존 코드 유지 | ✅ Match |
| 평일 weekdayColor 반영 | FR-06 | `DayCell.dateColor: else -> weekdayColor` — `HomeScreen.kt:355` | ✅ Match |
| 토일 색상 유지 | SC-08 | `DateSaturday`, `DateSunday` 유지 | ✅ Match |
| 오늘 배경 todayBg 유지 | SC-08 | `themeColors.todayBg` 유지 | ✅ Match |

#### DiaryEditorScreen / DiaryDetailScreen

| 항목 | 설계 | 구현 | 상태 |
|------|------|------|------|
| Editor containerColor = diaryBg | FR-03, FR-04 | `LocalThemeColors.current.diaryBg` → Scaffold containerColor | ✅ Match |
| Detail containerColor = diaryBg | FR-03 | `LocalThemeColors.current.diaryBg` → Scaffold containerColor | ✅ Match |

**Functional: 12/12 = 100%**

### 3.3 Contract Match — 100%

해당 없음 — 로컬 UI 전용 (서버 API 없음).

**Contract: 100% (N/A → 100% 처리)**

---

## 4. Match Rate 계산

```
Static only (Android 앱, 서버 없음):
Overall = (Structural × 0.2) + (Functional × 0.4) + (Contract × 0.4)
        = (100 × 0.2) + (100 × 0.4) + (100 × 0.4)
        =    20        +    40        +    40
        = 100%
```

**Overall Match Rate: 100% ✅ (임계값 90% 초과)**

---

## 5. 발견된 Gap 목록

### Critical / Important

없음.

### Info (허용)

| ID | 위치 | 내용 |
|----|------|------|
| I-01 | `SettingsScreen.kt:ColorPaletteRow` | `isSelected = color == selectedColor` — Color 동등 비교. `color.toArgb()` 저장 후 `Color(int)` 복원 시 동등성 보장됨 (Round-trip 무손실). |
| I-02 | `DiaryDetailScreen.kt:290` | 내부 카드 `containerColor = surfaceVariant` — 메인 Scaffold diaryBg와 별개. 설계 의도와 일치 (일기 내용 카드 강조용). |
| I-03 | v3 dead code | `calendarBgColor`, `appBgColor`, `todayBgColor` StateFlow가 SettingsViewModel에 남아있음 — 의도적 허용 (v4 Option A 방침 유지). |

---

## 6. Decision Record Verification

| 결정 | 준수 여부 | 비고 |
|------|---------|------|
| KD-01: ThemeColors 기본값 파라미터 | ✅ | `LocalThemeColors.kt:12-13` — 두 필드 모두 기본값 포함 |
| KD-02: MainActivity override 패턴 | ✅ | `MainActivity.kt:38-41` — `template.themeColors.copy(diaryBg=..., weekdayColor=...)` |
| KD-03: 42셀 패딩 | ✅ | `HomeScreen.kt:281` — `while (size < 42) add(null)` |
| KD-04: 300KB 한도 | ✅ | `ImageCompressor.kt:18` — `307_200L` |
| Option C: Color.kt object 네임스페이스 | ✅ | `DiaryBgPalette`, `WeekdayColorPalette` object로 그룹화 |

---

## 7. 결론

**Match Rate: 100%** — 임계값(90%) 초과, 반복 수정 불필요.

모든 8개 Success Criteria 충족. 설계 결정 4개 모두 준수. Gap 없음.

`/pdca report joyary-upgrade-v5` 진행 권장.
