package com.example.opengluco.mobile.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// --- PALETA CLÍNICA MODO OSCURO (PURE OLED BLACK) ---
val DarkBackground = Color(0xFF000000)
val DarkSurfaceOrb = Color(0xFF1E232D)
val DarkSurfaceCard = Color(0xFF161A22)
val DarkSurfaceBorder = Color(0xFF2D3748)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)
val DarkMint = Color(0xFF4ADE80)
val DarkArcticCyan = Color(0xFF38BDF8)
val DarkLowCoral = Color(0xFFF87171)
val DarkUrgentCrimson = Color(0xFFEF4444)
val DarkHighAmber = Color(0xFFFBBF24)
val DarkVeryHighOrange = Color(0xFFFB923C)

// --- PALETA CLÍNICA MODO CLARO (CLEAN CLINICAL WHITE) ---
val LightBackground = Color(0xFFF8FAFC)
val LightSurfaceOrb = Color(0xFFFFFFFF)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightSurfaceBorder = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)
val LightTextMuted = Color(0xFF94A3B8)
val LightMint = Color(0xFF16A34A)
val LightArcticCyan = Color(0xFF0284C7)
val LightLowCoral = Color(0xFFDC2626)
val LightUrgentCrimson = Color(0xFFB91C1C)
val LightHighAmber = Color(0xFFD97706)
val LightVeryHighOrange = Color(0xFFEA580C)

// Tokens estáticos legacy para compatibilidad
val ClinicalBackground = DarkBackground
val ClinicalSurfaceOrb = DarkSurfaceOrb
val ClinicalSurfaceCard = DarkSurfaceCard
val ClinicalSurfaceBorder = DarkSurfaceBorder
val ClinicalSurfaceActiveBorder = Color(0x4D4ADE80)

val ClinicalMint = DarkMint
val ClinicalMintLight = LightMint
val ClinicalLowCoral = DarkLowCoral
val ClinicalUrgentCrimson = DarkUrgentCrimson
val ClinicalHighAmber = DarkHighAmber
val ClinicalVeryHighOrange = DarkVeryHighOrange
val ClinicalArcticCyan = DarkArcticCyan

val ClinicalTextPrimary = DarkTextPrimary
val ClinicalTextSecondary = DarkTextSecondary
val ClinicalTextMuted = DarkTextMuted

val GlucoseInRange = DarkMint
val GlucoseLow = DarkLowCoral
val GlucoseUrgentLow = DarkUrgentCrimson
val GlucoseHigh = DarkHighAmber
val GlucoseVeryHigh = DarkVeryHighOrange
val PrimaryBlue = DarkMint
val SecondaryTeal = DarkArcticCyan

// Estructura semántica para acceso dinámico al tema activo
data class ClinicalColorScheme(
    val isDark: Boolean,
    val background: Color,
    val surfaceOrb: Color,
    val surfaceCard: Color,
    val surfaceBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val mint: Color,
    val arcticCyan: Color,
    val lowCoral: Color,
    val urgentCrimson: Color,
    val highAmber: Color,
    val veryHighOrange: Color
)

val DarkClinicalPalette = ClinicalColorScheme(
    isDark = true,
    background = DarkBackground,
    surfaceOrb = DarkSurfaceOrb,
    surfaceCard = DarkSurfaceCard,
    surfaceBorder = DarkSurfaceBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    mint = DarkMint,
    arcticCyan = DarkArcticCyan,
    lowCoral = DarkLowCoral,
    urgentCrimson = DarkUrgentCrimson,
    highAmber = DarkHighAmber,
    veryHighOrange = DarkVeryHighOrange
)

val LightClinicalPalette = ClinicalColorScheme(
    isDark = false,
    background = LightBackground,
    surfaceOrb = LightSurfaceOrb,
    surfaceCard = LightSurfaceCard,
    surfaceBorder = LightSurfaceBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    mint = LightMint,
    arcticCyan = LightArcticCyan,
    lowCoral = LightLowCoral,
    urgentCrimson = LightUrgentCrimson,
    highAmber = LightHighAmber,
    veryHighOrange = LightVeryHighOrange
)

val LocalClinicalColors = staticCompositionLocalOf { DarkClinicalPalette }

object ClinicalTheme {
    val colors: ClinicalColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalClinicalColors.current
}

val DarkColorScheme = darkColorScheme(
    primary = DarkMint,
    onPrimary = Color(0xFF000000),
    primaryContainer = DarkSurfaceOrb,
    onPrimaryContainer = DarkMint,
    secondary = DarkArcticCyan,
    onSecondary = Color(0xFF000000),
    secondaryContainer = DarkSurfaceCard,
    onSecondaryContainer = DarkArcticCyan,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurfaceCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceOrb,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkSurfaceBorder,
    outlineVariant = DarkSurfaceBorder,
    error = DarkUrgentCrimson,
    onError = Color.White
)

