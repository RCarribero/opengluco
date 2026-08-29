package com.example.opengluco.wear.ui.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.wear.ui.theme.WearSurfaceVariant
import com.example.opengluco.wear.ui.theme.getGlucoseStatusColor

@Composable
fun WearGlucoseGauge(
    measurement: GlucoseMeasurement?,
    targetLow: Int = 70,
    targetHigh: Int = 180,
    unit: GlucoseUnit = GlucoseUnit.MGDL,
    modifier: Modifier = Modifier
) {
    val mgdl = measurement?.numericValue ?: 0.0
    val statusColor = if (measurement != null) getGlucoseStatusColor(mgdl, targetLow, targetHigh) else WearSurfaceVariant
    val displayValue = measurement?.getFormattedValue(isMmol = (unit == GlucoseUnit.MMOL)) ?: "--"
    val trendSymbol = measurement?.trendSymbol ?: ""

    // Proporción en el arco (rango visual de 40 a 300 mg/dL)
    val sweepProgress = if (mgdl > 0) {
        ((mgdl - 40.0) / (300.0 - 40.0)).coerceIn(0.05, 1.0).toFloat()
    } else 0f

    Box(
        modifier = modifier.size(145.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val strokeWidth = 8.dp.toPx()
            val startAngle = 140f
            val totalSweep = 260f

            // Fondo del arco
            drawArc(
                color = Color(0xFF263238),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Arco activo de glucosa
            if (sweepProgress > 0f) {
                drawArc(
                    color = statusColor,
                    startAngle = startAngle,
                    sweepAngle = totalSweep * sweepProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Lectura central de glucosa
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = displayValue,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = statusColor,
                    textAlign = TextAlign.Center
                )
                if (trendSymbol.isNotEmpty()) {
                    Text(
                        text = " $trendSymbol",
                        fontSize = 22.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            Text(
                text = unit.label,
                fontSize = 11.sp,
                color = com.example.opengluco.wear.ui.theme.ClinicalTextSecondary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
