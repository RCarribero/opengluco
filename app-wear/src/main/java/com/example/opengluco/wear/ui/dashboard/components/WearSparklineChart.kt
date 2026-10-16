package com.example.opengluco.wear.ui.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
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

    val validPoints = remember(measurements) {
        val list = ArrayList<Double>(measurements.size)
        for (i in measurements.indices) {
            val v = measurements[i].numericValue
            if (v > 0.0) list.add(v)
        }
        list
    }
    if (validPoints.size < 2) return

    val minVal = remember(validPoints) {
        var min = 70.0
        for (i in validPoints.indices) {
            val v = validPoints[i]
            if (v < min) min = v
        }
        min.coerceAtMost(60.0).toFloat()
    }
    val maxVal = remember(validPoints) {
        var max = 180.0
        for (i in validPoints.indices) {
            val v = validPoints[i]
            if (v > max) max = v
        }
        max.coerceAtLeast(200.0).toFloat()
    }
    val valRange = remember(minVal, maxVal) {
        (maxVal - minVal).coerceAtLeast(40f)
    }

    val density = LocalDensity.current
    val stroke = remember(density) {
        Stroke(width = with(density) { 2.5.dp.toPx() }, cap = StrokeCap.Round)
    }
    val dashEffect = remember {
        PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
    }
    val segmentFill = remember { Path() }
    val segmentStroke = remember { Path() }
    val gradColorsCache = remember { HashMap<Color, List<Color>>(4) }
    val pointsCache = remember {
        object {
            var lastWidth = 0f
            var lastHeight = 0f
            var lastValidPoints: List<Double>? = null
            var points: List<Offset> = emptyList()
        }
    }

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
        if (pointsCache.lastWidth != width || pointsCache.lastHeight != height || pointsCache.lastValidPoints !== validPoints) {
            pointsCache.lastWidth = width
            pointsCache.lastHeight = height
            pointsCache.lastValidPoints = validPoints
            val stepX = width / (validPoints.size - 1)
            val pts = ArrayList<Offset>(validPoints.size)
            for (index in validPoints.indices) {
                val x = index * stepX
                val y = height - ((validPoints[index].toFloat() - minVal) / valRange * height).coerceIn(0f, height)
                pts.add(Offset(x, y))
            }
            pointsCache.points = pts
        }
        val points = pointsCache.points

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

            segmentFill.reset()
            segmentFill.moveTo(p0.x, height)
            segmentFill.lineTo(p0.x, p0.y)
            val controlPointX = (p0.x + p1.x) / 2f
            segmentFill.cubicTo(controlPointX, p0.y, controlPointX, p1.y, p1.x, p1.y)
            segmentFill.lineTo(p1.x, height)
            segmentFill.close()

            val gradColors = gradColorsCache.getOrPut(segmentColor) {
                listOf(segmentColor.copy(alpha = 0.28f), Color.Transparent)
            }
            val fillBrush = Brush.verticalGradient(
                colors = gradColors,
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

            segmentStroke.reset()
            segmentStroke.moveTo(p0.x, p0.y)
            val controlPointX = (p0.x + p1.x) / 2f
            segmentStroke.cubicTo(controlPointX, p0.y, controlPointX, p1.y, p1.x, p1.y)

            drawPath(
                path = segmentStroke,
                color = segmentColor,
                style = stroke
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
