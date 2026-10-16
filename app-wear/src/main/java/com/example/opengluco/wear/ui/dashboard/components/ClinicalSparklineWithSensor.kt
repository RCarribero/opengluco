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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.SensorInfo
import com.example.opengluco.wear.ui.theme.ClinicalMint
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceBorder
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceCard
import com.example.opengluco.wear.ui.theme.ClinicalTextPrimary
import com.example.opengluco.wear.ui.theme.ClinicalTextSecondary

@Composable
fun ClinicalSparklineWithSensor(
    history: List<GlucoseMeasurement>,
    sensor: SensorInfo?,
    targetLow: Int = 70,
    targetHigh: Int = 180,
    onSensorBadgeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val daysRemaining = sensor?.getRemainingDays() ?: 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 1. GRÁFICA SPARKLINE CLÍNICA ---
        Box(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ClinicalSurfaceCard)
                .border(1.dp, ClinicalSurfaceBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            val validPoints = remember(history) {
                val list = ArrayList<Double>(history.size)
                for (i in history.indices) {
                    val v = history[i].numericValue
                    if (v > 0.0) list.add(v)
                }
                list
            }

            if (validPoints.size >= 2) {
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
                val curveStroke = remember(density) {
                    Stroke(width = with(density) { 2.dp.toPx() }, cap = StrokeCap.Round)
                }
                val targetDashEffect = remember {
                    PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                }
                val path = remember { Path() }
                val fillPath = remember { Path() }
                val mintGradColors = remember { listOf(ClinicalMint.copy(alpha = 0.25f), Color.Transparent) }
                val pointsCache = remember {
                    object {
                        var lastWidth = 0f
                        var lastHeight = 0f
                        var lastValidPoints: List<Double>? = null
                        var points: List<Offset> = emptyList()
                    }
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Línea objetivo superior (180 mg/dL)
                    val yTargetHigh = height - ((targetHigh - minVal) / valRange * height).coerceIn(0f, height)
                    drawLine(
                        color = Color(0x334ADE80),
                        start = Offset(0f, yTargetHigh),
                        end = Offset(width, yTargetHigh),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = targetDashEffect
                    )

                    // Línea objetivo inferior (70 mg/dL)
                    val yTargetLow = height - ((targetLow - minVal) / valRange * height).coerceIn(0f, height)
                    drawLine(
                        color = Color(0x334ADE80),
                        start = Offset(0f, yTargetLow),
                        end = Offset(width, yTargetLow),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = targetDashEffect
                    )

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

                    path.reset()
                    fillPath.reset()

                    path.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, height)
                    fillPath.lineTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlPointX = (p0.x + p1.x) / 2f
                        path.cubicTo(controlPointX, p0.y, controlPointX, p1.y, p1.x, p1.y)
                        fillPath.cubicTo(controlPointX, p0.y, controlPointX, p1.y, p1.x, p1.y)
                    }

                    fillPath.lineTo(points.last().x, height)
                    fillPath.close()

                    // Sombreado de área suave
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = mintGradColors,
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Trazo continuo de glucosa
                    drawPath(
                        path = path,
                        color = ClinicalMint,
                        style = curveStroke
                    )
                }
            } else {
                Text(
                    text = "Sincronizando...",
                    fontSize = 9.sp,
                    color = ClinicalTextSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // --- 2. MINI BADGE LATERAL DE DÍAS DE SENSOR ---
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ClinicalSurfaceCard)
                .border(1.dp, ClinicalSurfaceBorder, RoundedCornerShape(12.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSensorBadgeClick()
                }
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val sensorText = when {
                    sensor == null -> "--"
                    daysRemaining <= 0 -> "0d"
                    sensor.getLifecycleState() is com.example.opengluco.core.model.SensorLifecycleState.WarmingUp -> "WUp"
                    else -> "${daysRemaining}d"
                }
                val badgeColor = when {
                    sensor == null || daysRemaining <= 0 -> Color(0xFFEF4444)
                    daysRemaining <= 2 -> Color(0xFFF87171)
                    else -> ClinicalTextPrimary
                }
                Text(
                    text = sensorText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Sensor",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = ClinicalTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
