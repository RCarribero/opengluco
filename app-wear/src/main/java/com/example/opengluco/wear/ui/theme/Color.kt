package com.example.opengluco.wear.ui.theme

import androidx.compose.ui.graphics.Color

// Fondos y Superficies OLED
val ClinicalBackground = Color(0xFF000000)
val ClinicalSurfaceOrb = Color(0xFF1E232D)
val ClinicalSurfaceCard = Color(0xFF161A22)
val ClinicalSurfaceBorder = Color(0xFF2D3748)

// Estados Clínicos de Glucosa (Tonos Pastel Muted)
val ClinicalMint = Color(0xFF4ADE80)           // En rango (70 - 180 mg/dL)
val ClinicalLowCoral = Color(0xFFF87171)       // Bajo (56 - 69 mg/dL)
val ClinicalUrgentCrimson = Color(0xFFEF4444)  // Urgente bajo (<= 55 mg/dL)
val ClinicalHighAmber = Color(0xFFFBBF24)      // Alto (181 - 249 mg/dL)
val ClinicalVeryHighOrange = Color(0xFFFB923C) // Muy alto (>= 250 mg/dL)
val ClinicalArcticCyan = Color(0xFF38BDF8)     // Acento médico secundario

// Tipografía
val ClinicalTextPrimary = Color(0xFFFFFFFF)
val ClinicalTextSecondary = Color(0xFF94A3B8)
val ClinicalTextMuted = Color(0xFF64748B)

// Tokens y alias heredados para compatibilidad total
val WearPrimary = ClinicalMint
val WearOnPrimary = Color(0xFF003919)
val WearSecondary = ClinicalArcticCyan
val WearDarkBackground = ClinicalBackground
val WearSurface = ClinicalSurfaceOrb
val WearSurfaceVariant = ClinicalSurfaceCard
val WearTextPrimary = ClinicalTextPrimary

val GlucoseInRange = ClinicalMint
val GlucoseLow = ClinicalLowCoral
val GlucoseUrgentLow = ClinicalUrgentCrimson
val GlucoseHigh = ClinicalHighAmber
val GlucoseVeryHigh = ClinicalVeryHighOrange

fun getClinicalStatusColor(valueInMgDl: Double, low: Int = 70, high: Int = 180): Color {
    return when {
        valueInMgDl <= 55 -> ClinicalUrgentCrimson
        valueInMgDl < low -> ClinicalLowCoral
        valueInMgDl > 250 -> ClinicalVeryHighOrange
        valueInMgDl > high -> ClinicalHighAmber
        else -> ClinicalMint
    }
}

fun getGlucoseStatusColor(valueInMgDl: Double, low: Int = 70, high: Int = 180): Color {
    return getClinicalStatusColor(valueInMgDl, low, high)
}
