package com.example.diaryapp.ui.diary

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.diaryapp.data.model.DiaryEntry
import com.example.diaryapp.ui.theme.LocalThemeColors
import com.example.diaryapp.viewmodel.AuthViewModel
import com.example.diaryapp.viewmodel.DiaryUiState
import com.example.diaryapp.viewmodel.DiaryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Design Ref: §5.3 — HorizontalPager 날짜 스와이프 ±365일 (FR-09,FR-10)
// Design Ref: joyary-upgrade-v6 §5.2 — 날짜+요일 (FR-03), §5.5 — 이미지 확대 레이어 (FR-08, FR-09)
// Design Ref: joyary-upgrade-v7 §3.3 — 이미지 오버레이 HorizontalPager 스와이프 (FR-01, FR-02)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaryDetailScreen(
    date: String,
    onEdit: (String, String) -> Unit,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onAddDiary: (String) -> Unit = {},
    diaryViewModel: DiaryViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val userId = authViewModel.currentUserId
    val uiState by diaryViewModel.uiState.collectAsStateWithLifecycle()
    val isDetailLoading by diaryViewModel.isDetailLoading.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is DiaryUiState.Success -> {
                diaryViewModel.resetState()
                onDeleted()
            }
            is DiaryUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as DiaryUiState.Error).message)
                diaryViewModel.resetState()
            }
            else -> Unit
        }
    }

    // Plan SC: FR-09 — HorizontalPager ±365일 (총 731페이지), 초기 페이지 = 365(중앙)
    val baseDate = remember(date) { LocalDate.parse(date) }
    val TOTAL_PAGES = 731
    val INITIAL_PAGE = 365
    val pagerState = rememberPagerState(initialPage = INITIAL_PAGE) { TOTAL_PAGES }

    fun pageToDate(page: Int): LocalDate = baseDate.plusDays((page - INITIAL_PAGE).toLong())

    LaunchedEffect(pagerState.settledPage, userId) {
        val targetDate = pageToDate(pagerState.settledPage).format(DateTimeFormatter.ISO_LOCAL_DATE)
        diaryViewModel.loadDiaryByDate(userId, targetDate)
    }

    val entry by diaryViewModel.selectedEntry.collectAsStateWithLifecycle()

    // Design Ref: joyary-upgrade-v5 §5.1 — containerColor = diaryBg (FR-03)
    val diaryBg = LocalThemeColors.current.diaryBg
    Scaffold(
        containerColor = diaryBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            val targetDate = pageToDate(page)
            val targetDateStr = targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

            val isCurrentPage = page == pagerState.settledPage
            val pageEntry = if (isCurrentPage) entry else null

            DiaryPageContent(
                date = targetDateStr,
                entry = pageEntry,
                isCurrentPage = isCurrentPage,
                isDetailLoading = isCurrentPage && isDetailLoading,
                onEdit = onEdit,
                onBack = onBack,
                onDelete = { e -> diaryViewModel.deleteDiary(e) },
                onAddDiary = onAddDiary
            )
        }
    }
}

// Design Ref: joyary-upgrade-v6 §5.2 — 날짜+요일 포맷 (FR-03)
private fun formatDateWithDay(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val dayNames = listOf("월", "화", "수", "목", "금", "토", "일")
        val dayName = dayNames[date.dayOfWeek.value - 1]
        "$dateStr ($dayName)"
    } catch (e: Exception) {
        dateStr
    }
}

