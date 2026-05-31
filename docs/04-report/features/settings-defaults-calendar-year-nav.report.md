# Report: settings-defaults-calendar-year-nav

**완료일**: 2026-06-01  
**Match Rate**: 100% ✅  
**변경 파일**: 3개 | **변경 라인**: ~15줄

---

## 수정 내용

### 1. 테마 기본값 변경 (ThemePreferences.kt)

| 항목 | 기존 | 변경 |
|------|------|------|
| 색상 테마 | 0 (하늘) | 20 (미드나잇) |
| 평일 글씨색 | `0xFF424242` (어두운 회색) | `0xFFFFFFFF` (흰색) |
| 글쓰기/수정 배경색 | `0xFFFFF8F0` (크림) | `0xFF000000` (검정) |

- `resetToDefault()`: `selectedTemplateIndex = 20`
- `resetDiaryColors()`: `diaryBgColor = 검정`, `weekdayColor = 흰색`

### 2. SettingsViewModel reset 수정 (SettingsViewModel.kt)

```kotlin
fun resetThemeTemplate() {
    themePreferences.resetToDefault()
    _selectedTemplateIndex.value = 20   // 0 → 20 (hardcoded 값 동기화)
}
```

### 3. 달력 년 단위 네비게이션 (HomeScreen.kt)

```kotlin
// 화살표 클릭: 월 단위 → 년 단위
onPrev → pagerState.animateScrollToPage(currentPage - 12)  // 1년 이전
onNext → pagerState.animateScrollToPage(currentPage + 12)  // 1년 이후
```

### 4. << >> 아이콘 UI

```kotlin
// Before: Material Icon ArrowBack / ArrowForward
// After: Text 버튼 << / >>
IconButton(onClick = onPrev) {
    Text("<<", style = titleMedium, fontWeight = Bold)
}
IconButton(onClick = onNext) {
    Text(">>", style = titleMedium, fontWeight = Bold)
}
```

---

## 적용 범위

- **신규 설치**: 앱 최초 실행 시 미드나잇 테마 + 흰 평일 글씨 + 검정 에디터 배경이 기본 적용
- **기존 사용자**: 이미 설정이 저장되어 있어 영향 없음 (SharedPreferences 저장값 우선)
- **초기화 시**: 설정 > 초기화 버튼 클릭 시 새 기본값으로 복원
- **달력 스와이프**: HorizontalPager 스와이프로 월 단위 이동은 여전히 가능
