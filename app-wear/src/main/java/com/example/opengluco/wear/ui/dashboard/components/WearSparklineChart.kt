package com.example.opengluco.wear.ui.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.wear.ui.theme.GlucoseInRange
import com.example.opengluco.wear.ui.theme.WearPrimary

@Composable
fun WearSparklineChart(
    measurements: List<GlucoseMeasurement>,
    targetLow: Int = 70,
    targetHigh: Int = 180,
    modifier: Modifier = Modifier
) {
    if (measurements.isEmpty()) return

    val validPoints = measurements.map { it.numericValue }.filter { it > 0 }
    if (validPoints.size < 2) return

    val minVal = (validPoints.minOrNull() ?: 70.0).coerceAtMost(60.0).toFloat()
    val maxVal = (validPoints.maxOrNull() ?: 180.0).coerceAtLeast(200.0).toFloat()
    val valRange = (maxVal - minVal).coerceAtLeast(40f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        val width = size.width
        val height = size.height

        // Franja de rango objetivo (Líneas discontinuas superior e inferior)
        val yHigh = height - ((targetHigh - minVal) / valRange * height).coerceIn(0f, height)
        val yLow = height - ((targetLow - minVal) / valRange * height).coerceIn(0f, height)

        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

        // Línea 180 mg/dL
        drawLine(
            color = Color(0x3300E676),
            start = Offset(0f, yHigh),
            end = Offset(width, yHigh),
            strokeWidth = 1.5f,
            pathEffect = dashEffect
        )

        // Línea 70 mg/dL
        drawLine(
            color = Color(0x3300E676),
            start = Offset(0f, yLow),
            end = Offset(width, yLow),
            strokeWidth = 1.5f,
            pathEffect = dashEffect
        )

        // Trazado de la curva de glucosa con Bézier suave
        val stepX = width / (validPoints.size - 1)
        val path = Path()
        val fillPath = Path()

        val points = validPoints.mapIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value.toFloat() - minVal) / valRange * height).coerceIn(0f, height)
            Offset(x, y)
        }

        fun getLevelColor(valMg: Double): Color = when {
            valMg < targetLow -> com.example.opengluco.wear.ui.theme.ClinicalUrgentCrimson
            valMg > targetHigh -> com.example.opengluco.wear.ui.theme.ClinicalVeryHighOrange
            else -> com.example.opengluco.wear.ui.theme.ClinicalMint
        }

        // Sombreado de area bajo la curva con cortes fijos (sin fusion de color)
        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val v0 = validPoints[i]
            val v1 = validPoints[i + 1]

            val midVal = (v0 + v1) / 2.0
            val segmentColor = getLevelColor(midVal)

            val segmentFill = Path().apply {
                moveTo(p0.x, height)
                lineTo(p0.x, p0.y)
                val controlPointX = (p0.x + p1.x) / 2f
                cubicTo(controlPointX, p0.y, controlPointX, p1.y, p1.x, p1.y)
                lineTo(p1.x, height)
                close()
            }

            val fillBrush = Brush.verticalGradient(
                colors = listOf(segmentColor.copy(alpha = 0.28f), Color.Transparent),
                startY = minOf(p0.y, p1.y),
                endY = height
            )

            drawPath(
                path = segmentFill,
                brush = fillBrush
            )
        }

        // Trazo de linea con cortes fijos y colores solidos
        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val v0 = validPoints[i]
            val v1 = validPoints[i + 1]

            val midVal = (v0 + v1) / 2.0
            val segmentColor = getLevelColor(midVal)

            val segmentStroke = Path().apply {
                moveTo(p0.x, p0.y)
                val controlPointX = (p0.x + p1.x) / 2f
                cubicTo(controlPointX, p0.y, controlPointX, p1.y, p1.x, p1.y)
            }

            drawPath(
                path = segmentStroke,
                brush = androidx.compose.ui.graphics.SolidColor(segmentColor),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Punto de la ultima lectura
        val lastPoint = points.last()
        val lastColor = getLevelColor(validPoints.last())
        drawCircle(
            color = Color.White,
            radius = 3.5.dp.toPx(),
            center = lastPoint
        )
        drawCircle(
            color = lastColor,
            radius = 2.dp.toPx(),
            center = lastPoint
        )
    }
}
