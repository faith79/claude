package com.example.diaryapp.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Design Ref: kbo-standings §F2 — 팀별 순위 데이터 모델
data class KboTeamStanding(
    val rank: String,
    val team: String,
    val games: String,
    val wins: String,
    val draws: String,
    val losses: String,
    val pct: String,
    val gb: String,
    val streak: String
)

sealed class KboStandingsUiState {
    object Loading : KboStandingsUiState()
    data class Success(
        val year: Int,
        val standings: List<KboTeamStanding>,
        val lastUpdated: String
    ) : KboStandingsUiState()
    data class Error(val message: String) : KboStandingsUiState()
}

// Design Ref: kbo-standings §F1 — ViewModel: Jsoup 스크래핑 + StateFlow
@HiltViewModel
class KboStandingsViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow<KboStandingsUiState>(KboStandingsUiState.Loading)
    val state: StateFlow<KboStandingsUiState> = _state

    init { fetchStandings() }

    fun fetchStandings() {
        viewModelScope.launch {
            _state.value = KboStandingsUiState.Loading
            try {
                val year = LocalDate.now().year
                val standings = withContext(Dispatchers.IO) {
                    val doc = Jsoup.connect(
                        "https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx"
                    )
                        .userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/120.0")
                        .timeout(15_000)
                        .get()

                    // Design Ref: kbo-streak-fix §F3 — 팀명 포함 테이블 탐색 + 헤더 기반 컬럼 인덱스
                    val standingsTable = doc.select("table").firstOrNull { t ->
                        val txt = t.text()
                        txt.contains("LG") && (txt.contains("KT") || txt.contains("삼성"))
                    }

                    standingsTable?.let { table ->
                        // 헤더에서 "연속" 컬럼 위치 동적 탐지 (KBO 표: 최근10경기=8, 연속=9)
                        val headerTexts = table
                            .select("thead tr th, thead tr td")
                            .map { it.text().trim() }
                        val streakIdx = headerTexts.indexOfFirst {
                            it.contains("연속")
                        }.takeIf { it >= 0 } ?: 9   // 기본값: 인덱스 9
                        val gbIdx = headerTexts.indexOfFirst {
                            it.contains("게임차") || it == "GB"
                        }.takeIf { it >= 0 } ?: 7

                        table.select("tbody tr").mapNotNull { row ->
                            val cells = row.select("td")
                            if (cells.size < 7) return@mapNotNull null
                            KboTeamStanding(
                                rank   = cells[0].text().trim(),
                                team   = cells[1].text().trim(),
                                games  = cells.getOrNull(2)?.text()?.trim() ?: "",
                                wins   = cells.getOrNull(3)?.text()?.trim() ?: "",
                                draws  = cells.getOrNull(4)?.text()?.trim() ?: "",
                                losses = cells.getOrNull(5)?.text()?.trim() ?: "",
                                pct    = cells.getOrNull(6)?.text()?.trim() ?: "",
                                gb     = cells.getOrNull(gbIdx)?.text()?.trim() ?: "",
                                streak = cells.getOrNull(streakIdx)?.text()?.trim() ?: "-"
                            )
                        }
                    } ?: emptyList()
                }

                val now = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
                _state.value = if (standings.isEmpty()) {
                    KboStandingsUiState.Error("순위 데이터를 파싱할 수 없습니다.\n사이트 구조가 변경되었을 수 있습니다.")
                } else {
                    KboStandingsUiState.Success(year, standings, now)
                }
            } catch (e: Exception) {
                _state.value = KboStandingsUiState.Error(
                    "데이터를 불러올 수 없습니다.\n네트워크 연결을 확인해 주세요."
                )
            }
        }
    }
}

