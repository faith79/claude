package com.example.diaryapp.notification

import android.content.Context

// Design Ref: joyary-upgrade-v3 §2.1 — SharedPreferences 색상 저장소 (FR-07)
class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    // 달력 배경색 기본값: SkyCalendarBg #8EC6E6
    var calendarBgColor: Int
        get() = prefs.getInt("calendar_bg_color", 0xFF8EC6E6.toInt())
        set(value) { prefs.edit().putInt("calendar_bg_color", value).apply() }

    // 앱 배경색 기본값: SkyBackground #F0F8FF
    var appBgColor: Int
        get() = prefs.getInt("app_bg_color", 0xFFF0F8FF.toInt())
        set(value) { prefs.edit().putInt("app_bg_color", value).apply() }

    // 오늘 날짜 배경색 기본값: SkyBlue #7EC8E3
    var todayBgColor: Int
        get() = prefs.getInt("today_bg_color", 0xFF7EC8E3.toInt())
        set(value) { prefs.edit().putInt("today_bg_color", value).apply() }

    fun resetToDefaults() {
        prefs.edit()
            .putInt("calendar_bg_color", 0xFF8EC6E6.toInt())
            .putInt("app_bg_color", 0xFFF0F8FF.toInt())
            .putInt("today_bg_color", 0xFF7EC8E3.toInt())
            .apply()
    }
}
