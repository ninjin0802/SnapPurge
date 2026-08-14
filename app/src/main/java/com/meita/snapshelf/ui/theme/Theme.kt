package com.meita.snapshelf.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.meita.snapshelf.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = SnapPurple,
    secondary = SnapCyan,
    tertiary = SnapRose,
    background = SnapCloud,
    surface = SnapCloud,
    onPrimary = SnapCloud,
    onBackground = SnapInk,
    onSurface = SnapInk
)

private val DarkColors = darkColorScheme(
    primary = ColorTokens.DarkPrimary,
    secondary = SnapCyan,
    tertiary = SnapRose,
    background = SnapInk,
    surface = ColorTokens.DarkSurface,
    onPrimary = SnapInk,
    onBackground = SnapCloud,
    onSurface = SnapCloud
)

@Composable
fun SnapShelfTheme(
    themeMode: ThemeMode?,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode ?: ThemeMode.System) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = SnapTypography,
        shapes = SnapShapes,
        content = content
    )
}

private object ColorTokens {
    val DarkPrimary = androidx.compose.ui.graphics.Color(0xFFA78BFA)
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF111827)
}

