package com.example.diaryapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.diaryapp.ui.theme.AppThemeTemplates
import com.example.diaryapp.ui.theme.WeekdayColorPalette
import com.example.diaryapp.viewmodel.AuthViewModel
import com.example.diaryapp.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val reminderEnabled by settingsViewModel.reminderEnabled.collectAsStateWithLifecycle()
    val reminderHour by settingsViewModel.reminderHour.collectAsStateWithLifecycle()
    val reminderMinute by settingsViewModel.reminderMinute.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }

    // Design Ref: joyary-upgrade-v4 §3.2 — templateIndex collect (FR-02~FR-05)
    val selectedTemplateIndex by settingsViewModel.selectedTemplateIndex.collectAsStateWithLifecycle()
    // Design Ref: joyary-upgrade-v5 §3.1 — 평일 글씨색 collect (FR-06)
    val weekdayColor by settingsViewModel.weekdayColor.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        // Design Ref: joyary-diary-style-fix §SC-02 — verticalScroll + drawWithContent scrollbar
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .drawWithContent {
                    drawContent()
                    val max = scrollState.maxValue
                    if (max > 0) {
                        val ratio  = size.height / (size.height + max)
                        val thumbH = size.height * ratio
                        val thumbY = (size.height - thumbH) * (scrollState.value.toFloat() / max)
                        val alpha  = if (scrollState.isScrollInProgress) 0.7f else 0.3f
                        drawRect(
                            color = Color.Gray,
                            alpha = alpha,
                            topLeft = Offset(size.width - 4.dp.toPx(), thumbY),
                            size = Size(4.dp.toPx(), thumbH)
                        )
                    }
                }
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 알림 섹션
            Text(
                "알림",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("일일 리마인더", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "매일 일기 쓰기를 알려드립니다",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { settingsViewModel.setReminderEnabled(it) }
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("알림 시간", style = MaterialTheme.typography.bodyLarge)
                            TextButton(onClick = { showTimePicker = true }) {
                                Text(
                                    "%02d:%02d".format(reminderHour, reminderMinute),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 테마 섹션
            // Design Ref: joyary-upgrade-v4 §3.2 — ThemeTemplateSelector (FR-02~FR-07)
            Text(
                "테마",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ThemeTemplateSelector(
                        selectedIndex = selectedTemplateIndex,
                        onSelect = settingsViewModel::selectTemplate
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    // Design Ref: joyary-diary-style-fix — 일기 배경색은 색상테마와 자동 연동 (테마 appBg 사용)
                    // Design Ref: joyary-upgrade-v5 §5.4 — 평일 글씨색 팔레트 행 (FR-06)
                    ColorPaletteRow(
                        label = "평일 글씨색",
                        colors = WeekdayColorPalette.colors,
                        labels = WeekdayColorPalette.labels,
                        selectedColor = weekdayColor,
                        onColorSelected = settingsViewModel::setWeekdayColor
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    // Plan SC: SC-07 — 기본값으로 초기화 (FR-08)
                    TextButton(
                        onClick = {
                            settingsViewModel.resetThemeTemplate()
                            settingsViewModel.resetDiaryColors()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("기본값으로 초기화")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 계정 섹션
            Text(
                "계정",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = {
                        authViewModel.signOut()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("로그아웃", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            onConfirm = { h, m ->
                settingsViewModel.setReminderTime(h, m)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

// Design Ref: joyary-upgrade-v5 §5.4 — 색상 팔레트 행 (FR-05, FR-06)
@Composable
private fun ColorPaletteRow(
    label: String,
    colors: List<Color>,
    labels: List<String>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(colors.zip(labels)) { (color, colorLabel) ->
                ThemeCircleCard(
                    color = color,
                    label = colorLabel,
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

// Design Ref: joyary-upgrade-v4 §3.2 — 테마 원형 카드 선택 UI (FR-02, FR-07)
@Composable
private fun ThemeTemplateSelector(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column {
        Text("색상 테마", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(AppThemeTemplates) { template ->
                ThemeCircleCard(
                    color = template.previewColor,
                    label = template.nameKo,
                    isSelected = template.index == selectedIndex,
                    onClick = { onSelect(template.index) }
                )
            }
        }
    }
}

@Composable
private fun ThemeCircleCard(
    color: Color,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .clickable(onClick = onClick)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림 시간 설정") },
        text = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("확인") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
