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

// 10: 인디고
private val IndigoTemplate = AppThemeTemplate(
    index = 10, nameKo = "인디고",
    previewColor = Color(0xFF5C7CFA),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF5C7CFA),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFDBE4FF),
        onPrimaryContainer     = Color(0xFF1C3BBF),
        secondary              = Color(0xFF4C6EF5),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFEEF2FF),
        onSecondaryContainer   = Color(0xFF1C3BBF),
        background             = Color(0xFFF0F2FF),
        onBackground           = Color(0xFF1A1E3A),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF1A1E3A),
        surfaceVariant         = Color(0xFFE8ECFF),
        onSurfaceVariant       = Color(0xFF4A4E72),
        outline                = Color(0xFF9AAAF8),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF7C9BFA),
        appBg      = Color(0xFFF0F2FF),
        todayBg    = Color(0xFF6888F5)
    )
)

// 11: 에메랄드
private val EmeraldTemplate = AppThemeTemplate(
    index = 11, nameKo = "에메랄드",
    previewColor = Color(0xFF2ECC71),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF2ECC71),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFB7F5D2),
        onPrimaryContainer     = Color(0xFF0A5C30),
        secondary              = Color(0xFF27AE60),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFD5F5E3),
        onSecondaryContainer   = Color(0xFF0A5C30),
        background             = Color(0xFFF0FFF8),
        onBackground           = Color(0xFF0A2E1A),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF0A2E1A),
        surfaceVariant         = Color(0xFFD5F5E3),
        onSurfaceVariant       = Color(0xFF2A6040),
        outline                = Color(0xFF6EE0A0),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF55D98A),
        appBg      = Color(0xFFF0FFF8),
        todayBg    = Color(0xFF3DC876)
    )
)

// 12: 써니
private val SunnyTemplate = AppThemeTemplate(
    index = 12, nameKo = "써니",
    previewColor = Color(0xFFFFCA28),
    colorScheme = lightColorScheme(
        primary                = Color(0xFFFFCA28),
        onPrimary              = Color(0xFF3A2A00),
        primaryContainer       = Color(0xFFFFF3C4),
        onPrimaryContainer     = Color(0xFF5A4200),
        secondary              = Color(0xFFFFB300),
        onSecondary            = Color(0xFF3A2A00),
        secondaryContainer     = Color(0xFFFFF8DC),
        onSecondaryContainer   = Color(0xFF5A4200),
        background             = Color(0xFFFFFDE7),
        onBackground           = Color(0xFF2A2200),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF2A2200),
        surfaceVariant         = Color(0xFFFFF9C4),
        onSurfaceVariant       = Color(0xFF6A5A00),
        outline                = Color(0xFFE8C040),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFFFD740),
        appBg      = Color(0xFFFFFDE7),
        todayBg    = Color(0xFFFFCA28)
    )
)

// 13: 체리
private val CherryTemplate = AppThemeTemplate(
    index = 13, nameKo = "체리",
    previewColor = Color(0xFFC0392B),
    colorScheme = lightColorScheme(
        primary                = Color(0xFFC0392B),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFFFCDD2),
        onPrimaryContainer     = Color(0xFF7B1818),
        secondary              = Color(0xFFE74C3C),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFFFEBEE),
        onSecondaryContainer   = Color(0xFF7B1818),
        background             = Color(0xFFFFF5F5),
        onBackground           = Color(0xFF2A0A0A),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF2A0A0A),
        surfaceVariant         = Color(0xFFFFE0E0),
        onSurfaceVariant       = Color(0xFF723030),
        outline                = Color(0xFFD87878),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFD05050),
        appBg      = Color(0xFFFFF5F5),
        todayBg    = Color(0xFFBF3838)
    )
)

// 14: 딥블루
private val DeepBlueTemplate = AppThemeTemplate(
    index = 14, nameKo = "딥블루",
    previewColor = Color(0xFF1565C0),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF1565C0),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFBBDEFB),
        onPrimaryContainer     = Color(0xFF0D3E7A),
        secondary              = Color(0xFF1976D2),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFE3F2FD),
        onSecondaryContainer   = Color(0xFF0D3E7A),
        background             = Color(0xFFF0F4FF),
        onBackground           = Color(0xFF0A1A30),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF0A1A30),
        surfaceVariant         = Color(0xFFDDEEFF),
        onSurfaceVariant       = Color(0xFF2A4060),
        outline                = Color(0xFF6898D8),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF3A7ED8),
        appBg      = Color(0xFFF0F4FF),
        todayBg    = Color(0xFF2060C0)
    )
)

