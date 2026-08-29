package com.example.opengluco.wear.ui.dashboard.components

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceBorder
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceOrb
import com.example.opengluco.wear.ui.theme.ClinicalTextPrimary
import com.example.opengluco.wear.ui.theme.ClinicalTextSecondary
import com.example.opengluco.wear.ui.theme.getClinicalStatusColor

@Composable
fun DualFloatingOrbs(
    measurement: GlucoseMeasurement?,
    unit: GlucoseUnit = GlucoseUnit.MGDL,
    targetLow: Int = 70,
    targetHigh: Int = 180,
    onGlucoseOrbClick: () -> Unit = {},
    onTrendOrbClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val mgdl = measurement?.numericValue ?: 0.0
    val statusColor = if (measurement != null) getClinicalStatusColor(mgdl, targetLow, targetHigh) else Color(0xFF64748B)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- ESFERA IZQUIERDA: NIVEL DE GLUCOSA ---
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(ClinicalSurfaceOrb)
                .border(1.dp, ClinicalSurfaceBorder, CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onGlucoseOrbClick()
                },
            contentAlignment = Alignment.Center
        ) {
            // Anillo sutil de estado clínico
            Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                drawArc(
                    color = statusColor,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = measurement?.getFormattedValue(isMmol = unit == GlucoseUnit.MMOL) ?: "--",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClinicalTextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = unit.label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = ClinicalTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.size(10.dp))

        // --- ESFERA DERECHA: TENDENCIA CLÍNICA ---
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(ClinicalSurfaceOrb)
                .border(1.dp, ClinicalSurfaceBorder, CircleShape)
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
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClinicalTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = measurement?.trendText ?: "Estable",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
