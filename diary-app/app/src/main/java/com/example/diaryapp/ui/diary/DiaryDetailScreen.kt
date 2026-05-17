package com.example.diaryapp.ui.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.diaryapp.viewmodel.AuthViewModel
import com.example.diaryapp.viewmodel.DiaryUiState
import com.example.diaryapp.viewmodel.DiaryViewModel

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

    LaunchedEffect(date, userId) {
        diaryViewModel.loadDiaryByDate(userId, date)
    }

    LaunchedEffect(uiState) {
        if (uiState is DiaryUiState.Success) {
            diaryViewModel.resetState()
            onDeleted()
        }
    }

    Scaffold(
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
            e.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "일기 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                e.emotion?.let { emotion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(emotion.emoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text(emotion.label) }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (e.title.isNotBlank()) {
                    Text(e.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(12.dp))
                }

                Text(
                    e.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
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
                        entry?.id?.let { diaryViewModel.deleteDiary(it) }
                    }
                ) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소") }
            }
        )
    }
}
