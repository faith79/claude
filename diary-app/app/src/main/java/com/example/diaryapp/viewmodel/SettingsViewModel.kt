package com.example.diaryapp.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.diaryapp.notification.DailyReminderWorker
import com.example.diaryapp.notification.NotificationPreferences
import com.example.diaryapp.notification.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val notificationPreferences: NotificationPreferences,
    private val themePreferences: ThemePreferences,
    private val workManager: WorkManager
) : ViewModel() {

    private val _reminderEnabled = MutableStateFlow(notificationPreferences.isEnabled)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(notificationPreferences.reminderHour)
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(notificationPreferences.reminderMinute)
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    // Design Ref: joyary-upgrade-v3 §4.1 — 테마 색상 StateFlow (FR-06, FR-07)
    private val _calendarBgColor = MutableStateFlow(Color(themePreferences.calendarBgColor))
    val calendarBgColor: StateFlow<Color> = _calendarBgColor.asStateFlow()

    private val _appBgColor = MutableStateFlow(Color(themePreferences.appBgColor))
    val appBgColor: StateFlow<Color> = _appBgColor.asStateFlow()

    private val _todayBgColor = MutableStateFlow(Color(themePreferences.todayBgColor))
    val todayBgColor: StateFlow<Color> = _todayBgColor.asStateFlow()

    fun setReminderEnabled(enabled: Boolean) {
        notificationPreferences.isEnabled = enabled
        _reminderEnabled.value = enabled
        if (enabled) scheduleReminder() else cancelReminder()
    }

    fun setReminderTime(hour: Int, minute: Int) {
        notificationPreferences.reminderHour = hour
        notificationPreferences.reminderMinute = minute
        _reminderHour.value = hour
        _reminderMinute.value = minute
        if (_reminderEnabled.value) scheduleReminder()
    }

    // Plan SC: SC-03 — 색상 변경 즉시 StateFlow 반영
    fun setCalendarBgColor(color: Color) {
        themePreferences.calendarBgColor = color.toArgb()
        _calendarBgColor.value = color
    }

    fun setAppBgColor(color: Color) {
        themePreferences.appBgColor = color.toArgb()
        _appBgColor.value = color
    }

    fun setTodayBgColor(color: Color) {
        themePreferences.todayBgColor = color.toArgb()
        _todayBgColor.value = color
    }

    fun resetThemeColors() {
        themePreferences.resetToDefaults()
        _calendarBgColor.value = Color(themePreferences.calendarBgColor)
        _appBgColor.value = Color(themePreferences.appBgColor)
        _todayBgColor.value = Color(themePreferences.todayBgColor)
    }

    // Design Ref: joyary-upgrade-v4 §3.2 — templateIndex StateFlow (FR-02~FR-05)
    private val _selectedTemplateIndex = MutableStateFlow(themePreferences.selectedTemplateIndex)
    val selectedTemplateIndex: StateFlow<Int> = _selectedTemplateIndex.asStateFlow()

    fun selectTemplate(index: Int) {
        themePreferences.selectedTemplateIndex = index
        _selectedTemplateIndex.value = index
    }

    fun resetThemeTemplate() {
        themePreferences.resetToDefault()
        _selectedTemplateIndex.value = 0
    }

    // Design Ref: joyary-upgrade-v5 §3.1 — 일기 배경색 StateFlow (FR-05, FR-07)
    private val _diaryBgColor = MutableStateFlow(Color(themePreferences.diaryBgColor))
    val diaryBgColor: StateFlow<Color> = _diaryBgColor.asStateFlow()

    fun setDiaryBgColor(color: Color) {
        themePreferences.diaryBgColor = color.toArgb()
        _diaryBgColor.value = color
    }

    // Design Ref: joyary-upgrade-v5 §3.1 — 평일 글씨색 StateFlow (FR-06, FR-07)
    private val _weekdayColor = MutableStateFlow(Color(themePreferences.weekdayColor))
    val weekdayColor: StateFlow<Color> = _weekdayColor.asStateFlow()

    fun setWeekdayColor(color: Color) {
        themePreferences.weekdayColor = color.toArgb()
        _weekdayColor.value = color
    }

    fun resetDiaryColors() {
        themePreferences.resetDiaryColors()
        _diaryBgColor.value = Color(themePreferences.diaryBgColor)
        _weekdayColor.value = Color(themePreferences.weekdayColor)
    }

    // Design Ref: joyary-upgrade-v6 §5.6 — initialDelay로 설정 시간에 정확한 알림 (FR-11)
    private fun scheduleReminder() {
        val hour = notificationPreferences.reminderHour
        val minute = notificationPreferences.reminderMinute
        val now = LocalDateTime.now()
        val target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        val nextTarget = if (target.isAfter(now)) target else target.plusDays(1)
        val initialDelayMillis = ChronoUnit.MILLIS.between(now, nextTarget)

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun cancelReminder() {
        workManager.cancelUniqueWork("daily_reminder")
    }
}