// Design Ref: §5.3 — 일기 있는 날: 기존 상세 UI / 없는 날: EmptyDiaryPage (FR-09, FR-10)
// G-01 fix: selectedImageUrl 상태를 DiaryPageContent로 호이스팅 — overlay가 TopAppBar까지 커버
// Design Ref: joyary-upgrade-v7 §3.1 — selectedImageIndex(Int?)로 변경, HorizontalPager 오버레이
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DiaryPageContent(
    date: String,
    entry: DiaryEntry?,
    isCurrentPage: Boolean,
    isDetailLoading: Boolean = false,
    onEdit: (String, String) -> Unit,
    onBack: () -> Unit,
    onDelete: (DiaryEntry) -> Unit,
    onAddDiary: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    // Design Ref: joyary-upgrade-v7 §3.1 — Int? 인덱스로 관리 (null=닫힘, N=N번째 이미지 확대)
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    val overlayImages = entry?.imageUrls ?: emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    // Design Ref: joyary-upgrade-v6 §5.2 — 날짜 옆 요일 표시 (FR-03)
                    title = { Text(formatDateWithDay(date)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
                        }
                    },
                    actions = {
                        entry?.let { e ->
                            IconButton(onClick = { onEdit(date, e.id) }) {
                                Icon(Icons.Default.Edit, "수정")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, "삭제")
                            }
                        }
                    }
                )
            }
        ) { padding ->
            // Design Ref: joyary-upgrade-v9 §3.2 — 스켈레톤 분기 (SC-06)
            when {
                !isCurrentPage -> {
                    Box(Modifier.fillMaxSize().padding(padding))
                    return@Scaffold
                }
                isDetailLoading -> {
                    DiaryDetailSkeleton(Modifier.fillMaxSize().padding(padding))
                    return@Scaffold
                }
            }

            if (entry == null) {
                EmptyDiaryPage(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onAddDiary = { onAddDiary(date) }
                )
            } else {
                DiaryEntryContent(
                    entry = entry,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onImageClick = { index -> selectedImageIndex = index }
                )
            }
        }

        // Design Ref: joyary-upgrade-v7 §3.3 — HorizontalPager 오버레이 (FR-01, FR-02)
        // 배경 클릭 닫기 제거: 스와이프 제스처와 충돌 방지 → X버튼으로만 닫기
        if (selectedImageIndex != null && overlayImages.isNotEmpty()) {
            val pagerState = rememberPagerState(
                initialPage = selectedImageIndex!!.coerceIn(0, overlayImages.lastIndex)
            ) { overlayImages.size }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(overlayImages[page])
                            .crossfade(true)
                            .size(Size.ORIGINAL)
                            .build(),
                        contentDescription = "확대 이미지 ${page + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                IconButton(
                    onClick = { selectedImageIndex = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
                }

                // Design Ref: joyary-upgrade-v7 §3.3 — 이미지 2장 이상일 때 인디케이터 (FR-02)
                if (overlayImages.size > 1) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${overlayImages.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog && entry != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("일기 삭제") },
            text = { Text("이 일기를 삭제하시겠습니까? 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(entry)
                    }
                ) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소") }
            }
        )
    }
}

// Plan SC: FR-10 — 일기 없는 날 빈 화면
@Composable
private fun EmptyDiaryPage(
    modifier: Modifier = Modifier,
    onAddDiary: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "아직 일기가 없어요",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onAddDiary) {
                Text("일기 쓰기")
            }
        }
    }
}

// 기존 일기 상세 콘텐츠 — onImageClick(index) 콜백으로 인덱스 전달 (joyary-upgrade-v7)
@Composable
private fun DiaryEntryContent(
    entry: DiaryEntry,
    modifier: Modifier = Modifier,
    onImageClick: (Int) -> Unit = {}
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        // Design Ref: §5.5 — Coil EXIF 회전 보정 (FR-11): Size.ORIGINAL로 원본 해상도 EXIF 처리
        if (entry.imageUrls.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Design Ref: joyary-upgrade-v7 §3.2 — itemsIndexed로 인덱스 전달 (FR-01)
                itemsIndexed(entry.imageUrls) { index, url ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .size(Size.ORIGINAL)
                            .build(),
                        contentDescription = "일기 이미지",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(220.dp)
                            .width(if (entry.imageUrls.size == 1) 340.dp else 260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick(index) }
                    )
                }
            }
        }

        if (entry.emotion != null || entry.weather != null) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                entry.emotion?.let { emotion ->
                    AssistChip(
                        onClick = {},
                        label = { Text("${emotion.emoji} ${emotion.label}") }
                    )
                }
                entry.weather?.let { weather ->
                    AssistChip(
                        onClick = {},
                        label = { Text("${weather.emoji} ${weather.label}") }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(20.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// Design Ref: joyary-upgrade-v9 §3.1 — 로딩 중 콘텐츠 구조 표시 (SC-05)
@Composable
private fun DiaryDetailSkeleton(modifier: Modifier = Modifier) {
    val alpha by rememberInfiniteTransition(label = "skeleton")
        .animateFloat(
            initialValue = 0.35f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
    val shimmer = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        // 이미지 플레이스홀더
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmer)
        )
        // 감정/날씨 칩 플레이스홀더
        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(16.dp)).background(shimmer))
            Box(Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(16.dp)).background(shimmer))
        }
        Spacer(Modifier.height(12.dp))
        // 본문 카드 플레이스홀더
        Box(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(shimmer)
        )
    }
}
