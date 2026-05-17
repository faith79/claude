package com.example.diaryapp.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import com.example.diaryapp.viewmodel.AuthViewModel
import com.example.diaryapp.viewmodel.DiaryViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

// Design Ref: §4.1 — HorizontalPager 스와이프 달력 (SC-06) + 화살표 버튼 병존
// Plan SC: SC-04 — FAB Upsert 체크 포함
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

    // Page ↔ YearMonth mapping anchored at 2000-01
    val BASE_YEAR = 2000
    val TOTAL_PAGES = (2100 - BASE_YEAR) * 12
    val now = YearMonth.now()
    val initialPage = (now.year - BASE_YEAR) * 12 + (now.monthValue - 1)

    val pagerState = rememberPagerState(initialPage = initialPage) { TOTAL_PAGES }

    fun pageToYearMonth(page: Int): YearMonth =
        YearMonth.of(BASE_YEAR + page / 12, page % 12 + 1)

    // Load month data whenever pager settles on a new page
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
                // Plan SC: SC-04 — 오늘 일기 있으면 수정, 없으면 신규 작성
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
                // Plan SC: SC-06 — 스와이프로 월 이동
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val pageMonth = pageToYearMonth(page)
                    Column {
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
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Box(Modifier.aspectRatio(1f))
                } else {
                    val date = yearMonth.atDay(day).format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val entry = diaryMap[date]
                    val isToday = yearMonth.atDay(day) == today
                    DayCell(
                        day = day,
                        entry = entry,
                        isToday = isToday,
                        onClick = { onDateClick(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    entry: DiaryEntry?,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val emotion = entry?.emotion
    val bgColor = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 13.sp,
            color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
        if (emotion != null) {
            Text(emotion.emoji, fontSize = 12.sp)
        } else if (entry != null) {
            Box(
                Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
