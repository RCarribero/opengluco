package com.example.opengluco.mobile.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Clases de ancho de ventana según las directrices estándar de Material 3.
 */
enum class WindowWidthClass {
    COMPACT,   // < 600dp: Teléfonos en vertical
    MEDIUM,    // 600dp .. 839dp: Plegables desplegados, tablets pequeñas, teléfonos en horizontal
    EXPANDED   // >= 840dp: Tablets grandes, monitores, modo de escritorio / DeX
}

/**
 * Clases de alto de ventana según las directrices estándar de Material 3.
 */
enum class WindowHeightClass {
    COMPACT,   // < 480dp: Teléfonos en horizontal (altura muy reducida)
    MEDIUM,    // 480dp .. 899dp: Pantallas verticales típicas
    EXPANDED   // >= 900dp: Tablets verticales
}

/**
 * Parámetros de dimensiones y tokens de diseño responsivos calculados dinámicamente.
 */
data class ResponsiveDimensions(
    val widthClass: WindowWidthClass = WindowWidthClass.COMPACT,
    val heightClass: WindowHeightClass = WindowHeightClass.MEDIUM,
    val screenWidthDp: Dp = 360.dp,
    val screenHeightDp: Dp = 800.dp,
    val isLandscape: Boolean = false,
    val isDualColumn: Boolean = false,
    val isNarrowPhone: Boolean = false,
    val contentMaxWidth: Dp = 1140.dp,
    val dialogMaxWidth: Dp = 520.dp,
    val formMaxWidth: Dp = 440.dp,
    val settingsMaxWidth: Dp = 760.dp,
    val horizontalPadding: Dp = 16.dp,
    val cardSpacing: Dp = 16.dp,
    val orbSize: Dp = 120.dp,
    val chartHeight: Dp = 220.dp
)

val LocalResponsiveDimensions = staticCompositionLocalOf { ResponsiveDimensions() }

@Composable
fun rememberResponsiveDimensions(): ResponsiveDimensions {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.dp
    val heightDp = configuration.screenHeightDp.dp
    val isLandscape = widthDp > heightDp

    val widthClass = when {
        widthDp < 600.dp -> WindowWidthClass.COMPACT
        widthDp < 840.dp -> WindowWidthClass.MEDIUM
        else -> WindowWidthClass.EXPANDED
    }

    val heightClass = when {
        heightDp < 480.dp -> WindowHeightClass.COMPACT
        heightDp < 900.dp -> WindowHeightClass.MEDIUM
        else -> WindowHeightClass.EXPANDED
    }

    return remember(widthDp, heightDp, isLandscape) {
        val isNarrow = widthDp < 360.dp
        val isDualColumn = widthClass != WindowWidthClass.COMPACT && heightClass != WindowHeightClass.COMPACT

        val orbSize = when {
            isNarrow -> 100.dp
            widthClass == WindowWidthClass.COMPACT -> 120.dp
            else -> 132.dp
        }

        val horizontalPadding = when {
            isNarrow -> 12.dp
            widthClass == WindowWidthClass.COMPACT -> 16.dp
            else -> 24.dp
        }

        val chartHeight = when {
            heightClass == WindowHeightClass.COMPACT -> 180.dp
            widthClass == WindowWidthClass.COMPACT -> 220.dp
            else -> 240.dp
        }

        val cardSpacing = if (isNarrow) 12.dp else 16.dp

        ResponsiveDimensions(
            widthClass = widthClass,
            heightClass = heightClass,
            screenWidthDp = widthDp,
            screenHeightDp = heightDp,
            isLandscape = isLandscape,
            isDualColumn = isDualColumn,
            isNarrowPhone = isNarrow,
            contentMaxWidth = if (widthClass == WindowWidthClass.EXPANDED) 1160.dp else 920.dp,
            dialogMaxWidth = 520.dp,
            formMaxWidth = 440.dp,
            settingsMaxWidth = 760.dp,
            horizontalPadding = horizontalPadding,
            cardSpacing = cardSpacing,
            orbSize = orbSize,
            chartHeight = chartHeight
        )
    }
}
