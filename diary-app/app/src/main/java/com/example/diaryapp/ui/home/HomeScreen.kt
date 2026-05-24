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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
// G-02 fix: joyary-upgrade-v6 — 검색 버튼 제거에 따른 Dead Code 일괄 삭제
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
    val scope = rememberCoroutineScope()

    val BASE_YEAR = 2000
    val TOTAL_PAGES = (2100 - BASE_YEAR) * 12
    val now = YearMonth.now()
    val initialPage = (now.year - BASE_YEAR) * 12 + (now.monthValue - 1)

    val pagerState = rememberPagerState(initialPage = initialPage) { TOTAL_PAGES }
    // Design Ref: joey-ui-material-you §FR-01 — LargeTopAppBar 스크롤 시 접힘
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    fun pageToYearMonth(page: Int): YearMonth =
        YearMonth.of(BASE_YEAR + page / 12, page % 12 + 1)

    LaunchedEffect(pagerState.settledPage, userId) {
        if (userId.isNotEmpty()) {
            val current = pageToYearMonth(pagerState.settledPage)
            diaryViewModel.loadMonth(userId, current)
            // Design Ref: joyary-ux-improvements §FR-03 — 인접 달 미리 불러오기
            diaryViewModel.prefetchMonth(userId, current.minusMonths(1))
            diaryViewModel.prefetchMonth(userId, current.plusMonths(1))
        }
    }

    val diaryMap = remember(diaries) {
        diaries.associateBy { it.date }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // Design Ref: joey-ui-material-you §FR-01 — LargeTopAppBar (접히는 큰 헤더)
            LargeTopAppBar(
                title = { Text("조이어리") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, "설정")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            // Design Ref: joey-ui-material-you §FR-02 — ExtendedFAB (텍스트 레이블 포함)
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val existing = diaryViewModel.getEntryByDate(userId, todayDate)
                        if (existing != null) {
                            onEditDiary(todayDate, existing.id)
                        } else {
                            onAddDiary(todayDate)
                        }
                    }
                },
                icon = { Icon(Icons.Default.Add, "일기 추가") },
                text = { Text("오늘 일기 쓰기") }
            )
        }
    ) { padding ->
        // Design Ref: §5.1 — 달력 고정(상단) + 아래 영역 weight(1f) (FR-02)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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

            // Design Ref: calendar-diary-bg-fix §FR-01 — weight 필러 제거, 달력 상단 정렬
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
    // Design Ref: joyary-upgrade-v5 §2.1 — weekdayColor LocalThemeColors에서 소비 (FR-06)
    val themeWeekdayColor = LocalThemeColors.current.weekdayColor

    val dayLabels = listOf("일", "월", "화", "수", "목", "금", "토")
    // Design Ref: joyary-upgrade-v5 §7.1 — 항상 42개(6×7) 패딩으로 달력 높이 고정 (FR-01, KD-03)
    val cells = buildList {
        repeat(startDayOfWeek) { add(null) }
        for (d in 1..daysInMonth) add(d)
        while (size < 42) add(null)
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
                        weekdayColor = themeWeekdayColor,
                        onClick = { onDateClick(date) }
                    )
                }
            }
        }
    }
}

// Design Ref: §5.2 — 이모지(위,24sp) + 날짜(아래,13sp) + 빈 동그라미 + 토/일 색상 (FR-04,05,06,08)
// Design Ref: joyary-upgrade-v5 §5.4 — weekdayColor 파라미터 추가 (FR-06)
@Composable
private fun DayCell(
    day: Int,
    entry: DiaryEntry?,
    isToday: Boolean,
    dayOfWeek: DayOfWeek,
    weekdayColor: Color,
    onClick: () -> Unit
) {
    val emotion = entry?.emotion
    val themeColors = LocalThemeColors.current
    // Design Ref: joey-ui-material-you §FR-03 — 오늘 날짜: solid primary 원형 강조
    val bgColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    // Plan SC: FR-08 — 토요일 파랑, 일요일 빨강; Plan SC: SC-05 — 평일 weekdayColor 적용
    val dateColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimary
        dayOfWeek == DayOfWeek.SATURDAY -> DateSaturday
        dayOfWeek == DayOfWeek.SUNDAY -> DateSunday
        else -> weekdayColor
    }

    // Design Ref: calendar-diary-bg-fix §FR-01 — 셀 내용 상단 정렬
    Column(
        modifier = Modifier
            .height(60.dp)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(top = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (emotion != null) {
            Text(emotion.emoji, fontSize = 24.sp)
        } else {
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
        Text(
            text = day.toString(),
            fontSize = 13.sp,
            color = dateColor
        )
    }
}
