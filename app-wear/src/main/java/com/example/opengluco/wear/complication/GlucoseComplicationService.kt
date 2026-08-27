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
import com.example.opengluco.wear.MainActivity

class GlucoseComplicationService : ComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("140 →").build(),
                    contentDescription = PlainComplicationText.Builder("Glucosa 140 mg/dL").build()
                ).setTitle(PlainComplicationText.Builder("GLU").build()).build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = 140f,
                    min = 40f,
                    max = 300f,
                    contentDescription = PlainComplicationText.Builder("Glucosa 140 mg/dL").build()
                ).setText(PlainComplicationText.Builder("140").build()).build()
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

        val lastGlucose = 140.0
        val displayVal = String.format("%.0f", lastGlucose)
        val displayText = "$displayVal →"

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
                    value = lastGlucose.toFloat(),
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
