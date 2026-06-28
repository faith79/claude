# Report: kbo-standings-ui-fix

## Summary
KBO 순위표 컬럼 순서 변경 및 도구 탭 아이콘 교체 완료.

## Changes

### 1. KboStandingsScreen.kt — 승·패·무 순서 변경
- 헤더: 승 | 무 | 패 → 승 | 패 | 무
- 데이터: wins | draws | losses → wins | losses | draws
- 색상 유지: 승=빨강, 패=파랑, 무=기본색

### 2. ToolsScreen.kt — 아이콘 교체
- 사다리 게임: Icons.Default.Games → 🪜 (40sp)
- 프로야구 순위: Icons.Default.Leaderboard → ⚾ (40sp)
- ToolCard API: ImageVector → @Composable () -> Unit 슬롯
- 불필요한 Icons/ImageVector import 제거

## Quality Gate
- Match Rate: 100% ✅
- Build: SUCCESS ✅
- Iterations: 1

## APK
- Path: app/build/outputs/apk/debug/app-debug.apk
- Build: SUCCESS
