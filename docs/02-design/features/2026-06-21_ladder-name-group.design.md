# Design: ladder-name-group

## Architecture: Option C — Pragmatic Balance

최소 변경으로 기존 구조(names: List<String?>, items: List<String>) 그대로 유지하며 버그 수정 + 기능 추가.

---

## §F1: onQuickStart 이름 보존 수정

### 변경 전
```kotlin
names = List(items.size) { "${it + 1}" }
```
→ 기존 names 배열 완전 교체 (입력값 소실)

### 변경 후
```kotlin
names = names.mapIndexed { idx, name ->
    if (name.isNullOrBlank()) "${idx + 1}" else name
}
```
→ null/blank 슬롯만 숫자로 채우고 기존 입력 보존

### 케이스 검증
| 상황 | 결과 |
|------|------|
| names[0]="Alice", names[1]=null | → ["Alice", "2"] ✓ |
| names[0]=null, names[1]="Bob" | → ["1", "Bob"] ✓ |
| 모두 null | → ["1", "2", "3", ...] ✓ |
| 모두 입력됨 | → 그대로 유지 ✓ |

---

## §F2: 결과 그룹 색상

### 팔레트 상수
```kotlin
private val LADDER_GROUP_PALETTE = listOf(
    Color(0xFFEF9A9A), // Red 200
    Color(0xFF90CAF9), // Blue 200
    Color(0xFFA5D6A7), // Green 200
    Color(0xFFFFF176), // Yellow 200
    Color(0xFFCE93D8), // Purple 200
    Color(0xFF80DEEA), // Cyan 200
    Color(0xFFFFAB91), // Deep Orange 200
    Color(0xFFB0BEC5), // Blue Grey 200
)
```

### itemGroupColorMap 계산 (LadderGameContent 내)
```
1. items를 텍스트별로 그룹화 → 빈도 내림차순 정렬
2. 최빈도 그룹 → null (= surfaceVariant, 시각적으로 "기본")
3. 나머지 그룹 → LADDER_GROUP_PALETTE[(idx-1) % 8]
```

예시: ["꽝", "꽝", "꽝", "당첨", "꽝"]
- "꽝"(4회) → null (surfaceVariant)
- "당첨"(1회) → Red 200

예시: ["A"×2, "B"×2, "C"×1]
- "A" → null (surfaceVariant)  
- "B" → Red 200
- "C" → Blue 200

### Surface 색상 우선순위
```
isHidden=true        → surfaceVariant + text="?"
pathColor != null    → pathColor (경로 공개됨, 기존 동작 유지)
groupColor != null   → groupColor (그룹 색상)
else                 → surfaceVariant
```

### 텍스트 색상
- isHidden: onSurfaceVariant
- pathColor 있음: Color.White (기존)
- groupColor 있음: onSurface (파스텔 배경 → 어두운 텍스트)
- 기본: onSurfaceVariant

### fontWeight
- pathColor 있음 (경로 공개): Bold (기존)
- 나머지: Normal

---

## 변경 파일

| 파일 | 변경 내용 |
|------|-----------|
| `LadderGameScreen.kt` | onQuickStart 수정 + LADDER_GROUP_PALETTE 추가 + items Row 색상 로직 |
