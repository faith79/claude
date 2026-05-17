package com.example.diaryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.diaryapp.notification.DailyReminderWorker
import com.example.diaryapp.notification.NotificationPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val notificationPreferences: NotificationPreferences,
    private val workManager: WorkManager
) : ViewModel() {

    private val _reminderEnabled = MutableStateFlow(notificationPreferences.isEnabled)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(notificationPreferences.reminderHour)
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(notificationPreferences.reminderMinute)
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

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

    private fun scheduleReminder() {
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
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
