package com.example.diaryapp.ui.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.diaryapp.viewmodel.AuthViewModel
import com.example.diaryapp.viewmodel.DiaryUiState
import com.example.diaryapp.viewmodel.DiaryViewModel

// Design Ref: §4.7 — 이미지 좌우 여백, 내용 Card 박스, 날씨 태그 표시 (SC-08)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(
    date: String,
    onEdit: (String, String) -> Unit,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    diaryViewModel: DiaryViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val userId = authViewModel.currentUserId
    val entry by diaryViewModel.selectedEntry.collectAsStateWithLifecycle()
    val uiState by diaryViewModel.uiState.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(date, userId) {
        diaryViewModel.loadDiaryByDate(userId, date)
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(date) },
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
        if (entry == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val e = entry!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Plan SC: SC-08 — 이미지 좌우 여백 16dp
            if (e.imageUrls.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    items(e.imageUrls) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "일기 이미지",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(220.dp)
                                .width(if (e.imageUrls.size == 1) 340.dp else 260.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }

            // Plan SC: SC-08 — 감정/날씨 칩 행
            if (e.emotion != null || e.weather != null) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    e.emotion?.let { emotion ->
                        AssistChip(
                            onClick = {},
                            label = { Text("${emotion.emoji} ${emotion.label}") }
                        )
                    }
                    e.weather?.let { weather ->
                        AssistChip(
                            onClick = {},
                            label = { Text("${weather.emoji} ${weather.label}") }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Plan SC: SC-08 — 내용 Card 박스
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
                    text = e.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("일기 삭제") },
            text = { Text("이 일기를 삭제하시겠습니까? 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        entry?.let { diaryViewModel.deleteDiary(it) }
                    }
                ) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소") }
            }
        )
    }
}
