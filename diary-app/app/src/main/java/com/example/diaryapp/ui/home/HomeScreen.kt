package com.example.diaryapp.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.diaryapp.data.model.DiaryEntry
import com.example.diaryapp.ui.theme.DateSaturday
import com.example.diaryapp.ui.theme.DateSunday
import com.example.diaryapp.ui.theme.LocalThemeColors
import com.example.diaryapp.viewmodel.AuthViewModel
import com.example.diaryapp.viewmodel.DiaryViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

// Design Ref: §5.1 — 달력 상단 고정 레이아웃 (FR-02), HorizontalPager 월 스와이프 유지
// Plan SC: SC-04 — FAB Upsert, SC-06 — 월 스와이프
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onDateSelected: (String) -> Unit,
    onAddDiary: (String) -> Unit,
    onEditDiary: (String, String) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
    diaryViewModel: DiaryViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val userId = authViewModel.currentUserId
    val diaries by diaryViewModel.diaries.collectAsStateWithLifecycle()
    val searchResults by diaryViewModel.searchResults.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val BASE_YEAR = 2000
    val TOTAL_PAGES = (2100 - BASE_YEAR) * 12
    val now = YearMonth.now()
    val initialPage = (now.year - BASE_YEAR) * 12 + (now.monthValue - 1)

    val pagerState = rememberPagerState(initialPage = initialPage) { TOTAL_PAGES }

    fun pageToYearMonth(page: Int): YearMonth =
        YearMonth.of(BASE_YEAR + page / 12, page % 12 + 1)

    LaunchedEffect(pagerState.settledPage, userId) {
        if (userId.isNotEmpty()) {
            diaryViewModel.loadMonth(userId, pageToYearMonth(pagerState.settledPage))
        }
    }

    val diaryMap = remember(diaries) {
        diaries.associateBy { it.date }
    }

    Scaffold(
        topBar = {
            if (showSearch) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        diaryViewModel.searchDiaries(userId, it)
                    },
                    onClose = {
                        showSearch = false
                        searchQuery = ""
                        diaryViewModel.searchDiaries(userId, "")
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("조이어리") },
                    actions = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, "검색")
                        }
                        IconButton(onClick = onSettings) {
                            Icon(Icons.Default.Settings, "설정")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val existing = diaryViewModel.getEntryByDate(userId, todayDate)
                    if (existing != null) {
                        onEditDiary(todayDate, existing.id)
                    } else {
                        onAddDiary(todayDate)
                    }
                }
            }) {
                Icon(Icons.Default.Add, "일기 추가")
            }
        }
    ) { padding ->
        // Design Ref: §5.1 — 달력 고정(상단) + 아래 영역 weight(1f) (FR-02)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showSearch && searchQuery.isNotBlank()) {
                SearchResultsList(
                    results = searchResults,
                    onItemClick = { onDateSelected(it.date) }
                )
            } else {
                // 달력 카드 — 상단 고정 (weight 없음)
                // Design Ref: joyary-upgrade-v3 §5.3 — LocalThemeColors.calendarBg 적용 (FR-01,FR-03)
                val themeColors = LocalThemeColors.current
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = themeColors.calendarBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val pageMonth = pageToYearMonth(page)
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            CalendarHeader(
                                currentMonth = pageMonth,
                                onPrev = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                                onNext = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            )
                            CalendarGrid(
                                yearMonth = pageMonth,
                                diaryMap = diaryMap,
                                onDateClick = { date ->
                                    if (diaryMap.containsKey(date)) onDateSelected(date)
                                    else onAddDiary(date)
                                }
                            )
                        }
                    }
                }

                // 달력 아래 여백 영역 (필요 시 추가 콘텐츠 배치 가능)
                Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("내용 검색...") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        TextButton(onClick = onClose) { Text("취소") }
    }
}

@Composable
private fun SearchResultsList(
    results: List<DiaryEntry>,
    onItemClick: (DiaryEntry) -> Unit
) {
    if (results.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("검색 결과가 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    Column {
        results.forEach { entry ->
            ListItem(
                headlineContent = { Text(entry.content.take(30).ifBlank { "(내용 없음)" }) },
                supportingContent = {
                    Text(entry.date + if (entry.emotion != null) " ${entry.emotion.emoji}" else "")
                },
                modifier = Modifier.clickable { onItemClick(entry) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "이전 달")
        }
        Text(
            "${currentMonth.year}년 ${currentMonth.monthValue}월",
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "다음 달")
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    diaryMap: Map<String, DiaryEntry>,
    onDateClick: (String) -> Unit
) {
    val today = LocalDate.now()
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startDayOfWeek = (firstDay.dayOfWeek.value % 7) // Sun=0

    val dayLabels = listOf("일", "월", "화", "수", "목", "금", "토")
    val cells = buildList {
        repeat(startDayOfWeek) { add(null) }
        for (d in 1..daysInMonth) add(d)
    }

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        // Design Ref: §5.2 — 요일 헤더 (FR-08: 토=파랑, 일=빨강)
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEachIndexed { index, label ->
                val labelColor = when (index) {
                    0 -> DateSunday
                    6 -> DateSaturday
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false
        ) {
            items(cells) { day ->
                if (day == null) {
                    Box(Modifier.height(60.dp))
                } else {
                    val date = yearMonth.atDay(day).format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val entry = diaryMap[date]
                    val isToday = yearMonth.atDay(day) == today
                    val dayOfWeek = yearMonth.atDay(day).dayOfWeek
                    DayCell(
                        day = day,
                        entry = entry,
                        isToday = isToday,
                        dayOfWeek = dayOfWeek,
                        onClick = { onDateClick(date) }
                    )
                }
            }
        }
    }
}

// Design Ref: §5.2 — 이모지(위,24sp) + 날짜(아래,13sp) + 빈 동그라미 + 토/일 색상 (FR-04,05,06,08)
@Composable
private fun DayCell(
    day: Int,
    entry: DiaryEntry?,
    isToday: Boolean,
    dayOfWeek: DayOfWeek,
    onClick: () -> Unit
) {
    val emotion = entry?.emotion
    // Design Ref: joyary-upgrade-v3 §5.3 — LocalThemeColors.todayBg 적용 (FR-05)
    val themeColors = LocalThemeColors.current
    val bgColor = when {
        isToday -> themeColors.todayBg
        else -> Color.Transparent
    }

    // Plan SC: FR-08 — 토요일 파랑, 일요일 빨강
    val dateColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        dayOfWeek == DayOfWeek.SATURDAY -> DateSaturday
        dayOfWeek == DayOfWeek.SUNDAY -> DateSunday
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .height(60.dp)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 위: 감정 이모지 or 빈 동그라미 (FR-04, FR-05, FR-06)
        if (emotion != null) {
            Text(emotion.emoji, fontSize = 24.sp)
        } else {
            // Plan SC: FR-06 — 일기 없는 날 빈 동그라미 (이모지 크기와 동일한 28dp)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
            )
        }
        Spacer(Modifier.height(2.dp))
        // 아래: 날짜 (FR-05)
        Text(
            text = day.toString(),
            fontSize = 13.sp,
            color = dateColor
        )
    }
}
