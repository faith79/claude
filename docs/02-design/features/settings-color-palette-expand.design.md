# Design: settings-color-palette-expand

## Architecture: Option C — Pragmatic Balance

### DiaryBgPalette 변경 (15 → 16종)
검정 항목만 추가. 흰색·다크그린은 이미 존재.

```kotlin
// Dark 섹션 끝에 추가
Color(0xFF000000),  // 검정
```
labels 끝: `"검정"`

### WeekdayColorPalette 변경 (10 → 13종)
세 항목 모두 추가.

```kotlin
// 끝에 추가
Color(0xFFFFFFFF),  // 하얀색
Color(0xFF000000),  // 검정
Color(0xFF1A2E1A),  // 다크그린 (딥그린 0xFF1B5E20과 색조 구별)
```
labels 끝: `"하얀색", "검정", "다크그린"`

### 변경 파일
| File | 변경 유형 |
|------|-----------|
| `ui/theme/Color.kt` | 수정 — 4항목 추가 (DiaryBgPalette 1 + WeekdayColorPalette 3) |
