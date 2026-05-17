package com.example.diaryapp.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Design Ref: joyary-upgrade-v3 §3.1 — CompositionLocal 테마 색상 전파 (FR-06)
data class ThemeColors(
    val calendarBg: Color,
    val appBg: Color,
    val todayBg: Color
) {
    companion object {
        val Default = ThemeColors(
            calendarBg = Color(0xFF8EC6E6),
            appBg      = Color(0xFFF0F8FF),
            todayBg    = Color(0xFF7EC8E3)
        )
    }
}

val LocalThemeColors = compositionLocalOf { ThemeColors.Default }
