package com.example.opengluco.mobile.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val fontScale: Float = 1.0f,
    val isLandscape: Boolean = false,
    val isDualColumn: Boolean = false,
    val isNarrowPhone: Boolean = false,
    val isLargeFont: Boolean = false,
    val isExtraLargeFont: Boolean = false,
    val contentMaxWidth: Dp = 1140.dp,
    val dialogMaxWidth: Dp = 520.dp,
    val formMaxWidth: Dp = 440.dp,
    val settingsMaxWidth: Dp = 760.dp,
    val horizontalPadding: Dp = 16.dp,
    val cardSpacing: Dp = 16.dp,
    val orbSize: Dp = 120.dp,
    val chartHeight: Dp = 220.dp
) {
    /**
     * Calcula un tamaño de texto en sp acotando el escalado de accesibilidad para evitar desbordes
     * en componentes cerrados (ej. orbs, badges o chips).
     */
    fun clampedSp(baseSp: Float, maxScale: Float = 1.30f, minScale: Float = 0.85f): TextUnit {
        val effectiveScale = fontScale.coerceIn(minScale, maxScale)
        return (baseSp * (effectiveScale / fontScale)).sp
    }
}

val LocalResponsiveDimensions = staticCompositionLocalOf { ResponsiveDimensions() }

@Composable
fun rememberResponsiveDimensions(): ResponsiveDimensions {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val fontScale = density.fontScale

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

    return remember(widthDp, heightDp, isLandscape, fontScale) {
        val isNarrow = widthDp < 360.dp
        val isLargeFont = fontScale >= 1.20f
        val isExtraLargeFont = fontScale >= 1.45f

        // Doble columna habilitada en tablets y también en móviles en apaisado (cockpit clínico)
        val isDualColumn = (widthClass != WindowWidthClass.COMPACT && heightClass != WindowHeightClass.COMPACT) ||
            (isLandscape && widthDp >= 560.dp)

        val orbSize = when {
            heightClass == WindowHeightClass.COMPACT -> 100.dp
            isNarrow -> 102.dp
            widthClass == WindowWidthClass.COMPACT -> if (isLargeFont) 124.dp else 120.dp
            else -> 132.dp
        }

        val horizontalPadding = when {
            isNarrow -> 12.dp
            widthClass == WindowWidthClass.COMPACT -> 16.dp
            widthClass == WindowWidthClass.MEDIUM -> 20.dp
            else -> 24.dp
        }

        val chartHeight = when {
            heightClass == WindowHeightClass.COMPACT -> 160.dp
            widthClass == WindowWidthClass.COMPACT -> 220.dp
            else -> 260.dp
        }

        val cardSpacing = if (isNarrow) 12.dp else 16.dp

        ResponsiveDimensions(
            widthClass = widthClass,
            heightClass = heightClass,
            screenWidthDp = widthDp,
            screenHeightDp = heightDp,
            fontScale = fontScale,
            isLandscape = isLandscape,
            isDualColumn = isDualColumn,
            isNarrowPhone = isNarrow,
            isLargeFont = isLargeFont,
            isExtraLargeFont = isExtraLargeFont,
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
