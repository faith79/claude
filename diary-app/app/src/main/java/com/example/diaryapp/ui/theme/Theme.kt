package com.example.diaryapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Design Ref: §4.1 — SkyBlue 파스텔 팔레트 적용 (FR-01), dynamicColor 비활성
private val SkyLightColorScheme = lightColorScheme(
    primary = SkyBlue,
    onPrimary = SkyOnPrimary,
    primaryContainer = SkyBlueLight,
    onPrimaryContainer = SkyDeepBlue,
    secondary = SkyMint,
    onSecondary = SkyOnPrimary,
    secondaryContainer = SkyBluePale,
    onSecondaryContainer = SkyDeepBlue,
    tertiary = SkyLavender,
    onTertiary = SkyDeepBlue,
    surface = SkySurface,
    onSurface = SkyOnSurface,
    surfaceVariant = SkyBluePale,
    onSurfaceVariant = SkyOnSurfaceVar,
    background = SkyBackground,
    onBackground = SkyOnSurface,
    error = SkyError,
    onError = SkyOnPrimary
)

private val SkyDarkColorScheme = darkColorScheme(
    primary = SkyDarkPrimary,
    onPrimary = SkyDeepBlue,
    primaryContainer = SkyDarkContainer,
    onPrimaryContainer = SkyBlueLight,
    secondary = SkyMint,
    onSecondary = SkyDeepBlue,
    surface = SkyDarkSurface,
    onSurface = SkyDarkOnSurface,
    background = SkyDarkBackground,
    onBackground = SkyDarkOnSurface,
    error = SkyError
)

@Composable
fun DiaryAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SkyDarkColorScheme else SkyLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
