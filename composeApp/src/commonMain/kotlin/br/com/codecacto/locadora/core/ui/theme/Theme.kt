package br.com.codecacto.locadora.core.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// App sempre usa tema claro - sem suporte a dark mode
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
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}
