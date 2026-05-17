package com.example.diaryapp.ui.diary

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
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
import com.example.diaryapp.data.model.EmotionTag
import com.example.diaryapp.viewmodel.AuthViewModel
import com.example.diaryapp.viewmodel.DiaryUiState
import com.example.diaryapp.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditorScreen(
    date: String,
    existingId: String = "",
    onSaved: () -> Unit,
    onBack: () -> Unit,
    diaryViewModel: DiaryViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val userId = authViewModel.currentUserId
    val uiState by diaryViewModel.uiState.collectAsStateWithLifecycle()
    val selectedEntry by diaryViewModel.selectedEntry.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedEmotion by remember { mutableStateOf<EmotionTag?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(existingId) {
        if (existingId.isNotEmpty()) {
            diaryViewModel.loadDiaryByDate(userId, date)
        }
    }

    LaunchedEffect(selectedEntry) {
        if (!initialized && existingId.isNotEmpty() && selectedEntry != null) {
            val e = selectedEntry!!
            title = e.title
            content = e.content
            selectedEmotion = e.emotion
            existingImageUrl = e.imageUrl
            initialized = true
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is DiaryUiState.Success) {
            diaryViewModel.resetState()
            onSaved()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingId.isEmpty()) "새 일기" else "일기 수정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            diaryViewModel.saveDiary(
                                userId = userId,
                                title = title,
                                content = content,
                                date = date,
                                emotion = selectedEmotion,
                                existingId = existingId,
                                imageUri = imageUri,
                                existingImageUrl = existingImageUrl
                            )
                        },
                        enabled = content.isNotBlank() && uiState !is DiaryUiState.Loading
                    ) {
                        Icon(Icons.Default.Check, "저장")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(date, style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            EmotionSelector(
                selected = selectedEmotion,
                onSelect = { selectedEmotion = if (selectedEmotion == it) null else it }
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목 (선택)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("오늘 하루는 어땠나요?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                maxLines = Int.MAX_VALUE
            )

            Spacer(Modifier.height(16.dp))

            val displayUri = imageUri ?: existingImageUrl?.let { Uri.parse(it) }
            if (displayUri != null) {
                AsyncImage(
                    model = displayUri,
                    contentDescription = "선택된 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { imagePickerLauncher.launch("image/*") }
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { imageUri = null; existingImageUrl = null },
                    modifier = Modifier.align(Alignment.End)
                ) { Text("이미지 제거") }
            } else {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("사진 추가")
                }
            }

            if (uiState is DiaryUiState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(
                    (uiState as DiaryUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (uiState is DiaryUiState.Loading) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun EmotionSelector(
    selected: EmotionTag?,
    onSelect: (EmotionTag) -> Unit
) {
    Column {
        Text("오늘의 감정", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            EmotionTag.entries.forEach { emotion ->
                val isSelected = selected == emotion
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(emotion) }
                        .padding(vertical = 8.dp)
                ) {
                    Text(emotion.emoji, style = MaterialTheme.typography.titleMedium)
                    Text(
                        emotion.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
