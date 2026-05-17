package com.example.diaryapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.diaryapp.ui.theme.AppBgPalette
import com.example.diaryapp.ui.theme.CalendarBgPalette
import com.example.diaryapp.ui.theme.TodayBgPalette
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

    // Design Ref: joyary-upgrade-v3 §5.2 — 테마 색상 StateFlow collect (FR-03,04,05)
    val calendarBgColor by settingsViewModel.calendarBgColor.collectAsStateWithLifecycle()
    val appBgColor by settingsViewModel.appBgColor.collectAsStateWithLifecycle()
    val todayBgColor by settingsViewModel.todayBgColor.collectAsStateWithLifecycle()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
            // Design Ref: joyary-upgrade-v3 §5.2 — 색상 팔레트 UI (FR-02,FR-03,FR-04,FR-05,FR-08)
            Text(
                "테마",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ColorPaletteRow(
                        label = "달력 배경색",
                        palette = CalendarBgPalette,
                        selectedColor = calendarBgColor,
                        onColorSelected = settingsViewModel::setCalendarBgColor
                    )
                    HorizontalDivider()
                    ColorPaletteRow(
                        label = "앱 배경색",
                        palette = AppBgPalette,
                        selectedColor = appBgColor,
                        onColorSelected = settingsViewModel::setAppBgColor
                    )
                    HorizontalDivider()
                    ColorPaletteRow(
                        label = "오늘 날짜 배경색",
                        palette = TodayBgPalette,
                        selectedColor = todayBgColor,
                        onColorSelected = settingsViewModel::setTodayBgColor
                    )
                    HorizontalDivider()
                    // Plan SC: SC-05 — 기본값으로 초기화 (FR-08)
                    TextButton(
                        onClick = settingsViewModel::resetThemeColors,
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

// Design Ref: joyary-upgrade-v3 §5.2 — 색상 원형 팔레트 선택 컴포넌트 (FR-03~FR-05)
@Composable
private fun ColorPaletteRow(
    label: String,
    palette: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(palette) { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
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
