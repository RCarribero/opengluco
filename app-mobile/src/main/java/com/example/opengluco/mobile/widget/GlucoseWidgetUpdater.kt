package com.example.opengluco.mobile.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.mobile.MainActivity
import com.example.opengluco.mobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GlucoseWidgetUpdater {

    // Tokens clínicos
    private const val COLOR_MINT = 0xFF4ADE80.toInt()
    private const val COLOR_LOW = 0xFFF87171.toInt()
    private const val COLOR_URGENT_LOW = 0xFFEF4444.toInt()
    private const val COLOR_HIGH = 0xFFFBBF24.toInt()
    private const val COLOR_VERY_HIGH = 0xFFFB923C.toInt()

    fun updateAllWidgets(
        context: Context,
        latestMeasurement: GlucoseMeasurement? = null,
        history: List<GlucoseMeasurement>? = null,
        patientName: String? = null
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val compactComponent = ComponentName(context, GlucoseCompactWidgetProvider::class.java)
        val chartComponent = ComponentName(context, GlucoseChartWidgetProvider::class.java)

        val compactIds = appWidgetManager.getAppWidgetIds(compactComponent)
        val chartIds = appWidgetManager.getAppWidgetIds(chartComponent)

        if (compactIds.isEmpty() && chartIds.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            val prefs = UserPreferencesRepository(context)
            val settings = prefs.userSettingsFlow.firstOrNull()
            val unit = settings?.unit ?: GlucoseUnit.MGDL
            val targetLow = settings?.lowThreshold ?: 70
            val targetHigh = settings?.highThreshold ?: 180

            val effectiveReadings = history ?: prefs.getHistoricalReadings(90).first()
            val effectiveLatest = latestMeasurement ?: effectiveReadings.lastOrNull()

            val displayPatient = patientName ?: "OpenGluco"

            // 1. Actualizar Widgets Compactos
            if (compactIds.isNotEmpty()) {
                val views = buildCompactRemoteViews(
                    context = context,
                    measurement = effectiveLatest,
                    unit = unit,
                    targetLow = targetLow,
                    targetHigh = targetHigh,
                    patientName = displayPatient
                )
                appWidgetManager.updateAppWidget(compactIds, views)
            }

            // 2. Actualizar Widgets con Gráfica
            if (chartIds.isNotEmpty()) {
                val views = buildChartRemoteViews(
                    context = context,
                    measurement = effectiveLatest,
                    history = effectiveReadings,
                    unit = unit,
                    targetLow = targetLow,
                    targetHigh = targetHigh,
                    patientName = displayPatient
                )
                appWidgetManager.updateAppWidget(chartIds, views)
            }
        }
    }

    private fun buildCompactRemoteViews(
        context: Context,
        measurement: GlucoseMeasurement?,
        unit: GlucoseUnit,
        targetLow: Int,
        targetHigh: Int,
        patientName: String
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_glucose_compact)

        // PendingIntent para abrir MainActivity al pulsar
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_compact_root, pendingIntent)

        views.setTextViewText(R.id.widget_compact_patient, patientName)

        if (measurement != null && measurement.numericValue > 0.0) {
            val mgDl = measurement.numericValue
            val displayValue = if (unit == GlucoseUnit.MMOL) {
                String.format(Locale.US, "%.1f", mgDl / 18.0)
            } else {
                mgDl.toInt().toString()
            }

            val statusText = when {
                mgDl <= 55 -> "Urgente Bajo"
                mgDl < targetLow -> "Bajo"
                mgDl > 249 -> "Muy Alto"
                mgDl > targetHigh -> "Alto"
                else -> "En rango"
            }

            val clinicalColor = when {
                mgDl <= 55 -> COLOR_URGENT_LOW
                mgDl < targetLow -> COLOR_LOW
                mgDl > 249 -> COLOR_VERY_HIGH
                mgDl > targetHigh -> COLOR_HIGH
                else -> COLOR_MINT
            }

            val timeFormatted = formatTime24h(measurement.timestamp)

            views.setTextViewText(R.id.widget_compact_value, displayValue)
            views.setTextColor(R.id.widget_compact_value, clinicalColor)

            views.setTextViewText(R.id.widget_compact_arrow, measurement.trendSymbol)
            views.setTextColor(R.id.widget_compact_arrow, clinicalColor)

            views.setTextViewText(R.id.widget_compact_unit, if (unit == GlucoseUnit.MMOL) "mmol/L" else "mg/dL")
            views.setTextViewText(R.id.widget_compact_status, statusText)
            views.setTextColor(R.id.widget_compact_status, clinicalColor)

            views.setTextViewText(R.id.widget_compact_time, timeFormatted)
        } else {
            views.setTextViewText(R.id.widget_compact_value, "---")
            views.setTextViewText(R.id.widget_compact_arrow, "→")
            views.setTextViewText(R.id.widget_compact_status, "Esperando datos")
            views.setTextViewText(R.id.widget_compact_time, "--:--")
        }

        return views
    }

    private fun buildChartRemoteViews(
        context: Context,
        measurement: GlucoseMeasurement?,
        history: List<GlucoseMeasurement>,
        unit: GlucoseUnit,
        targetLow: Int,
        targetHigh: Int,
        patientName: String
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_glucose_chart)

        // PendingIntent para abrir MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            102,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_chart_root, pendingIntent)

        views.setTextViewText(R.id.widget_chart_patient, patientName)

        if (measurement != null && measurement.numericValue > 0.0) {
            val mgDl = measurement.numericValue
            val displayValue = if (unit == GlucoseUnit.MMOL) {
                String.format(Locale.US, "%.1f", mgDl / 18.0)
            } else {
                mgDl.toInt().toString()
            }

            val statusText = when {
                mgDl <= 55 -> "Urgente Bajo"
                mgDl < targetLow -> "Bajo"
                mgDl > 249 -> "Muy Alto"
                mgDl > targetHigh -> "Alto"
                else -> "En rango"
            }

            val clinicalColor = when {
                mgDl <= 55 -> COLOR_URGENT_LOW
                mgDl < targetLow -> COLOR_LOW
                mgDl > 249 -> COLOR_VERY_HIGH
                mgDl > targetHigh -> COLOR_HIGH
                else -> COLOR_MINT
            }

            val timeFormatted = formatTime24h(measurement.timestamp)

            views.setTextViewText(R.id.widget_chart_value, displayValue)
            views.setTextColor(R.id.widget_chart_value, clinicalColor)

            views.setTextViewText(R.id.widget_chart_arrow, measurement.trendSymbol)
            views.setTextColor(R.id.widget_chart_arrow, clinicalColor)

            views.setTextViewText(R.id.widget_chart_unit, if (unit == GlucoseUnit.MMOL) "mmol/L" else "mg/dL")

            views.setTextViewText(R.id.widget_chart_status_badge, statusText)
            views.setTextColor(R.id.widget_chart_status_badge, clinicalColor)

            views.setTextViewText(R.id.widget_chart_time, timeFormatted)

            // Renderizar gráfica Sparkline en Bitmap
            val chartBitmap = WidgetChartRenderer.renderSparkline(
                readings = history,
                width = 440,
                height = 190,
                targetLowMgDl = targetLow,
                targetHighMgDl = targetHigh
            )
            views.setImageViewBitmap(R.id.widget_chart_image, chartBitmap)
        } else {
            views.setTextViewText(R.id.widget_chart_value, "---")
            views.setTextViewText(R.id.widget_chart_arrow, "→")
            views.setTextViewText(R.id.widget_chart_status_badge, "Sin datos")
            views.setTextViewText(R.id.widget_chart_time, "--:--")
        }

        return views
    }

    private fun formatTime24h(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return "--:--"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = inputFormat.parse(timestamp.take(19)) ?: Date()
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outputFormat.format(date)
        } catch (_: Exception) {
            timestamp.takeLast(5)
        }
    }
}
