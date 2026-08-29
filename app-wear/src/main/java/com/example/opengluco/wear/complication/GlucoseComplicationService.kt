package com.example.opengluco.wear.complication

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.data.UserSettings
import com.example.opengluco.wear.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class GlucoseComplicationService : ComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("104 →").build(),
                    contentDescription = PlainComplicationText.Builder("Glucosa 104 mg/dL").build()
                ).setTitle(PlainComplicationText.Builder("GLU").build()).build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = 104f,
                    min = 40f,
                    max = 300f,
                    contentDescription = PlainComplicationText.Builder("Glucosa 104 mg/dL").build()
                ).setText(PlainComplicationText.Builder("104").build()).build()
            }
            else -> null
        }
    }

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        val tapIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val prefs = UserPreferencesRepository(this)
        val (settings, history) = runBlocking(Dispatchers.IO) {
            try {
                val s = prefs.userSettingsFlow.first()
                val h = prefs.getHistoricalReadingsList(1, patientId = s.selectedPatientId)
                s to h
            } catch (_: Exception) {
                UserSettings() to emptyList()
            }
        }
        val last = history.lastOrNull()
        val isMmol = settings.unit == GlucoseUnit.MMOL
        val displayVal = last?.getFormattedValue(isMmol) ?: "--"
        val trendSymbol = last?.trendSymbol ?: "→"
        val displayText = "$displayVal $trendSymbol"
        val mgdl = (last?.numericValue ?: 104.0).toFloat()

        val complicationData = when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(displayText).build(),
                    contentDescription = PlainComplicationText.Builder("Glucosa: $displayText").build()
                )
                    .setTitle(PlainComplicationText.Builder("GLU").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = mgdl.coerceIn(40f, 300f),
                    min = 40f,
                    max = 300f,
                    contentDescription = PlainComplicationText.Builder("Glucosa: $displayVal").build()
                )
                    .setText(PlainComplicationText.Builder(displayVal).build())
                    .setTitle(PlainComplicationText.Builder("GLU").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            else -> null
        }

        listener.onComplicationData(complicationData)
    }
}
