package com.example.diaryapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Design Ref: §5.1 — dynamicColor 비활성, 파스텔 팔레트 고정 (SC-07)
private val PastelLightColorScheme = lightColorScheme(
    primary = PastelCoral,
    onPrimary = PastelOnPrimary,
    primaryContainer = PastelPeach,
    onPrimaryContainer = PastelBrown,
    secondary = PastelPink,
    onSecondary = PastelBrown,
    secondaryContainer = PastelRose,
    onSecondaryContainer = PastelBrown,
    tertiary = PastelLavender,
    onTertiary = PastelBrown,
    surface = PastelSurface,
    onSurface = PastelOnSurface,
    background = PastelBackground,
    onBackground = PastelOnSurface,
    error = PastelError,
    onError = PastelOnPrimary
)

private val PastelDarkColorScheme = darkColorScheme(
    primary = DeepCoral,
    onPrimary = PastelBrown,
    primaryContainer = DeepPeach,
    onPrimaryContainer = PastelPeach,
    secondary = DeepPink,
    onSecondary = PastelBrown,
    surface = DarkSurface,
    onSurface = PastelPeach,
    background = DarkBackground,
    onBackground = PastelPeach,
    error = PastelError
)

@Composable
fun DiaryAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PastelDarkColorScheme else PastelLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
