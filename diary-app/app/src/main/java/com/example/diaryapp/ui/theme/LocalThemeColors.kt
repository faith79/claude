package com.example.diaryapp.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Design Ref: joyary-upgrade-v3 §3.1 — CompositionLocal 테마 색상 전파 (FR-06)
// Design Ref: joyary-upgrade-v5 §3.1 — diaryBg, weekdayColor 선택적 파라미터 추가 (KD-01)
data class ThemeColors(
    val calendarBg: Color,
    val appBg: Color,
    val todayBg: Color,
    val diaryBg: Color = Color(0xFFFFF8F0),      // 기본: 크림
    val weekdayColor: Color = Color(0xFF424242)   // 기본: 진회색
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
