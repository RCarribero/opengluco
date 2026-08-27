package com.example.opengluco.core.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.opengluco.core.model.GlucoseMeasurement
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HealthDataExporter {

    fun generateCsv(
        readings: List<GlucoseMeasurement>,
        unit: GlucoseUnit = GlucoseUnit.MGDL,
        patientName: String = "Paciente"
    ): String {
        val sb = StringBuilder()
        sb.append("Timestamp,Glucosa (${unit.label}),Tendencia,Estado Clinico\n")

        val sorted = readings.sortedBy { it.timestamp ?: "" }
        for (r in sorted) {
            val ts = r.timestamp ?: r.factoryTimestamp ?: ""
            val valStr = if (unit == GlucoseUnit.MMOL) {
                String.format(Locale.US, "%.1f", r.numericValue / 18.0182)
            } else {
                r.numericValue.toInt().toString()
            }
            val status = when {
                r.numericValue < 70 -> "Hipoglucemia (Bajo)"
                r.numericValue > 180 -> "Hiperglucemia (Alto)"
                else -> "En Rango"
            }
            sb.append("\"$ts\",$valStr,\"${r.trendText}\",\"$status\"\n")
        }
        return sb.toString()
    }

    fun shareCsv(
        context: Context,
        readings: List<GlucoseMeasurement>,
        unit: GlucoseUnit = GlucoseUnit.MGDL,
        patientName: String = "Paciente"
    ) {
        val safeName = patientName.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "Paciente" }
        try {
            val csvData = generateCsv(readings, unit, patientName)
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "OpenGluco_${safeName}_$timeStamp.csv")
            file.writeText(csvData)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Historial de Glucosa - $patientName")
                putExtra(Intent.EXTRA_STREAM, uri as android.os.Parcelable)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Exportar Historial ($patientName) a CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Fallback plain share
            val csvData = generateCsv(readings, unit, patientName)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Historial de Glucosa - $patientName")
                putExtra(Intent.EXTRA_TEXT, csvData)
            }
            val chooser = Intent.createChooser(intent, "Exportar Historial ($patientName)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}