val LightColorScheme = lightColorScheme(
    primary = LightMint,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceOrb,
    onPrimaryContainer = LightMint,
    secondary = LightArcticCyan,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceCard,
    onSecondaryContainer = LightArcticCyan,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurfaceCard,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = LightTextSecondary,
    outline = LightSurfaceBorder,
    outlineVariant = LightSurfaceBorder,
    error = LightUrgentCrimson,
    onError = Color.White
)

fun getClinicalStatusColor(valueInMgDl: Double, low: Int = 70, high: Int = 180, isDark: Boolean = true): Color {
    return when {
        valueInMgDl <= 55 -> if (isDark) DarkUrgentCrimson else LightUrgentCrimson
        valueInMgDl < low -> if (isDark) DarkHighAmber else LightHighAmber
        valueInMgDl > 250 -> if (isDark) DarkVeryHighOrange else LightVeryHighOrange
        valueInMgDl > high -> if (isDark) DarkHighAmber else LightHighAmber
        else -> if (isDark) DarkMint else LightMint
    }
}

fun getGlucoseStatusColor(valueInMgDl: Double, low: Int = 70, high: Int = 180, isDark: Boolean = true): Color {
    return getClinicalStatusColor(valueInMgDl, low, high, isDark)
}

fun getGlucoseValueColor(
    valueInMgDl: Double,
    low: Int = 70,
    high: Int = 180,
    alarms: List<com.example.opengluco.core.model.GlucoseAlarm> = emptyList(),
    colors: ClinicalColorScheme = DarkClinicalPalette
): Color {
    val enabledAlarms = alarms.filter { it.enabled }

    // 1. Barreras URGENTES -> Rojo
    val urgentLow = enabledAlarms.filter { it.type == com.example.opengluco.core.model.AlarmType.LOW && it.severity == com.example.opengluco.core.model.AlarmSeverity.URGENT }
        .minByOrNull { it.thresholdMgDl }
    if (urgentLow != null && valueInMgDl <= urgentLow.thresholdMgDl) {
        return colors.urgentCrimson
    }
    val urgentHigh = enabledAlarms.filter { it.type == com.example.opengluco.core.model.AlarmType.HIGH && it.severity == com.example.opengluco.core.model.AlarmSeverity.URGENT }
        .maxByOrNull { it.thresholdMgDl }
    if (urgentHigh != null && valueInMgDl >= urgentHigh.thresholdMgDl) {
        return colors.urgentCrimson
    }
    if (valueInMgDl <= 55.0) {
        return colors.urgentCrimson
    }

    // 2. Barreras de ALERTA (Baja -> Rojo, Alta -> Naranja)
    val alertLow = enabledAlarms.filter { it.type == com.example.opengluco.core.model.AlarmType.LOW && it.severity == com.example.opengluco.core.model.AlarmSeverity.ALERT }
        .minByOrNull { it.thresholdMgDl }
    if (alertLow != null && valueInMgDl <= alertLow.thresholdMgDl) {
        return colors.urgentCrimson
    }
    if (valueInMgDl < low) {
        return colors.urgentCrimson
    }

    val alertHigh = enabledAlarms.filter { it.type == com.example.opengluco.core.model.AlarmType.HIGH && it.severity == com.example.opengluco.core.model.AlarmSeverity.ALERT }
        .maxByOrNull { it.thresholdMgDl }
    if (alertHigh != null && valueInMgDl >= alertHigh.thresholdMgDl) {
        return colors.veryHighOrange
    }
    if (valueInMgDl > high) {
        return colors.veryHighOrange
    }

    // 3. Barreras INFORMATIVAS -> Azul
    val infoLow = enabledAlarms.filter { it.type == com.example.opengluco.core.model.AlarmType.LOW && it.severity == com.example.opengluco.core.model.AlarmSeverity.INFORMATIVE }
        .maxByOrNull { it.thresholdMgDl }
    if (infoLow != null && valueInMgDl <= infoLow.thresholdMgDl) {
        return colors.arcticCyan
    }

    val infoHigh = enabledAlarms.filter { it.type == com.example.opengluco.core.model.AlarmType.HIGH && it.severity == com.example.opengluco.core.model.AlarmSeverity.INFORMATIVE }
        .minByOrNull { it.thresholdMgDl }
    if (infoHigh != null && valueInMgDl >= infoHigh.thresholdMgDl) {
        return colors.arcticCyan
    }

    // 4. En rango
    return colors.mint
}
