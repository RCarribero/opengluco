package com.example.opengluco.mobile.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.example.opengluco.core.model.GlucoseMeasurement
import java.text.SimpleDateFormat
import java.util.Locale

object WidgetChartRenderer {

    // Tokens clínicos del proyecto
    private const val COLOR_MINT = 0xFF4ADE80.toInt()
    private const val COLOR_LOW = 0xFFF87171.toInt()
    private const val COLOR_URGENT_LOW = 0xFFEF4444.toInt()
    private const val COLOR_HIGH = 0xFFFBBF24.toInt()
    private const val COLOR_VERY_HIGH = 0xFFFB923C.toInt()
    private const val COLOR_GRID = 0x33475569
    private const val COLOR_GRID_TEXT = 0xFF64748B.toInt()
    private const val COLOR_TARGET_ZONE = 0x154ADE80

    fun renderSparkline(
        readings: List<GlucoseMeasurement>,
        width: Int = 400,
        height: Int = 180,
        targetLowMgDl: Int = 70,
        targetHighMgDl: Int = 180
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (readings.isEmpty()) {
            val emptyPaint = Paint().apply {
                color = COLOR_GRID_TEXT
                textSize = 24f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Sin datos recientes", width / 2f, height / 2f + 8f, emptyPaint)
            return bitmap
        }

        val paddingLeft = 10f
        val paddingRight = 20f
        val paddingTop = 16f
        val paddingBottom = 16f

        val plotWidth = width - paddingLeft - paddingRight
        val plotHeight = height - paddingTop - paddingBottom

        val minGlucose = 40.0
        val maxGlucose = 300.0

        fun yForValue(mgDl: Double): Float {
            val clamped = mgDl.coerceIn(minGlucose, maxGlucose)
            val fraction = (clamped - minGlucose) / (maxGlucose - minGlucose)
            return paddingTop + plotHeight * (1f - fraction.toFloat())
        }

        // 1. Dibujar Zona de Rango Objetivo (70 - 180)
        val targetZonePaint = Paint().apply {
            color = COLOR_TARGET_ZONE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val yTargetHigh = yForValue(targetHighMgDl.toDouble())
        val yTargetLow = yForValue(targetLowMgDl.toDouble())
        canvas.drawRect(
            paddingLeft,
            yTargetHigh,
            width - paddingRight,
            yTargetLow,
            targetZonePaint
        )

        // 2. Líneas Guía Discontinuas de Rango
        val gridLinePaint = Paint().apply {
            color = COLOR_GRID
            strokeWidth = 2f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
            isAntiAlias = true
        }
        canvas.drawLine(paddingLeft, yTargetHigh, width - paddingRight, yTargetHigh, gridLinePaint)
        canvas.drawLine(paddingLeft, yTargetLow, width - paddingRight, yTargetLow, gridLinePaint)

        // 3. Trazar la Curva Continua de Glucosa con Filtro Fisiológico y Catmull-Rom
        val consolidated = com.example.opengluco.core.model.CgmCurveSmoother.consolidateTemporalBuckets(readings)
        val smoothed = com.example.opengluco.core.model.CgmCurveSmoother.smoothMeasurements(consolidated)
        val sortedReadings = smoothed.takeLast(36) // Últimas 3-6 horas

        if (sortedReadings.size >= 2) {
            val stepX = plotWidth / (sortedReadings.size - 1).toFloat()

            val points = sortedReadings.mapIndexed { index, m ->
                val x = paddingLeft + index * stepX
                val y = yForValue(m.numericValue)
                Pair(x, y)
            }

            val splineSegments = com.example.opengluco.core.model.CgmCurveSmoother.computeCatmullRomSpline(points)

            val linePath = Path()
            val fillPath = Path()

            fillPath.moveTo(points.first().first, height - paddingBottom)
            fillPath.lineTo(points.first().first, points.first().second)
            linePath.moveTo(points.first().first, points.first().second)

            for (seg in splineSegments) {
                linePath.cubicTo(seg.cp1X, seg.cp1Y, seg.cp2X, seg.cp2Y, seg.endX, seg.endY)
                fillPath.cubicTo(seg.cp1X, seg.cp1Y, seg.cp2X, seg.cp2Y, seg.endX, seg.endY)
            }

            val lastPoint = points.last()
            fillPath.lineTo(lastPoint.first, height - paddingBottom)
            fillPath.close()

            // Relleno suave con gradiente vertical
            val fillPaint = Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true
                shader = LinearGradient(
                    0f, paddingTop,
                    0f, height.toFloat(),
                    COLOR_MINT and 0x30FFFFFF,
                    0x00000000,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawPath(fillPath, fillPaint)

            // Línea de la curva
            val latestValue = sortedReadings.last().numericValue
            val curveColor = when {
                latestValue <= 55 -> COLOR_URGENT_LOW
                latestValue < targetLowMgDl -> COLOR_LOW
                latestValue > 249 -> COLOR_VERY_HIGH
                latestValue > targetHighMgDl -> COLOR_HIGH
                else -> COLOR_MINT
            }

            val curvePaint = Paint().apply {
                color = curveColor
                strokeWidth = 4.5f
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }
            canvas.drawPath(linePath, curvePaint)

            // 4. Aura y Punto en la Última Medición
            val lastX = lastPoint.first
            val lastY = yForValue(latestValue)
            val auraPaint = Paint().apply {
                color = curveColor and 0x40FFFFFF
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(lastX, lastY, 11f, auraPaint)

            val pointPaint = Paint().apply {
                color = curveColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(lastX, lastY, 5.5f, pointPaint)

            val innerDotPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(lastX, lastY, 2.5f, innerDotPaint)
        }

        return bitmap
    }
}
