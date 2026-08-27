package com.example.opengluco.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val WearColorScheme = ColorScheme(
    primary = WearPrimary,
    onPrimary = WearOnPrimary,
    secondary = WearSecondary,
    onSecondary = WearDarkBackground,
    background = WearDarkBackground,
    onBackground = WearTextPrimary,
    surfaceContainer = WearSurface,
    onSurface = WearTextPrimary
)

@Composable
fun LibreWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WearColorScheme,
        content = content
    )
}
