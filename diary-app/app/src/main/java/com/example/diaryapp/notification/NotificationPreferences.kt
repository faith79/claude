package com.example.diaryapp.notification

import android.content.Context

class NotificationPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

    var reminderHour: Int
        get() = prefs.getInt("reminder_hour", 21)
        set(value) { prefs.edit().putInt("reminder_hour", value).apply() }

    var reminderMinute: Int
        get() = prefs.getInt("reminder_minute", 0)
        set(value) { prefs.edit().putInt("reminder_minute", value).apply() }

    var isEnabled: Boolean
        get() = prefs.getBoolean("reminder_enabled", true)
        set(value) { prefs.edit().putBoolean("reminder_enabled", value).apply() }
}