// Design Ref: kbo-standings §F4 — 순위표 화면
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KboStandingsScreen(
    onBack: () -> Unit,
    viewModel: KboStandingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val year = (state as? KboStandingsUiState.Success)?.year ?: LocalDate.now().year

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$year KBO 프로야구 순위") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchStandings() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                is KboStandingsUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                is KboStandingsUiState.Error -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                ) {
                    Text(
                        text = s.message,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.fetchStandings() }) {
                        Text("다시 시도")
                    }
                }

                is KboStandingsUiState.Success -> KboTable(
                    standings = s.standings,
                    lastUpdated = s.lastUpdated,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Design Ref: kbo-standings §F4 — 표 UI: 공유 hScrollState로 헤더·행 동기 스크롤
@Composable
private fun KboTable(
    standings: List<KboTeamStanding>,
    lastUpdated: String,
    modifier: Modifier = Modifier
) {
    val hScrollState  = rememberScrollState()
    val headerBg      = MaterialTheme.colorScheme.primaryContainer
    val headerText    = MaterialTheme.colorScheme.onPrimaryContainer
    val rowBg1        = MaterialTheme.colorScheme.surface
    val rowBg2        = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val dividerColor  = MaterialTheme.colorScheme.outlineVariant
    val winColor      = Color(0xFF1E88E5)
    val lossColor     = Color(0xFFE53935)
    val onSurface     = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        // 헤더 행
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .horizontalScroll(hScrollState)
                .padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            KboHeaderCell("순위",   34.dp, headerText)
            KboHeaderCell("팀",     68.dp, headerText)
            KboHeaderCell("경기",   38.dp, headerText)
            KboHeaderCell("승",     32.dp, headerText)
            KboHeaderCell("무",     32.dp, headerText)
            KboHeaderCell("패",     32.dp, headerText)
            KboHeaderCell("승률",   48.dp, headerText)
            KboHeaderCell("게임차", 50.dp, headerText)
            KboHeaderCell("연속",   46.dp, headerText)
        }
        HorizontalDivider(color = dividerColor, thickness = 1.dp)

        // 데이터 행
        standings.forEachIndexed { idx, item ->
            val isTop3 = idx < 3
            val rankColor = when (idx) {
                0    -> Color(0xFFFFB300)          // 금
                1    -> Color(0xFF90A4AE)           // 은
                2    -> Color(0xFFBF8650)           // 동
                else -> onSurface
            }
            val streakColor = when {
                item.streak.contains("승") -> winColor
                item.streak.contains("패") -> lossColor
                else                       -> onSurface
            }
            val fw = if (isTop3) FontWeight.Bold else FontWeight.Normal

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (idx % 2 == 0) rowBg1 else rowBg2)
                    .horizontalScroll(hScrollState)
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KboDataCell(item.rank,   34.dp, rankColor,  fw)
                KboDataCell(item.team,   68.dp, onSurface,  fw)
                KboDataCell(item.games,  38.dp)
                KboDataCell(item.wins,   32.dp, winColor)
                KboDataCell(item.draws,  32.dp)
                KboDataCell(item.losses, 32.dp, lossColor)
                KboDataCell(item.pct,    48.dp)
                KboDataCell(item.gb,     50.dp)
                KboDataCell(item.streak, 46.dp, streakColor)
            }
            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
        }

        Text(
            text = "마지막 업데이트: $lastUpdated  ·  출처: KBO 공식",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun KboHeaderCell(text: String, width: Dp, color: Color) {
    Text(
        text       = text,
        modifier   = Modifier.width(width),
        textAlign  = TextAlign.Center,
        fontSize   = 12.sp,
        fontWeight = FontWeight.Bold,
        color      = color,
        maxLines   = 1
    )
}

@Composable
private fun KboDataCell(
    text:       String,
    width:      Dp,
    color:      Color      = Color.Unspecified,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text       = text,
        modifier   = Modifier.width(width),
        textAlign  = TextAlign.Center,
        fontSize   = 13.sp,
        color      = color,
        fontWeight = fontWeight,
        maxLines   = 1
    )
}
