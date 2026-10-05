package com.example.opengluco.core.data

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.TirCategory
import java.io.File
import java.io.FileOutputStream
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
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "OpenGluco_${safeName}_$timeStamp.csv")
            file.bufferedWriter().use { writer ->
                writer.write("Timestamp,Glucosa (${unit.label}),Tendencia,Estado Clinico\n")
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
                    writer.write("\"$ts\",$valStr,\"${r.trendText}\",\"$status\"\n")
                }
            }

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

    fun shareClinicalReportPdf(
        context: Context,
        readings: List<GlucoseMeasurement>,
        patientName: String,
        periodDays: Int,
        unit: GlucoseUnit = GlucoseUnit.MGDL,
        targetLow: Double = 70.0,
        targetHigh: Double = 180.0
    ) {
        val safeName = patientName.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "Paciente" }
        val reportReadings = ClinicalReportsCalculator.filterReadingsByPeriod(readings, periodDays)
        val tir = ClinicalReportsCalculator.calculateTimeInRange(reportReadings, periodDays)
        val average = ClinicalReportsCalculator.calculateAverageGlucose(reportReadings, periodDays)
        val patterns = ClinicalReportsCalculator.calculateDailyPatterns(reportReadings, periodDays, targetLow, targetHigh)
        val gmi = ClinicalReportsCalculator.calculateEstimatedA1c(reportReadings, periodDays)
        val quality = ClinicalReportsCalculator.calculateDataQuality(reportReadings, periodDays)
        val document = PdfDocument()

        try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            fun drawText(text: String, x: Float, y: Float, size: Float = 10f, bold: Boolean = false, color: Int = android.graphics.Color.rgb(35, 42, 52)) {
                paint.color = color
                paint.textSize = size
                paint.typeface = if (bold) android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD) else android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                canvas.drawText(text, x, y, paint)
            }

            fun drawWrapped(text: String, x: Float, startY: Float, maxChars: Int, size: Float = 8.5f, lineHeight: Float = 12f, color: Int = android.graphics.Color.DKGRAY): Float {
                val words = text.split(Regex("\\s+"))
                var line = StringBuilder()
                var y = startY
                for (word in words) {
                    val candidate = if (line.isEmpty()) word else "${line} $word"
                    if (candidate.length > maxChars && line.isNotEmpty()) {
                        drawText(line.toString(), x, y, size, color = color)
                        y += lineHeight
                        line = StringBuilder(word)
                    } else {
                        line = StringBuilder(candidate)
                    }
                }
                if (line.isNotEmpty()) {
                    drawText(line.toString(), x, y, size, color = color)
                    y += lineHeight
                }
                return y
            }

            val ink = android.graphics.Color.rgb(28, 36, 48)
            val secondary = android.graphics.Color.rgb(92, 102, 115)
            val mint = android.graphics.Color.rgb(34, 153, 91)
            val amber = android.graphics.Color.rgb(205, 142, 19)
            val coral = android.graphics.Color.rgb(211, 79, 79)
            val pageWidth = pageInfo.pageWidth.toFloat()
            val left = 38f

            drawText("OpenGluco · Informe de glucosa", left, 43f, 18f, bold = true, color = ink)
            drawText(patientName.ifBlank { "Paciente" }, left, 67f, 12f, bold = true)
            val generatedAt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            drawText("Periodo: últimos $periodDays días    Generado: $generatedAt", left, 84f, 9f, color = secondary)
            paint.color = android.graphics.Color.rgb(220, 225, 231)
            paint.strokeWidth = 1f
            canvas.drawLine(left, 98f, pageWidth - left, 98f, paint)

            val cadenceText = quality.samplingIntervalMinutes?.let { "$it min" } ?: "no estimable"
            val coverageText = quality.coveragePercentage?.let { String.format(Locale.US, "%.1f%%", it) } ?: "no estimable"
            drawText("Calidad de datos", left, 119f, 11f, bold = true, color = ink)
            drawText("Lecturas: ${quality.totalReadings}    Dias con datos: ${quality.daysWithReadings}    Cobertura estimada: $coverageText", left, 137f, 9f, color = secondary)
            drawText("Cadencia detectada: $cadenceText    Huecos largos: ${quality.gapCount}    Hueco maximo: ${quality.longestGapMinutes} min", left, 152f, 9f, color = secondary)
            drawText("Cobertura calculada entre la primera y la ultima lectura del periodo.", left, 167f, 8f, color = secondary)

            val columns = listOf(left, 175f, 310f, 445f)
            val metricLabels = listOf("Tiempo en rango", "Media", "GMI estimado", "Variabilidad CV")
            val metricValues = listOf(
                "${tir.inRangePercent.toInt()}%",
                formatGlucoseValue(average.overallAverageMgDl, unit),
                String.format(Locale.US, "%.1f%%", gmi.gmiPercent),
                String.format(Locale.US, "%.1f%%", patterns.coefficientOfVariation)
            )
            for (i in columns.indices) {
                drawText(metricLabels[i], columns[i], 199f, 8.5f, bold = true, color = secondary)
                drawText(metricValues[i], columns[i], 223f, 18f, bold = true, color = if (i == 0) mint else ink)
            }
            drawText("GMI calculado a partir de la glucosa media; no equivale a HbA1c de laboratorio.", left, 240f, 8f, color = secondary)

            drawText("Perfil diario de glucosa (AGP)", left, 274f, 12f, bold = true, color = ink)
            drawText("Mediana por hora y dispersión P10–P90 (${unit.label})", left, 290f, 8.5f, color = secondary)
            val chartLeft = 88f
            val chartRight = pageWidth - left
            val chartTop = 309f
            val chartBottom = 458f
            val chartHeight = chartBottom - chartTop
            val chartMin = 40.0
            val chartMax = 350.0
            fun yFor(value: Double): Float = chartBottom - (((value.coerceIn(chartMin, chartMax) - chartMin) / (chartMax - chartMin)) * chartHeight).toFloat()

            if (tir.totalReadings == 0) {
                drawText("Sin lecturas en el periodo seleccionado.", chartLeft, chartTop + 70f, 10f, color = secondary)
            } else {
                listOf(targetLow, targetHigh).forEach { target ->
                    val y = yFor(target)
                    paint.color = android.graphics.Color.rgb(190, 199, 208)
                    paint.strokeWidth = 0.8f
                    paint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(4f, 4f), 0f)
                    canvas.drawLine(chartLeft, y, chartRight, y, paint)
                    paint.pathEffect = null
                    drawText(formatGlucoseTick(target, unit), left, y + 3f, 7.5f, color = secondary)
                }

                val medians = patterns.hourlyPercentiles
                val medianPath = android.graphics.Path()
                medians.forEachIndexed { index, percentile ->
                    val x = chartLeft + (index.toFloat() / 23f) * (chartRight - chartLeft)
                    val p10 = yFor(percentile.p10)
                    val p90 = yFor(percentile.p90)
                    paint.color = android.graphics.Color.rgb(184, 220, 198)
                    paint.strokeWidth = 3f
                    canvas.drawLine(x, p10, x, p90, paint)
                    val medianY = yFor(percentile.p50)
                    if (index == 0) medianPath.moveTo(x, medianY) else medianPath.lineTo(x, medianY)
                }
                paint.color = mint
                paint.strokeWidth = 2.2f
                paint.style = Paint.Style.STROKE
                canvas.drawPath(medianPath, paint)
                paint.style = Paint.Style.FILL
                listOf(0 to "00:00", 6 to "06:00", 12 to "12:00", 18 to "18:00", 23 to "24:00").forEach { (hour, label) ->
                    val x = chartLeft + (hour / 23f) * (chartRight - chartLeft)
                    drawText(label, x - 13f, chartBottom + 15f, 7.5f, color = secondary)
                }
            }

            drawText("Distribución de lecturas", left, 501f, 11f, bold = true, color = ink)
            val categoryLines = listOf(
                TirCategory.VERY_LOW to "Muy bajo (<${formatGlucoseTick(54.0, unit)} ${unit.label})",
                TirCategory.LOW to "Bajo (${formatGlucoseTick(54.0, unit)}–${formatGlucoseTick(70.0, unit)} ${unit.label})",
                TirCategory.IN_RANGE to "En rango (${formatGlucoseTick(70.0, unit)}–${formatGlucoseTick(180.0, unit)} ${unit.label})",
                TirCategory.HIGH to "Alto (${formatGlucoseTick(180.0, unit)}–${formatGlucoseTick(250.0, unit)} ${unit.label})",
                TirCategory.VERY_HIGH to "Muy alto (>${formatGlucoseTick(250.0, unit)} ${unit.label})"
            )
            categoryLines.forEachIndexed { index, (category, label) ->
                val percent = tir.buckets.firstOrNull { it.category == category }?.percentage ?: 0.0
                val y = 522f + index * 18f
                val color = when (category) {
                    TirCategory.VERY_LOW -> coral
                    TirCategory.LOW -> android.graphics.Color.rgb(225, 117, 117)
                    TirCategory.IN_RANGE -> mint
                    TirCategory.HIGH -> amber
                    TirCategory.VERY_HIGH -> android.graphics.Color.rgb(227, 118, 46)
                }
                drawText(label, left, y, 8.5f, color = secondary)
                drawText(String.format(Locale.US, "%.1f%%", percent), 440f, y, 9f, bold = true, color = color)
            }

            val sufficiency = if (quality.isSufficientForStandardSummary) {
                "Muestra: al menos 14 días con cobertura estimada de 70% o superior."
            } else {
                "Muestra menor que la referencia habitual de 14 días y 70% de cobertura."
            }
            drawText(sufficiency, left, 623f, 8.5f, bold = true, color = secondary)
            drawText("Rango objetivo usado en este resumen: ${formatGlucoseValue(targetLow.toDouble(), unit)}–${formatGlucoseValue(targetHigh.toDouble(), unit)}.", left, 641f, 8.5f, color = secondary)

            paint.color = android.graphics.Color.rgb(220, 225, 231)
            canvas.drawLine(left, 670f, pageWidth - left, 670f, paint)
            var footerY = 691f
            footerY = drawWrapped(
                "OpenGluco es un visualizador secundario pasivo. No es un dispositivo médico, no sustituye al lector oficial ni a profesionales sanitarios y no calcula dosis de insulina ni recomienda cambios terapéuticos.",
                left, footerY, 108, size = 8f, lineHeight = 11f, color = secondary
            )
            footerY += 3f
            drawWrapped(
                "FreeStyle, Libre, LibreLink, LibreLinkUp y LibreView son marcas registradas de Abbott Laboratories / Abbott Diabetes Care Inc. OpenGluco es independiente y no está afiliada, patrocinada ni respaldada oficialmente por Abbott Laboratories.",
                left, footerY, 108, size = 7.5f, lineHeight = 10f, color = secondary
            )

            document.finishPage(page)
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "OpenGluco_${safeName}_$timestamp.pdf")
            FileOutputStream(file).use { document.writeTo(it) }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "Informe de glucosa - $patientName")
                putExtra(Intent.EXTRA_STREAM, uri as android.os.Parcelable)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Compartir informe de glucosa")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo generar el informe PDF.", Toast.LENGTH_LONG).show()
        } finally {
            document.close()
        }
    }

    private fun formatGlucoseValue(valueMgDl: Double, unit: GlucoseUnit): String = when (unit) {
        GlucoseUnit.MGDL -> "${valueMgDl.toInt()} mg/dL"
        GlucoseUnit.MMOL -> String.format(Locale.US, "%.1f mmol/L", valueMgDl / 18.0182)
    }

    private fun formatGlucoseTick(valueMgDl: Double, unit: GlucoseUnit): String = when (unit) {
        GlucoseUnit.MGDL -> valueMgDl.toInt().toString()
        GlucoseUnit.MMOL -> String.format(Locale.US, "%.1f", valueMgDl / 18.0182)
    }
}
