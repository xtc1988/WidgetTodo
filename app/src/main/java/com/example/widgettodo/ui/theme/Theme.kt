package com.example.widgettodo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Zen Garden Color Palette
object ZenColors {
    val Washi = Color(0xFFF5F2EB)        // 和紙 - Primary background
    val Sand = Color(0xFFE8E4DA)          // 砂 - Secondary background
    val Moss = Color(0xFF5C7A5C)          // 苔 - Primary accent
    val Wood = Color(0xFF8B7355)          // 木 - Secondary accent
    val Ink = Color(0xFF2C2C2C)           // 墨 - Text
    val White = Color(0xFFFFFFFF)         // 白 - Card background
    val Stone = Color(0xFFD4CFC4)         // 石 - Border/divider
    val DarkWashi = Color(0xFF2A2822)     // Dark mode background
    val DarkSand = Color(0xFF3D3A33)      // Dark mode surface
    val LightMoss = Color(0xFF7A9A7A)     // Light moss for dark mode
}

private val ZenLightColorScheme = lightColorScheme(
    primary = ZenColors.Moss,
    onPrimary = Color.White,
    primaryContainer = ZenColors.Moss.copy(alpha = 0.2f),
    onPrimaryContainer = ZenColors.Moss,
    secondary = ZenColors.Wood,
    onSecondary = Color.White,
    secondaryContainer = ZenColors.Wood.copy(alpha = 0.2f),
    onSecondaryContainer = ZenColors.Wood,
    tertiary = ZenColors.Stone,
    background = ZenColors.Washi,
    onBackground = ZenColors.Ink,
    surface = ZenColors.White,
    onSurface = ZenColors.Ink,
    surfaceVariant = ZenColors.Sand,
    onSurfaceVariant = ZenColors.Ink.copy(alpha = 0.7f),
    outline = ZenColors.Stone,
    outlineVariant = ZenColors.Stone.copy(alpha = 0.5f)
)

private val ZenDarkColorScheme = darkColorScheme(
    primary = ZenColors.LightMoss,
    onPrimary = ZenColors.DarkWashi,
    primaryContainer = ZenColors.Moss.copy(alpha = 0.3f),
    onPrimaryContainer = ZenColors.LightMoss,
    secondary = ZenColors.Wood,
    onSecondary = ZenColors.DarkWashi,
    secondaryContainer = ZenColors.Wood.copy(alpha = 0.3f),
    onSecondaryContainer = ZenColors.Wood,
    tertiary = ZenColors.Stone,
    background = ZenColors.DarkWashi,
    onBackground = ZenColors.Washi,
    surface = ZenColors.DarkSand,
    onSurface = ZenColors.Washi,
    surfaceVariant = ZenColors.DarkSand,
    onSurfaceVariant = ZenColors.Washi.copy(alpha = 0.7f),
    outline = ZenColors.Stone.copy(alpha = 0.5f),
    outlineVariant = ZenColors.Stone.copy(alpha = 0.3f)
)

@Composable
fun WidgetTodoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ZenDarkColorScheme else ZenLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
