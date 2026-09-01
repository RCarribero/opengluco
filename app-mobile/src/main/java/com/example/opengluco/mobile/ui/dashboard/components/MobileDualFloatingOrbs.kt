package com.example.opengluco.mobile.ui.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.GlucoseAlarm
import com.example.opengluco.mobile.ui.theme.ClinicalTheme
import com.example.opengluco.mobile.ui.theme.getClinicalStatusColor
import com.example.opengluco.mobile.ui.theme.getGlucoseValueColor

@Composable
fun MobileDualFloatingOrbs(
    measurement: GlucoseMeasurement?,
    unit: GlucoseUnit = GlucoseUnit.MGDL,
    targetLow: Int = 70,
    targetHigh: Int = 180,
    alarms: List<GlucoseAlarm> = emptyList(),
    onGlucoseOrbClick: () -> Unit = {},
    onTrendOrbClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val colors = ClinicalTheme.colors
    val mgdl = measurement?.numericValue ?: 0.0
    val statusColor = if (measurement != null) getGlucoseValueColor(mgdl, targetLow, targetHigh, alarms, colors) else colors.textMuted

    val formattedVal = measurement?.getFormattedValue(isMmol = unit == GlucoseUnit.MMOL) ?: "--"
    val glucoseDesc = "Nivel de glucosa actual: $formattedVal ${unit.label}"
    val trendDesc = "Tendencia: ${measurement?.trendText ?: "Estable"}, direccion ${measurement?.trendSymbol ?: "→"}"

    val responsive = ClinicalTheme.responsive
    val orbSize = responsive.orbSize
    val valFontSize = if (responsive.isNarrowPhone) 28.sp else if (orbSize > 125.dp) 38.sp else 34.sp
    val symbolFontSize = if (responsive.isNarrowPhone) 28.sp else if (orbSize > 125.dp) 38.sp else 34.sp
    val orbSpacing = if (responsive.isNarrowPhone) 10.dp else 16.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = responsive.horizontalPadding, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- ESFERA IZQUIERDA: NIVEL DE GLUCOSA (PASIVA / SIN ACCIÓN AL PULSAR) ---
        Box(
            modifier = Modifier
                .size(orbSize)
                .clip(CircleShape)
                .background(colors.surfaceOrb)
                .border(1.dp, colors.surfaceBorder, CircleShape)
                .semantics {
                    contentDescription = glucoseDesc
                },
            contentAlignment = Alignment.Center
        ) {
            // Anillo sutil de estado clínico
            Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                drawArc(
                    color = statusColor,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = measurement?.getFormattedValue(isMmol = unit == GlucoseUnit.MMOL) ?: "--",
                    fontSize = valFontSize,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = unit.label,
                    fontSize = if (responsive.isNarrowPhone) 10.5.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.size(orbSpacing))

        // --- ESFERA DERECHA: TENDENCIA CLÍNICA ---
        Box(
            modifier = Modifier
                .size(orbSize)
                .clip(CircleShape)
                .background(colors.surfaceOrb)
                .border(1.dp, colors.surfaceBorder, CircleShape)
                .semantics {
                    contentDescription = trendDesc
                }
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTrendOrbClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = measurement?.trendSymbol ?: "→",
                    fontSize = symbolFontSize,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(if (responsive.isNarrowPhone) 4.dp else 6.dp))
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = if (colors.isDark) 0.2f else 0.15f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = if (responsive.isNarrowPhone) 7.dp else 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = measurement?.trendText ?: "Estable",
                        fontSize = if (responsive.isNarrowPhone) 9.5.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
