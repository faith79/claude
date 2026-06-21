# Design: kbo-standings

## Architecture: Option C — Pragmatic Balance

Jsoup 단일 라이브러리로 HTTP + HTML 파싱을 처리. ViewModel에 로직 집중 (별도 Repository 생략).

---

## §F1: 데이터 흐름
```
KboStandingsScreen
  └── hiltViewModel<KboStandingsViewModel>()
        └── viewModelScope.launch { withContext(IO) {
              Jsoup.connect(KBO_URL).get()  // HTML fetch
              → find table containing "LG" & "KT" & "삼성"
              → parse tbody tr → List<KboTeamStanding>
            }}
            → _state: MutableStateFlow<KboStandingsUiState>
```

## §F2: 데이터 모델
```kotlin
data class KboTeamStanding(
    val rank: String, val team: String, val games: String,
    val wins: String, val draws: String, val losses: String,
    val pct: String, val gb: String, val streak: String
)

sealed class KboStandingsUiState {
    object Loading : KboStandingsUiState()
    data class Success(val year: Int, val standings: List<KboTeamStanding>, val lastUpdated: String)
    data class Error(val message: String)
}
```

## §F3: 파싱 전략 (robust)
```kotlin
val doc = Jsoup.connect("https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx")
    .userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36")
    .timeout(10_000).get()

// KBO 팀명이 포함된 테이블 탐색
val standingsTable = doc.select("table").firstOrNull { t ->
    val txt = t.text()
    txt.contains("LG") && txt.contains("KT") && txt.contains("삼성")
}

// tbody tr → cells[0..8]
rows.mapNotNull { row ->
    val cells = row.select("td")
    if (cells.size < 7) null
    else KboTeamStanding(rank=cells[0], team=cells[1], ..., streak=cells.getOrNull(8))
}
```

## §F4: UI 표 레이아웃
```
[상단] TopAppBar: "2026 KBO 프로야구 순위" + Refresh 버튼
[표 헤더] primaryContainer 배경
  순위 | 팀  | 경기 | 승 | 무 | 패 | 승률  | 게임차 | 연속
  34dp  66dp  36dp  30dp 30dp 30dp 46dp   46dp   44dp  = 362dp
[표 행] 홀짝 행 교차 배경
  - 1위=금, 2위=은, 3위=동 색상 순위숫자
  - 승 컬럼: Blue 200, 패 컬럼: Red 200
  - 연속 "N승" → Blue, "N패" → Red
[하단] "마지막 업데이트: MM/dd HH:mm | 출처: KBO 공식"
```

가로 스크롤: 단일 hScrollState를 헤더·모든 행이 공유

## §F5: 네비게이션 추가 사항
- Screen.kt: `object KboStandings : Screen("kbo_standings")`
- ToolsScreen: `onNavigateToKbo` 파라미터 + `Icons.Default.Leaderboard` 아이콘
- HomeScreen: `onNavigateToKbo: () -> Unit = {}` 추가 → ToolsContent 전달
- NavGraph: `onNavigateToKbo = { navController.navigate(Screen.KboStandings.route) }` + composable 등록
