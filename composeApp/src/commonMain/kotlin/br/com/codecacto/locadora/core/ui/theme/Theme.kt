package br.com.codecacto.locadora.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Violet500,
    onPrimary = Color.White,
    primaryContainer = AppColors.Violet600,
    onPrimaryContainer = AppColors.Violet100,
    secondary = AppColors.Purple600,
    onSecondary = Color.White,
    secondaryContainer = AppColors.Violet400,
    onSecondaryContainer = AppColors.Violet100,
    tertiary = AppColors.Emerald600,
    onTertiary = Color.White,
    tertiaryContainer = AppColors.GreenDark,
    onTertiaryContainer = AppColors.GreenLight,
    error = AppColors.Red,
    onError = Color.White,
    errorContainer = AppColors.RedDark,
    onErrorContainer = AppColors.RedLight,
    background = AppColors.Slate900,
    onBackground = AppColors.Slate100,
    surface = AppColors.Slate800,
    onSurface = AppColors.Slate100,
    surfaceVariant = AppColors.Slate700,
    onSurfaceVariant = AppColors.Slate300,
    outline = AppColors.Slate600,
    outlineVariant = AppColors.Slate700,
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Violet600,
    onPrimary = Color.White,
    primaryContainer = AppColors.Violet100,
    onPrimaryContainer = AppColors.Violet600,
    secondary = AppColors.Purple600,
    onSecondary = Color.White,
    secondaryContainer = AppColors.Violet100,
    onSecondaryContainer = AppColors.Violet600,
    tertiary = AppColors.Emerald600,
    onTertiary = Color.White,
    tertiaryContainer = AppColors.Emerald100,
    onTertiaryContainer = AppColors.GreenDark,
    error = AppColors.Red,
    onError = Color.White,
    errorContainer = AppColors.RedLight,
    onErrorContainer = AppColors.RedDark,
    background = AppColors.Slate50,
    onBackground = AppColors.Slate900,
    surface = Color.White,
    onSurface = AppColors.Slate900,
    surfaceVariant = AppColors.Slate100,
    onSurfaceVariant = AppColors.Slate600,
    outline = AppColors.Slate300,
    outlineVariant = AppColors.Slate200,
)

@Composable
fun AppTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme == null -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
