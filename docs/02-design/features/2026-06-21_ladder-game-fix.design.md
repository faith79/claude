# Design: ladder-game-fix

## Architecture: Option C — Pragmatic Balance

기존 알고리즘 구조(List<Set<Int>> 형식)를 유지하면서 최소 변경으로 복잡도 보장.

---

## §F1: generateLadder 알고리즘 개선

### 핵심 아이디어: straight-streak 강제 전환

```
각 컬럼별 연속 직선 횟수(straightStreak)를 추적.
streak >= 2이면 해당 컬럼에 강제 가로 연결(forced rung) 삽입.
→ 어떤 컬럼도 3행 연속 직선 이동 불가능.
```

### 행 수
```
rows = max(n * 6, 28)   // n=2이면 28행, n=5이면 30행
```

### 인접 가로 연결 방지 (canAdd 함수)
셔플된 순서로 처리 시 양방향 인접 체크 필요:
```kotlin
fun canAdd(p: Int) = p in 0 until n-1 &&
    p !in rowRungs && p-1 !in rowRungs && p+1 !in rowRungs
```

### 처리 순서
1. streak >= 2인 컬럼에 forced rung 삽입 (우선)
2. 나머지 위치에 random rung (확률 0.65f)

### 의사코드
```
for row in 0..rows:
    rowRungs = mutableSetOf()
    
    # 1. Forced turns
    cols_needing_turn = filter(0..n-1) { straightStreak[it] >= 2 }.shuffled()
    for col in cols_needing_turn:
        if canAdd(col):         rowRungs.add(col)    # go right
        elif canAdd(col-1):     rowRungs.add(col-1)  # go left
    
    # 2. Random fill
    (0..n-2).shuffled().forEach { col ->
        if canAdd(col) && Random.nextFloat() < 0.65f: rowRungs.add(col)
    }
    
    result.add(rowRungs)
    
    # Update streaks
    for col in 0..n-1:
        turned = (col in rowRungs) || (col>0 && col-1 in rowRungs)
        straightStreak[col] = if(turned) 0 else straightStreak[col]+1
```

---

## §F2: 결과 하단 Row 비공개 처리

### 조건
- `isHidden = true` (revealedPaths 비어있고 애니메이션 없음) → "?" 표시
- `isHidden = false` → 기존 동작 (색상 + 실제 텍스트)

### 변경 대상
`LadderGameContent` 내 하단 items Row:

```kotlin
// 변경 전
text = item
color = if (itemColor != null) Color.White else ...
fontWeight = if (itemColor != null) FontWeight.Bold else ...
Surface color = itemColor ?: surfaceVariant

// 변경 후
text = if (isHidden) "?" else item
color = when { isHidden -> onSurfaceVariant; itemColor != null -> White; else -> onSurfaceVariant }
fontWeight = if (!isHidden && itemColor != null) FontWeight.Bold else FontWeight.Normal
Surface color = if (isHidden) surfaceVariant else (itemColor ?: surfaceVariant)
```

---

## §F3: 결과 랜덤 배치 — 변경 없음

기존 `items = allItems.shuffled()` (onStart, restart 모두)로 이미 충족.

---

## 변경 파일

| 파일 | 변경 내용 |
|------|-----------|
| `LadderGameScreen.kt` | `generateLadder()` 개선 + items Row 조건 분기 |

## 영향 없는 코드

- `tracePath()` — 알고리즘 호환성 유지 (Set<Int> 구조 동일)
- `drawLadderPath()`, `drawPartialLadderPath()` — 변경 없음
- `LadderCanvas` — isHidden 처리 기존 그대로
- 모든 애니메이션 로직 — 변경 없음
