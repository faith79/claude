# Design: kbo-standings-ui-fix

## Architecture: Option C — Pragmatic Balance

### 변경 1: KboStandingsScreen.kt — 컬럼 순서 변경

헤더 행 (line 225-227):
- 변경 전: 승 | 무 | 패
- 변경 후: 승 | 패 | 무

데이터 행 (line 261-264):
- 변경 전: wins(빨강) | draws | losses(파랑)
- 변경 후: wins(빨강) | losses(파랑) | draws

색상 규칙 유지: 승=winColor(빨강), 패=lossColor(파랑), 무=기본색

### 변경 2: ToolsScreen.kt — 아이콘 교체

현재: `ImageVector` 기반 (`Icons.Default.Games`, `Icons.Default.Leaderboard`)
변경: `@Composable () -> Unit` 람다 기반 아이콘 슬롯

- 사다리 게임: `Text("🪜", fontSize = 40.sp)`
- 프로야구 순위: `Text("⚾", fontSize = 40.sp)`

ToolCard 시그니처 변경:
```kotlin
// 변경 전
fun ToolCard(icon: ImageVector, title: String, onClick: () -> Unit)
// 변경 후
fun ToolCard(iconContent: @Composable () -> Unit, title: String, onClick: () -> Unit)
```

불필요한 import 제거: `Icons`, `material.icons.*`, `ImageVector`
새 import 추가: `androidx.compose.ui.unit.sp`
