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

    // Design Ref: joyary-upgrade-v4 §3.2 — templateIndex 저장 (FR-05)
    var selectedTemplateIndex: Int
        get() = prefs.getInt("selected_theme_index", 0)
        set(value) { prefs.edit().putInt("selected_theme_index", value).apply() }

    fun resetToDefault() { selectedTemplateIndex = 0 }

    // Design Ref: joyary-upgrade-v5 §3.3 — 일기 배경색 저장 (FR-05, FR-07)
    var diaryBgColor: Int
        get() = prefs.getInt("diary_bg_color", 0xFFFFF8F0.toInt())
        set(value) { prefs.edit().putInt("diary_bg_color", value).apply() }

    // Design Ref: joyary-upgrade-v5 §3.3 — 평일 글씨색 저장 (FR-06, FR-07)
    var weekdayColor: Int
        get() = prefs.getInt("weekday_color", 0xFF424242.toInt())
        set(value) { prefs.edit().putInt("weekday_color", value).apply() }

    fun resetDiaryColors() {
        diaryBgColor = 0xFFFFF8F0.toInt()
        weekdayColor = 0xFF424242.toInt()
    }
}
