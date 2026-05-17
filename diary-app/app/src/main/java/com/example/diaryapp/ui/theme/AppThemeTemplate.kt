package com.example.diaryapp.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Design Ref: joyary-upgrade-v4 §3.1 — 10종 파스텔 통합 테마 (FR-02~FR-04)
data class AppThemeTemplate(
    val index: Int,
    val nameKo: String,
    val previewColor: Color,
    val colorScheme: androidx.compose.material3.ColorScheme,
    val themeColors: ThemeColors
)

// 0: 하늘
private val SkyTemplate = AppThemeTemplate(
    index = 0, nameKo = "하늘",
    previewColor = Color(0xFF5BBEE0),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF5BBEE0),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFB3E5FC),
        onPrimaryContainer     = Color(0xFF1565C0),
        secondary              = Color(0xFF4AABCC),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFE1F5FE),
        onSecondaryContainer   = Color(0xFF01579B),
        background             = Color(0xFFF0F8FF),
        onBackground           = Color(0xFF1A2A3A),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF1A2A3A),
        surfaceVariant         = Color(0xFFE1F5FE),
        onSurfaceVariant       = Color(0xFF4A6072),
        outline                = Color(0xFF90CAD8),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF8EC6E6),
        appBg      = Color(0xFFF0F8FF),
        todayBg    = Color(0xFF7EC8E3)
    )
)

// 1: 민트
private val MintTemplate = AppThemeTemplate(
    index = 1, nameKo = "민트",
    previewColor = Color(0xFF4CAF8B),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF4CAF8B),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFB2DFDB),
        onPrimaryContainer     = Color(0xFF1B5E20),
        secondary              = Color(0xFF3A9B78),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFE8F5E9),
        onSecondaryContainer   = Color(0xFF1B5E20),
        background             = Color(0xFFF0FAF6),
        onBackground           = Color(0xFF1A2E24),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF1A2E24),
        surfaceVariant         = Color(0xFFE8F5E9),
        onSurfaceVariant       = Color(0xFF4A6058),
        outline                = Color(0xFF90C8A8),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF80CBA9),
        appBg      = Color(0xFFF0FAF6),
        todayBg    = Color(0xFF66BB99)
    )
)

// 2: 라벤더
private val LavenderTemplate = AppThemeTemplate(
    index = 2, nameKo = "라벤더",
    previewColor = Color(0xFF9C88C8),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF9C88C8),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFE1D5FF),
        onPrimaryContainer     = Color(0xFF4A148C),
        secondary              = Color(0xFF8878B4),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFF3E5F5),
        onSecondaryContainer   = Color(0xFF4A148C),
        background             = Color(0xFFF5F0FF),
        onBackground           = Color(0xFF1E1A2E),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF1E1A2E),
        surfaceVariant         = Color(0xFFEDE7FF),
        onSurfaceVariant       = Color(0xFF5A4A72),
        outline                = Color(0xFFB0A0D8),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFB4A0D8),
        appBg      = Color(0xFFF5F0FF),
        todayBg    = Color(0xFFA08AC0)
    )
)

// 3: 피치
private val PeachTemplate = AppThemeTemplate(
    index = 3, nameKo = "피치",
    previewColor = Color(0xFFE8956D),
    colorScheme = lightColorScheme(
        primary                = Color(0xFFE8956D),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFFFCCBC),
        onPrimaryContainer     = Color(0xFFBF360C),
        secondary              = Color(0xFFD4815A),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFFFF3E0),
        onSecondaryContainer   = Color(0xFFBF360C),
        background             = Color(0xFFFFF5EE),
        onBackground           = Color(0xFF2E1A0A),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF2E1A0A),
        surfaceVariant         = Color(0xFFFFECE0),
        onSurfaceVariant       = Color(0xFF72503A),
        outline                = Color(0xFFD8A090),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFF0A882),
        appBg      = Color(0xFFFFF5EE),
        todayBg    = Color(0xFFE89070)
    )
)

// 4: 로즈
private val RoseTemplate = AppThemeTemplate(
    index = 4, nameKo = "로즈",
    previewColor = Color(0xFFD4779A),
    colorScheme = lightColorScheme(
        primary                = Color(0xFFD4779A),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFFFCDD2),
        onPrimaryContainer     = Color(0xFF880E4F),
        secondary              = Color(0xFFC06488),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFFCE4EC),
        onSecondaryContainer   = Color(0xFF880E4F),
        background             = Color(0xFFFFF0F5),
        onBackground           = Color(0xFF2E0A1A),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF2E0A1A),
        surfaceVariant         = Color(0xFFFFE0EC),
        onSurfaceVariant       = Color(0xFF724050),
        outline                = Color(0xFFD890A8),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFE099B4),
        appBg      = Color(0xFFFFF0F5),
        todayBg    = Color(0xFFCC80A8)
    )
)

