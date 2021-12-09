package com.kennyc.solarviewer.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

val DarkColors = darkColors(
    primary = Theme_Blue_200,
    onPrimary = Theme_Blue_700,
    secondary = Theme_Orange_500,
    onSecondary = Theme_Orange_200
)

val LightColors = lightColors(
    primary = Theme_Blue_700,
    onPrimary = Theme_Blue_800,
    secondary = Theme_Orange_500,
    onSecondary = Theme_Orange_200
)

@Composable
fun AppTheme(isDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val color = when {
        isDarkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(colors = color, content = content, typography = Typography(googleSans))
}


/* Material 3 Themes
private val DarkColorScheme = darkColorScheme(
    primary = Theme_Blue_200,
    onPrimary = Theme_Blue_200,
    secondary = Theme_Orange_500,
    onSecondary = Theme_Orange_200
)

private val LightColorScheme = lightColorScheme(
    primary = Theme_Blue_700,
    onPrimary = Theme_Blue_800,
    secondary = Theme_Orange_500,
    onSecondary = Theme_Orange_200
)

@SuppressLint("NewApi")
@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColorIfAvailable: Boolean = true,
    content: @Composable () -> Unit
) {
    val dynamicColor = useDynamicColorIfAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        dynamicColor && isDarkTheme -> dynamicDarkColorScheme(LocalContext.current)

        dynamicColor && !isDarkTheme -> dynamicLightColorScheme(LocalContext.current)

        isDarkTheme -> DarkColorScheme

        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}*/