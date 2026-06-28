# Design: tools-tab-ladder-game

## Architecture: Option C — Pragmatic Balance

### State Machine (LadderPhase)
```
INPUT ──onStart──→ NAMING ──all names filled──→ REVEALING
  ↑                   ↑                              ↑
  └─────── back ───────┴──────────────────────────────┘
```

### Data Model
```kotlin
// Ladder rungs: List<Set<Int>>
//   rungs[row] = set of column indices where a rung connects col → col+1
// Path: List<Int>, size = rungs.size + 1
//   path[i] = column the player is at during segment i
// Items: List<String> — bottom labels (shuffled from input)
// Names: List<String?> — top player names (null = not entered)
```

### Ladder Generation
- N columns (= input count), 12 rows
- Per row: iterate cols 0..N-2; add rung if previous col has no rung (prevents adjacent rungs) AND coin flip

### Path Tracing
- Start at column `startCol`
- Per row: if in rung → col++; elif col-1 in rung → col--
- Returns List<Int> of size rungs.size+1

### Canvas Layout
```
y=0                         ← top of canvas
y=rowH                      ← rung[0] level
y=2*rowH                    ← rung[1] level
...
y=rows*rowH = size.height   ← bottom
rowH = size.height / (rungs.size + 1)
```

### UI Layout (NAMING/REVEALING)
```
[1] [2] [3] [4]        ← top name buttons (weight 1f each)
──────────────────
│  │  │  │  │         ← ladder top stubs
│  🔒비밀🔒  │         ← hidden overlay (60% height, removed on reveal)
│  │  │  │  │         ← ladder bottom stubs
──────────────────
[A] [B] [C] [D]        ← bottom items (shuffled, weight 1f each)
```

### File Specs

#### ToolsScreen.kt (new)
- `ToolsContent(onNavigateToLadder, modifier)`: LazyVerticalGrid, 2 columns
- `ToolCard(icon, title, onClick)`: Card, aspectRatio(1f), icon 48dp + title

#### LadderGameScreen.kt (new)
- `LadderGameScreen(onBack)`: Scaffold + state management + dialog
- `LadderInputContent(...)`: scrollable Column, OutlinedTextField × N, add/remove buttons
- `LadderGameContent(...)`: Column with name row, canvas, items row
- `LadderCanvas(...)`: Box + Canvas, overlay when isHidden
- `generateLadder(n, rows=12)`: List<Set<Int>>
- `tracePath(startCol, rungs)`: List<Int>

#### Screen.kt (edit)
- Add: `object LadderGame : Screen("ladder_game")`

#### NavGraph.kt (edit)
- HomeScreen call: add `onNavigateToLadder = { navController.navigate(Screen.LadderGame.route) }`
- Add composable: `Screen.LadderGame.route` → `LadderGameScreen(onBack)`

#### HomeScreen.kt (edit)
- Add param: `onNavigateToLadder: () -> Unit = {}`
- NavigationBar: add tab 2 with `Icons.Default.Build`
- FAB: `0 →` diary, `1 →` memo, else → nothing
- Content: `else → ToolsContent(onNavigateToLadder, modifier)`

### Validation Criteria
- [ ] 도구모음 탭 클릭 시 아이콘 그리드 보임
- [ ] 사다리 게임 카드 클릭 시 전체화면 이동
- [ ] INPUT: 2개 미만이면 버튼 비활성화
- [ ] INPUT → NAMING: 사다리 생성, 중간 숨김
- [ ] NAMING: 이름 버튼 클릭 → 다이얼로그 → 이름 저장
- [ ] 모든 이름 입력 → REVEALING 자동 전환
- [ ] REVEALING: 이름 클릭 → 사다리 공개 + 경로 강조 + 결과 강조
- [ ] 다른 이름 클릭 → 해당 경로로 교체
- [ ] 뒤로 버튼: NAMING/REVEALING → INPUT 복귀, INPUT → 앱 뒤로