// 5: 세이지
private val SageTemplate = AppThemeTemplate(
    index = 5, nameKo = "세이지",
    previewColor = Color(0xFF78A882),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF78A882),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFC8E6C9),
        onPrimaryContainer     = Color(0xFF1B5E20),
        secondary              = Color(0xFF66946E),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFE8F5E9),
        onSecondaryContainer   = Color(0xFF1B5E20),
        background             = Color(0xFFF2F8F0),
        onBackground           = Color(0xFF1A2E1C),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF1A2E1C),
        surfaceVariant         = Color(0xFFE8F5E9),
        onSurfaceVariant       = Color(0xFF4A6050),
        outline                = Color(0xFF98C0A0),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF95C09A),
        appBg      = Color(0xFFF2F8F0),
        todayBg    = Color(0xFF80B087)
    )
)

// 6: 버터
private val ButterTemplate = AppThemeTemplate(
    index = 6, nameKo = "버터",
    previewColor = Color(0xFFD4B84A),
    colorScheme = lightColorScheme(
        primary                = Color(0xFFD4B84A),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFFFF9C4),
        onPrimaryContainer     = Color(0xFF827717),
        secondary              = Color(0xFFBCA438),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFFFFDE7),
        onSecondaryContainer   = Color(0xFF827717),
        background             = Color(0xFFFFFDF0),
        onBackground           = Color(0xFF2A2410),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF2A2410),
        surfaceVariant         = Color(0xFFFFFAD0),
        onSurfaceVariant       = Color(0xFF6A5C20),
        outline                = Color(0xFFD8C870),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFE0C860),
        appBg      = Color(0xFFFFFDF0),
        todayBg    = Color(0xFFCDB845)
    )
)

// 7: 릴락
private val LilacTemplate = AppThemeTemplate(
    index = 7, nameKo = "릴락",
    previewColor = Color(0xFFB088C8),
    colorScheme = lightColorScheme(
        primary                = Color(0xFFB088C8),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFE8D5FF),
        onPrimaryContainer     = Color(0xFF6A1B9A),
        secondary              = Color(0xFF9C74B4),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFF3E5F5),
        onSecondaryContainer   = Color(0xFF6A1B9A),
        background             = Color(0xFFF8F0FF),
        onBackground           = Color(0xFF201828),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF201828),
        surfaceVariant         = Color(0xFFEFE0FF),
        onSurfaceVariant       = Color(0xFF5E4878),
        outline                = Color(0xFFBCA8D8),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFC4A0D8),
        appBg      = Color(0xFFF8F0FF),
        todayBg    = Color(0xFFA878C0)
    )
)

// 8: 코랄
private val CoralTemplate = AppThemeTemplate(
    index = 8, nameKo = "코랄",
    previewColor = Color(0xFFE87870),
    colorScheme = lightColorScheme(
        primary                = Color(0xFFE87870),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFFFCDD2),
        onPrimaryContainer     = Color(0xFFB71C1C),
        secondary              = Color(0xFFD46460),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFFFEBEE),
        onSecondaryContainer   = Color(0xFFB71C1C),
        background             = Color(0xFFFFF2F0),
        onBackground           = Color(0xFF2E0E0A),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF2E0E0A),
        surfaceVariant         = Color(0xFFFFE0E0),
        onSurfaceVariant       = Color(0xFF724040),
        outline                = Color(0xFFD89090),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFF09090),
        appBg      = Color(0xFFFFF2F0),
        todayBg    = Color(0xFFE07878)
    )
)

// 9: 모카
private val MochaTemplate = AppThemeTemplate(
    index = 9, nameKo = "모카",
    previewColor = Color(0xFFB09070),
    colorScheme = lightColorScheme(
        primary                = Color(0xFFB09070),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFE8D8C8),
        onPrimaryContainer     = Color(0xFF5D4037),
        secondary              = Color(0xFF9C7C5C),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFF5EDE0),
        onSecondaryContainer   = Color(0xFF5D4037),
        background             = Color(0xFFFBF8F5),
        onBackground           = Color(0xFF261C10),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF261C10),
        surfaceVariant         = Color(0xFFF0E8D8),
        onSurfaceVariant       = Color(0xFF6A5440),
        outline                = Color(0xFFC8A888),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFC8A888),
        appBg      = Color(0xFFFBF8F5),
        todayBg    = Color(0xFFA89070)
    )
)

// Plan SC: SC-02 — 10종 통합 테마 목록
val AppThemeTemplates: List<AppThemeTemplate> = listOf(
    SkyTemplate, MintTemplate, LavenderTemplate, PeachTemplate, RoseTemplate,
    SageTemplate, ButterTemplate, LilacTemplate, CoralTemplate, MochaTemplate
)
