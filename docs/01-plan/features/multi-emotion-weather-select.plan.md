# Plan: multi-emotion-weather-select

## Context Anchor
- **WHY**: 현재 감정/날씨가 단일 선택만 가능 — 복합 감정(예: 행복+설렘) 표현 불가
- **WHO**: 일기를 작성하며 여러 감정과 날씨를 태그하고 싶은 사용자
- **RISK**: DiaryEntry 모델 변경 → Firestore 기존 데이터 하위 호환 필요 (legacy `emotion`/`weather` 단일 필드 읽기 유지)
- **SUCCESS**: 편집 화면에서 감정/날씨 다중 클릭 토글, 저장·표시·검색 모두 정상 동작
- **SCOPE**: 8개 파일 (모델, ViewModel, UI 3개, 컴포넌트, Firestore DTO, 로컬 캐시)

## 근본 원인
1. `DiaryEntry.emotion: EmotionTag?`, `DiaryEntry.weather: WeatherTag?` → 단일 nullable
2. `EmotionSelector`, `WeatherSelector` 모두 단일 선택 로직
3. Firestore DTO, 로컬 캐시 직렬화 모두 단일 문자열 저장

## 요구사항

| # | 요구사항 |
|---|---------|
| R-01 | `DiaryEntry`: `emotion/weather` 단일 필드 → `emotions/weathers` List 필드로 교체 |
| R-02 | Firestore DTO: `emotions: List<String>` 신규, legacy `emotion` 단일 필드 fallback 유지 |
| R-03 | 로컬 캐시: `emotions`/`weathers` JSONArray 직렬화, 구버전 단일 필드 fallback 유지 |
| R-04 | `DiaryViewModel.saveDiary`: `emotion/weather` → `emotions/weathers` List 파라미터로 변경 |
| R-05 | `EmotionSelector`: 다중 토글 (클릭 시 Set에 추가/제거) |
| R-06 | `WeatherSelector`: 다중 토글 지원 (`Set<WeatherTag>`) |
| R-07 | `DiaryEditorScreen`: `selectedEmotion/selectedWeather` → `selectedEmotions/selectedWeathers` Set |
| R-08 | `DiaryDetailScreen`: 복수 감정/날씨 칩 모두 표시 (LazyRow) |
| R-09 | `HomeScreen` DayCell: 첫 번째 감정(`emotions.firstOrNull()`) 이모지 표시 |

## 변경 파일

| 파일 | 변경 종류 |
|------|---------|
| `DiaryEntry.kt` | 모델 필드 변경 |
| `FirestoreDataSource.kt` | DTO emotions/weathers List + legacy fallback |
| `DiaryLocalCache.kt` | JSONArray 직렬화 + legacy fallback |
| `DiaryViewModel.kt` | saveDiary 파라미터 변경 |
| `DiaryEditorScreen.kt` | Set 상태, EmotionSelector 다중 선택 |
| `WeatherSelector.kt` | Set 기반 다중 선택 |
| `DiaryDetailScreen.kt` | 복수 칩 LazyRow 표시 |
| `HomeScreen.kt` | emotions.firstOrNull() |

[CP-1 Auto] 요구사항 확인됨 → 계속 진행
[CP-2 Auto] 명확화 질문 생략 → 합리적 기본값 적용
