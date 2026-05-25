package com.example.diaryapp.ui.diary

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.diaryapp.data.model.EmotionTag
import com.example.diaryapp.data.model.WeatherTag
import com.example.diaryapp.ui.components.LoadingOverlay
import com.example.diaryapp.ui.components.MultiImagePicker
import com.example.diaryapp.ui.components.WeatherSelector
import com.example.diaryapp.ui.theme.LocalThemeColors
import com.example.diaryapp.viewmodel.AuthViewModel
import com.example.diaryapp.viewmodel.DiaryUiState
import com.example.diaryapp.viewmodel.DiaryViewModel

// Design Ref: §4.2 — 편집 화면 전면 교체 (SC-02,SC-03,SC-05,SC-10,SC-11)
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
    val isLoading by diaryViewModel.isLoading.collectAsStateWithLifecycle()
    val selectedEntry by diaryViewModel.selectedEntry.collectAsStateWithLifecycle()

    var content by remember { mutableStateOf("") }
    var selectedEmotion by remember { mutableStateOf<EmotionTag?>(null) }
    var selectedWeather by remember { mutableStateOf<WeatherTag?>(null) }
    var existingImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var newImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(existingId) {
        if (existingId.isNotEmpty()) {
            diaryViewModel.loadDiaryByDate(userId, date)
        }
    }

    LaunchedEffect(selectedEntry) {
        if (!initialized && existingId.isNotEmpty() && selectedEntry != null) {
            val e = selectedEntry!!
            content = e.content
            selectedEmotion = e.emotion
            selectedWeather = e.weather
            existingImageUrls = e.imageUrls
            initialized = true
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is DiaryUiState.Success) {
            diaryViewModel.resetState()
            onSaved()
        }
    }

    // Design Ref: joyary-diary-style-fix §SC-01 — containerColor 제거, 시스템 테마 따라감
    Box(modifier = Modifier.fillMaxSize()) {
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
                                    content = content,
                                    date = date,
                                    emotion = selectedEmotion,
                                    weather = selectedWeather,
                                    existingId = existingId,
                                    newImageUris = newImageUris,
                                    existingImageUrls = existingImageUrls
                                )
                            },
                            enabled = content.isNotBlank() && !isLoading
                        ) {
                            Icon(Icons.Default.Check, "저장")
                        }
                    }
                )
            }
        ) { padding ->
            // Design Ref: joyary-ux-improvements §FR-04 — 에디터 배경색 사용자 지정
            val diaryBg = LocalThemeColors.current.diaryBg
            // Design Ref: joyary-upgrade-v6 §5.4 — imePadding으로 키보드 출현 시 스크롤 확보 (FR-07)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(diaryBg)
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    date,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                EmotionSelector(
                    selected = selectedEmotion,
                    onSelect = { selectedEmotion = if (selectedEmotion == it) null else it }
                )

                Spacer(Modifier.height(16.dp))

                // Plan SC: SC-03 — 감정 태그 아래 날씨 탭 배치
                WeatherSelector(
                    selected = selectedWeather,
                    onSelect = { selectedWeather = if (selectedWeather == it) null else it }
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("오늘 하루는 어땠나요?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    maxLines = Int.MAX_VALUE,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                    )
                )
                // Design Ref: skill-test §FR-01 — 글자수 카운터
                Text(
                    "${content.length}자",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.End)
                )

                Spacer(Modifier.height(16.dp))

                // Plan SC: SC-05,SC-10 — 최대 3장 다중 이미지 / X 클릭 시 Storage 즉시 삭제
                MultiImagePicker(
                    imageUrls = existingImageUrls,
                    newImageUris = newImageUris,
                    onAddImages = { uris ->
                        newImageUris = (newImageUris + uris).take(3 - existingImageUrls.size)
                    },
                    onRemoveExisting = { url ->
                        existingImageUrls = existingImageUrls - url
                        diaryViewModel.removeImage(url)
                    },
                    onRemoveNew = { uri ->
                        newImageUris = newImageUris - uri
                    }
                )

                if (uiState is DiaryUiState.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        (uiState as DiaryUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Plan SC: SC-11 — 저장 중 전체화면 Lottie 오버레이
        LoadingOverlay(
            isVisible = isLoading,
            message = "저장 중...",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun EmotionSelector(
    selected: EmotionTag?,
    onSelect: (EmotionTag) -> Unit
) {
    Column {
        Text(
            "오늘의 감정",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