// 15: 올리브
private val OliveTemplate = AppThemeTemplate(
    index = 15, nameKo = "올리브",
    previewColor = Color(0xFF8BC34A),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF8BC34A),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFDCEDC8),
        onPrimaryContainer     = Color(0xFF33691E),
        secondary              = Color(0xFF7CB342),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFF1F8E9),
        onSecondaryContainer   = Color(0xFF33691E),
        background             = Color(0xFFF5FAF0),
        onBackground           = Color(0xFF1A2C10),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF1A2C10),
        surfaceVariant         = Color(0xFFEAF4DA),
        onSurfaceVariant       = Color(0xFF4A6030),
        outline                = Color(0xFFA0C870),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFA4D46A),
        appBg      = Color(0xFFF5FAF0),
        todayBg    = Color(0xFF8EC058)
    )
)

// 16: 스틸
private val SteelTemplate = AppThemeTemplate(
    index = 16, nameKo = "스틸",
    previewColor = Color(0xFF78909C),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF78909C),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFCFD8DC),
        onPrimaryContainer     = Color(0xFF37474F),
        secondary              = Color(0xFF607D8B),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFECEFF1),
        onSecondaryContainer   = Color(0xFF37474F),
        background             = Color(0xFFF4F7F9),
        onBackground           = Color(0xFF1A2530),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF1A2530),
        surfaceVariant         = Color(0xFFE4EBF0),
        onSurfaceVariant       = Color(0xFF4A5A60),
        outline                = Color(0xFF9AB0B8),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF90A8B4),
        appBg      = Color(0xFFF4F7F9),
        todayBg    = Color(0xFF7898A4)
    )
)

// 17: 자수정
private val AmethystTemplate = AppThemeTemplate(
    index = 17, nameKo = "자수정",
    previewColor = Color(0xFF8E44AD),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF8E44AD),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFE1BEE7),
        onPrimaryContainer     = Color(0xFF4A1470),
        secondary              = Color(0xFF9B59B6),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFF3E5F5),
        onSecondaryContainer   = Color(0xFF4A1470),
        background             = Color(0xFFF8F0FF),
        onBackground           = Color(0xFF200A30),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF200A30),
        surfaceVariant         = Color(0xFFEDD8F8),
        onSurfaceVariant       = Color(0xFF5E2880),
        outline                = Color(0xFFBE80D8),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFFAA60C8),
        appBg      = Color(0xFFF8F0FF),
        todayBg    = Color(0xFF9848B8)
    )
)

// 18: 오션
private val OceanTemplate = AppThemeTemplate(
    index = 18, nameKo = "오션",
    previewColor = Color(0xFF00ACC1),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF00ACC1),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFB2EBF2),
        onPrimaryContainer     = Color(0xFF005B6A),
        secondary              = Color(0xFF0097A7),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFE0F7FA),
        onSecondaryContainer   = Color(0xFF005B6A),
        background             = Color(0xFFF0FAFA),
        onBackground           = Color(0xFF001E28),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF001E28),
        surfaceVariant         = Color(0xFFD8F4F8),
        onSurfaceVariant       = Color(0xFF205860),
        outline                = Color(0xFF60C8D8),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF30C0D4),
        appBg      = Color(0xFFF0FAFA),
        todayBg    = Color(0xFF18A8C0)
    )
)

// 19: 차콜
private val CharcoalTemplate = AppThemeTemplate(
    index = 19, nameKo = "차콜",
    previewColor = Color(0xFF546E7A),
    colorScheme = lightColorScheme(
        primary                = Color(0xFF546E7A),
        onPrimary              = Color(0xFFFFFFFF),
        primaryContainer       = Color(0xFFCFD8DC),
        onPrimaryContainer     = Color(0xFF263238),
        secondary              = Color(0xFF455A64),
        onSecondary            = Color(0xFFFFFFFF),
        secondaryContainer     = Color(0xFFECEFF1),
        onSecondaryContainer   = Color(0xFF263238),
        background             = Color(0xFFF5F6F7),
        onBackground           = Color(0xFF1A2228),
        surface                = Color(0xFFFFFFFF),
        onSurface              = Color(0xFF1A2228),
        surfaceVariant         = Color(0xFFE2E8EC),
        onSurfaceVariant       = Color(0xFF425058),
        outline                = Color(0xFF8898A0),
        error                  = Color(0xFFE57373),
        onError                = Color(0xFFFFFFFF)
    ),
    themeColors = ThemeColors(
        calendarBg = Color(0xFF708890),
        appBg      = Color(0xFFF5F6F7),
        todayBg    = Color(0xFF5A7078)
    )
)

// Design Ref: joyary-ux-improvements §FR-05 — 10→20종 통합 테마 목록
val AppThemeTemplates: List<AppThemeTemplate> = listOf(
    SkyTemplate, MintTemplate, LavenderTemplate, PeachTemplate, RoseTemplate,
    SageTemplate, ButterTemplate, LilacTemplate, CoralTemplate, MochaTemplate,
    IndigoTemplate, EmeraldTemplate, SunnyTemplate, CherryTemplate, DeepBlueTemplate,
    OliveTemplate, SteelTemplate, AmethystTemplate, OceanTemplate, CharcoalTemplate
)
