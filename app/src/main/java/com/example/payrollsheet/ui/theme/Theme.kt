package com.example.payrollsheet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Navy900,
    onPrimary = White,
    primaryContainer = NavySoft,
    onPrimaryContainer = Navy900,
    secondary = Teal600,
    onSecondary = White,
    secondaryContainer = TealLight,
    onSecondaryContainer = Teal700,
    tertiary = Amber600,
    onTertiary = White,
    tertiaryContainer = AmberLight,
    onTertiaryContainer = Amber600,
    background = Slate50,
    onBackground = Slate800,
    surface = White,
    onSurface = Slate800,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    error = Red600,
    onError = White,
    errorContainer = RedLight,
    onErrorContainer = Red600
)

private val DarkColorScheme = darkColorScheme(
    primary = Slate200,
    onPrimary = Navy900,
    primaryContainer = Navy800,
    onPrimaryContainer = White,
    secondary = Teal500,
    onSecondary = Navy900,
    secondaryContainer = Teal700,
    onSecondaryContainer = TealLight,
    tertiary = Amber500,
    onTertiary = Navy900,
    background = Navy900,
    onBackground = White,
    surface = Navy800,
    onSurface = White,
    surfaceVariant = Navy700,
    onSurfaceVariant = Slate400,
    outline = Navy700,
    error = Red600,
    onError = White
)

@Composable
fun PayrollSheetTheme(
    darkTheme: Boolean = false, // Strictly light version as requested
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = White.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
            window.navigationBarColor = White.toArgb()
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
