package com.example.opengluco.core.data

import com.example.opengluco.core.model.AverageGlucoseReport
import com.example.opengluco.core.model.DailyGraphDaySummary
import com.example.opengluco.core.model.DailyGraphReport
import com.example.opengluco.core.model.DailyPatternsReport
import com.example.opengluco.core.model.EstimatedA1cReport
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.HourlyPercentile
import com.example.opengluco.core.model.LowGlucoseEvent
import com.example.opengluco.core.model.LowGlucoseEventsReport
import com.example.opengluco.core.model.ReportTimeBlock
import com.example.opengluco.core.model.SensorInfo
import com.example.opengluco.core.model.SensorUsageReport
import com.example.opengluco.core.model.TimeInRangeReport
import com.example.opengluco.core.model.TirBucket
import com.example.opengluco.core.model.TirCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object ClinicalReportsCalculator {

    const val GMI_LEGAL_DISCLAIMER = "AVISO LEGAL Y REGULATORIO (FDA MDDS / MDR UE 2017/745):\n" +
            "El valor de Indicador de Gestion de Glucosa (GMI / A1c Estimada) es un calculo matematico estimativo retrospectivo basado en el promedio de glucosa del sensor continuo (formula de Bergenstal et al., 2018).\n" +
            "NO es un analisis clinico de laboratorio de Hemoglobina Glicosilada (HbA1c) en sangre venosa y NO debe utilizarse para diagnostico de diabetes ni para modificar pautas terapeuticas sin supervision medica profesional."

    /**
     * Calcula la cantidad real de dias con lecturas disponibles en el historial acumulado.
     * Utilizado para validar si existen suficientes datos para reportes semanales, mensuales o trimestrales.
     */
    fun calculateAvailableDays(readings: List<GlucoseMeasurement>): Int {
        val valid = readings.filter { it.numericValue > 0 }
        if (valid.isEmpty()) return 0

        val distinctDates = valid.mapNotNull { m ->
            val ts = m.timestamp ?: m.factoryTimestamp
            if (!ts.isNullOrBlank() && ts.length >= 10) {
                ts.substring(0, 10)
            } else {
                val epoch = m.getEpochMillis()
                if (epoch > 0) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    sdf.format(Date(epoch))
                } else null
            }
        }.toSet()

        if (distinctDates.isNotEmpty()) {
            return distinctDates.size
        }

        val epochs = valid.mapNotNull { it.getEpochMillis().takeIf { t -> t > 0L } }
        if (epochs.isEmpty()) return 1
        val minT = epochs.minOrNull() ?: return 1
        val maxT = epochs.maxOrNull() ?: return 1
        val spanDays = ((maxT - minT) / (24 * 3600 * 1000L)).toInt() + 1
        return maxOf(1, spanDays)
    }

    /**
     * Filtra las lecturas para conservar unicamente las correspondientes a la ventana de periodDays (relativa al maximo timestamp).
     */
    fun filterReadingsByPeriod(readings: List<GlucoseMeasurement>, periodDays: Int): List<GlucoseMeasurement> {
        val valid = readings.filter { it.numericValue > 0 }
        if (valid.isEmpty()) return emptyList()

        val epochs = valid.mapNotNull { it.getEpochMillis().takeIf { t -> t > 0L } }
        if (epochs.isEmpty()) return valid

        val maxEpoch = epochs.maxOrNull() ?: return valid
        val cutoffEpoch = maxEpoch - (periodDays.toLong() * 24L * 3600L * 1000L)
        return valid.filter { (it.getEpochMillis().takeIf { t -> t > 0L } ?: maxEpoch) >= cutoffEpoch }
    }

    /**
     * 1. Daily Patterns (Patrones Diarios / AGP Modal Day)
     */
    fun calculateDailyPatterns(
        readings: List<GlucoseMeasurement>,
        periodDays: Int,
        targetLow: Double = 70.0,
        targetHigh: Double = 180.0
    ): DailyPatternsReport {
        val valid = filterReadingsByPeriod(readings, periodDays)
        if (valid.isEmpty()) {
            val emptyHourly = (0..23).map { HourlyPercentile(it, 0.0, 0.0, 0.0, 0.0, 0.0, 0) }
            return DailyPatternsReport(periodDays, emptyHourly, 0.0, 0.0, 0.0, targetLow, targetHigh)
        }

        val hourlyBuckets = Array(24) { mutableListOf<Double>() }
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }

        for (r in valid) {
            val hour = extractHour(r.timestamp, sdf)
            if (hour in 0..23) {
                hourlyBuckets[hour].add(r.numericValue)
            }
        }

        val allValues = valid.map { it.numericValue }
        val mean = allValues.average()

        // Si algunas horas no tienen muestras, interpolar suavemente desde las horas vecinas
        for (h in 0..23) {
            if (hourlyBuckets[h].isEmpty()) {
                var leftVal: Double? = null
                for (lh in (h - 1) downTo 0) {
                    if (hourlyBuckets[lh].isNotEmpty()) {
                        leftVal = hourlyBuckets[lh].average()
                        break
                    }
                }
                var rightVal: Double? = null
                for (rh in (h + 1)..23) {
                    if (hourlyBuckets[rh].isNotEmpty()) {
                        rightVal = hourlyBuckets[rh].average()
                        break
                    }
                }
                val interp = when {
                    leftVal != null && rightVal != null -> (leftVal + rightVal) / 2.0
                    leftVal != null -> leftVal
                    rightVal != null -> rightVal
                    else -> mean
                }
                hourlyBuckets[h].add(interp)
            }
        }

        val hourlyPercentiles = (0..23).map { h ->
            val values = hourlyBuckets[h]
            values.sort()
            HourlyPercentile(
                hour = h,
                p10 = calculatePercentile(values, 10.0),
                p25 = calculatePercentile(values, 25.0),
                p50 = calculatePercentile(values, 50.0),
                p75 = calculatePercentile(values, 75.0),
                p90 = calculatePercentile(values, 90.0),
                sampleCount = values.size
            )
        }

        val variance = allValues.map { (it - mean).pow(2) }.average()
        val sd = sqrt(variance)
        val cv = if (mean > 0) (sd / mean) * 100.0 else 0.0
        val mage = calculateMage(valid, sd)

        return DailyPatternsReport(
            periodDays = periodDays,
            hourlyPercentiles = hourlyPercentiles,
            meanGlucose = roundDec(mean, 1),
            standardDeviation = roundDec(sd, 1),
            coefficientOfVariation = roundDec(cv, 1),
            targetLow = targetLow,
            targetHigh = targetHigh,
            mage = mage
        )
    }

    /**
     * 2. Time In Range (Consenso Internacional ATTD 2019 - 5 Niveles, TiTR 70-140 y GRI)
     */
    fun calculateTimeInRange(
        readings: List<GlucoseMeasurement>,
        periodDays: Int
    ): TimeInRangeReport {
        val valid = filterReadingsByPeriod(readings, periodDays)
        val total = valid.size

        if (total == 0) {
            val emptyBuckets = TirCategory.values().map { TirBucket(it, 0, 0.0, 0) }
            return TimeInRangeReport(periodDays, 0, emptyBuckets, 0.0, 0.0, 0.0, 0.0, 0.0, "Sin Datos")
        }

        val counts = mutableMapOf<TirCategory, Int>()
        TirCategory.values().forEach { counts[it] = 0 }

        for (r in valid) {
            val v = r.numericValue
            val cat = when {
                v < 54.0 -> TirCategory.VERY_LOW
                v < 70.0 -> TirCategory.LOW
                v <= 180.0 -> TirCategory.IN_RANGE
                v <= 250.0 -> TirCategory.HIGH
                else -> TirCategory.VERY_HIGH
            }
            counts[cat] = (counts[cat] ?: 0) + 1
        }

        // 1440 minutos en un dia. En 24h el tiempo proporcional = (count / total) * 1440 min
        val buckets = TirCategory.values().map { cat ->
            val c = counts[cat] ?: 0
            val pct = (c.toDouble() / total.toDouble()) * 100.0
            val durationMinutes = ((pct / 100.0) * 1440.0).roundToInt()
            TirBucket(
                category = cat,
                count = c,
                percentage = roundDec(pct, 1),
                durationMinutes = durationMinutes
            )
        }

        val inRange = buckets.find { it.category == TirCategory.IN_RANGE }?.percentage ?: 0.0
        val low = (buckets.find { it.category == TirCategory.LOW }?.percentage ?: 0.0) +
                (buckets.find { it.category == TirCategory.VERY_LOW }?.percentage ?: 0.0)
        val high = (buckets.find { it.category == TirCategory.HIGH }?.percentage ?: 0.0) +
                (buckets.find { it.category == TirCategory.VERY_HIGH }?.percentage ?: 0.0)

        // Time in Tight Range (TiTR: 70 - 140 mg/dL)
        val tightCount = valid.count { it.numericValue in 70.0..140.0 }
        val tightRangePct = roundDec((tightCount.toDouble() / total.toDouble()) * 100.0, 1)

        // Glycemic Risk Index (GRI: Klonoff et al. 2022)
        // GRI = (3.0 * %VLow) + (2.4 * %Low) + (1.6 * %VHigh) + (0.8 * %High)
        val vLowPct = buckets.find { it.category == TirCategory.VERY_LOW }?.percentage ?: 0.0
        val lowPct = buckets.find { it.category == TirCategory.LOW }?.percentage ?: 0.0
        val highPct = buckets.find { it.category == TirCategory.HIGH }?.percentage ?: 0.0
        val vHighPct = buckets.find { it.category == TirCategory.VERY_HIGH }?.percentage ?: 0.0

        val rawGri = (3.0 * vLowPct) + (2.4 * lowPct) + (1.6 * vHighPct) + (0.8 * highPct)
        val gri = roundDec(rawGri.coerceIn(0.0, 100.0), 1)

        val griCategory = when {
            gri <= 20.0 -> "Zona A (Muy Bajo Riesgo)"
            gri <= 40.0 -> "Zona B (Bajo Riesgo)"
            gri <= 60.0 -> "Zona C (Riesgo Moderado)"
            gri <= 80.0 -> "Zona D (Riesgo Alto)"
            else -> "Zona E (Riesgo Muy Alto)"
        }

        return TimeInRangeReport(
            periodDays = periodDays,
            totalReadings = total,
            buckets = buckets,
            inRangePercent = roundDec(inRange, 1),
            belowRangePercent = roundDec(low, 1),
            aboveRangePercent = roundDec(high, 1),
            tightRangePercent = tightRangePct,
            gri = gri,
            griCategory = griCategory
        )
    }

    /**
     * Calcula la Amplitud Media de Excursiones Glucemicas (MAGE) sobre variaciones que superan 1 SD.
     */
    private fun calculateMage(readings: List<GlucoseMeasurement>, sd: Double): Double {
        if (readings.size < 3 || sd <= 0.0) return 0.0
        val values = readings.sortedBy { it.getEpochMillis() }.map { it.numericValue }
        val extrema = mutableListOf<Double>()
        for (i in 1 until values.size - 1) {
            val prev = values[i - 1]
            val curr = values[i]
            val next = values[i + 1]
            if ((curr >= prev && curr > next) || (curr > prev && curr >= next)) {
                extrema.add(curr)
            } else if ((curr <= prev && curr < next) || (curr < prev && curr <= next)) {
                extrema.add(curr)
            }
        }
        if (extrema.size < 2) return 0.0
        val qualifyingExcursions = mutableListOf<Double>()
        for (i in 0 until extrema.size - 1) {
            val diff = kotlin.math.abs(extrema[i + 1] - extrema[i])
            if (diff > sd) {
                qualifyingExcursions.add(diff)
            }
        }
        return if (qualifyingExcursions.isNotEmpty()) {
            roundDec(qualifyingExcursions.average(), 1)
        } else {
            0.0
        }
    }

    /**
     * Evalua si el sensor requiere aviso preventivo de expiracion.
     */
    fun checkSensorExpirationAlert(sensor: SensorInfo?): com.example.opengluco.core.model.SensorExpirationAlert? {
        if (sensor == null) return null
        val daysRemaining = sensor.getRemainingDays() ?: return null
        return when {
            daysRemaining <= 0 -> {
                com.example.opengluco.core.model.SensorExpirationAlert(
                    daysRemaining = 0,
                    hoursRemaining = 0,
                    isCritical = true,
                    title = "Sensor Expirado",
                    message = "La vida util del sensor FreeStyle Libre ha finalizado. Aplica y vincula un nuevo sensor."
                )
            }
            daysRemaining == 1 -> {
                com.example.opengluco.core.model.SensorExpirationAlert(
                    daysRemaining = 1,
                    hoursRemaining = 24,
                    isCritical = false,
                    title = "Sensor Proximo a Expirar",
                    message = "El sensor expira en menos de 24 horas. Prepara un sensor de repuesto."
                )
            }
            else -> null
        }
    }

    /**
     * 3. Low Glucose Events (Eventos de Glucosa Baja)
     */
    fun calculateLowGlucoseEvents(
        readings: List<GlucoseMeasurement>,
        periodDays: Int
    ): LowGlucoseEventsReport {
        val valid = filterReadingsByPeriod(readings, periodDays).sortedBy { it.getEpochMillis() }
        val events = mutableListOf<LowGlucoseEvent>()

        var inEvent = false
        var eventStartEpoch = 0L
        var eventStartTs = ""
        var lowestVal = 1000.0
        var eventReadingsCount = 0

        for (i in valid.indices) {
            val r = valid[i]
            val v = r.numericValue
            val epoch = r.getEpochMillis()

            if (v < 70.0) {
                if (!inEvent) {
                    inEvent = true
                    eventStartEpoch = epoch
                    eventStartTs = r.timestamp ?: ""
                    lowestVal = v
                    eventReadingsCount = 1
                } else {
                    eventReadingsCount++
                    lowestVal = min(lowestVal, v)
                }
            } else {
                if (inEvent) {
                    // Fin del evento
                    val durationMin = max(15, ((epoch - eventStartEpoch) / (60 * 1000)).toInt())
                    // Para calificar como evento clinico, al menos 15 minutos o 2 lecturas consecutivas
                    if (durationMin >= 15 || eventReadingsCount >= 2) {
                        val hour = extractHour(eventStartTs)
                        val block = ReportTimeBlock.fromHour(hour)
                        events.add(
                            LowGlucoseEvent(
                                startTimestamp = eventStartTs,
                                endTimestamp = r.timestamp ?: "",
                                durationMinutes = durationMin,
                                lowestGlucose = lowestVal,
                                timeBlock = block
                            )
                        )
                    }
                    inEvent = false
                }
            }
        }

        // Si termino mientras estaba en evento
        if (inEvent && (eventReadingsCount >= 2)) {
            val last = valid.last()
            val durationMin = max(15, ((last.getEpochMillis() - eventStartEpoch) / (60 * 1000)).toInt())
            val hour = extractHour(eventStartTs)
            events.add(
                LowGlucoseEvent(
                    startTimestamp = eventStartTs,
                    endTimestamp = last.timestamp ?: "",
                    durationMinutes = durationMin,
                    lowestGlucose = lowestVal,
                    timeBlock = ReportTimeBlock.fromHour(hour)
                )
            )
        }

        val byBlock = mutableMapOf<ReportTimeBlock, Int>()
        ReportTimeBlock.values().forEach { byBlock[it] = 0 }
        for (e in events) {
            byBlock[e.timeBlock] = (byBlock[e.timeBlock] ?: 0) + 1
        }

        val avgDuration = if (events.isNotEmpty()) {
            events.map { it.durationMinutes }.average().roundToInt()
        } else {
            0
        }

        return LowGlucoseEventsReport(
            periodDays = periodDays,
            totalEvents = events.size,
            averageDurationMinutes = avgDuration,
            eventsByBlock = byBlock,
            events = events
        )
    }

    /**
     * 4. Average Glucose (Glucosa Promedio)
     */
    fun calculateAverageGlucose(
        readings: List<GlucoseMeasurement>,
        periodDays: Int,
        targetLow: Double = 70.0,
        targetHigh: Double = 180.0
    ): AverageGlucoseReport {
        val valid = filterReadingsByPeriod(readings, periodDays)
        if (valid.isEmpty()) {
            val emptyBlocks = ReportTimeBlock.values().associateWith { 0.0 }
            val emptyHourly = List(24) { 0.0 }
            return AverageGlucoseReport(periodDays, 0.0, 0.0, emptyBlocks, emptyHourly, targetLow, targetHigh)
        }

        val overallMgDl = valid.map { it.numericValue }.average()
        val overallMmolL = overallMgDl / 18.0182

        val blockLists = mutableMapOf<ReportTimeBlock, MutableList<Double>>()
        ReportTimeBlock.values().forEach { blockLists[it] = mutableListOf() }
        val hourlyLists = Array(24) { mutableListOf<Double>() }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (r in valid) {
            val hour = extractHour(r.timestamp, sdf)
            val block = ReportTimeBlock.fromHour(hour)
            blockLists[block]?.add(r.numericValue)
            if (hour in 0..23) {
                hourlyLists[hour].add(r.numericValue)
            }
        }

        val byBlock = ReportTimeBlock.values().associateWith { b ->
            val list = blockLists[b].orEmpty()
            if (list.isNotEmpty()) roundDec(list.average(), 1) else 0.0
        }

        val hourlyAverages = (0..23).map { h ->
            val list = hourlyLists[h]
            if (list.isNotEmpty()) roundDec(list.average(), 1) else 0.0
        }

        return AverageGlucoseReport(
            periodDays = periodDays,
            overallAverageMgDl = roundDec(overallMgDl, 1),
            overallAverageMmolL = roundDec(overallMmolL, 1),
            averageByBlock = byBlock,
            hourlyAverages = hourlyAverages,
            targetLow = targetLow,
            targetHigh = targetHigh
        )
    }

    /**
     * 5. Daily Graph (Grafico Diario Navegable)
     */
    fun calculateDailyGraph(readings: List<GlucoseMeasurement>): DailyGraphReport {
        val valid = readings.filter { it.numericValue > 0 }.sortedBy { it.getEpochMillis() }
        if (valid.isEmpty()) {
            return DailyGraphReport(emptyList(), 0)
        }

        val dateGroups = mutableMapOf<String, MutableList<GlucoseMeasurement>>()
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (r in valid) {
            val ts = r.timestamp.orEmpty()
            val dateKey = if (ts.length >= 10) ts.substring(0, 10) else sdfDate.format(Date(r.getEpochMillis()))
            dateGroups.getOrPut(dateKey) { mutableListOf() }.add(r)
        }

        val summaries = dateGroups.entries.sortedByDescending { it.key }.map { (dateStr, dayReadings) ->
            val vals = dayReadings.map { it.numericValue }
            val mean = vals.average()
            val minV = vals.minOrNull() ?: 0.0
            val maxV = vals.maxOrNull() ?: 0.0
            val inRangeCount = vals.count { it in 70.0..180.0 }
            val inRangePct = if (vals.isNotEmpty()) (inRangeCount.toDouble() / vals.size) * 100.0 else 0.0
            val lowEvents = calculateLowGlucoseEvents(dayReadings, 1).totalEvents

            DailyGraphDaySummary(
                dateString = dateStr,
                readings = dayReadings,
                meanGlucose = roundDec(mean, 1),
                minGlucose = roundDec(minV, 1),
                maxGlucose = roundDec(maxV, 1),
                inRangePercent = roundDec(inRangePct, 1),
                lowEventsCount = lowEvents
            )
        }

        return DailyGraphReport(summaries, 0)
    }

    /**
     * 6. Estimated A1c / GMI (Formula Clinica de Bergenstal et al. 2018)
     */
    fun calculateEstimatedA1c(
        readings: List<GlucoseMeasurement>,
        periodDays: Int
    ): EstimatedA1cReport {
        val valid = filterReadingsByPeriod(readings, periodDays)
        if (valid.isEmpty()) {
            return EstimatedA1cReport(periodDays, 0.0, 0.0, 0.0, 0, false, GMI_LEGAL_DISCLAIMER)
        }

        val meanMgDl = valid.map { it.numericValue }.average()
        val meanMmolL = meanMgDl / 18.0182

        // Formula oficial Bergenstal et al. (Diabetes Care 2018):
        // GMI (%) = 3.31 + 0.02392 * [mean glucose mg/dL]
        // GMI (mmol/mol) = 12.71 + 4.70587 * [mean glucose mmol/L]
        val gmiPercent = 3.31 + (0.02392 * meanMgDl)
        val gmiMmolMol = 12.71 + (4.70587 * meanMmolL)

        // Verificacion de suficiencia de datos (>= 14 dias con >= 70% de cobertura)
        val dateSet = valid.mapNotNull { it.timestamp?.take(10) }.toSet()
        val distinctDays = dateSet.size
        val expectedReadings = max(14, periodDays) * 96
        val isSufficient = distinctDays >= 14 || (valid.size >= (expectedReadings * 0.70))

        return EstimatedA1cReport(
            periodDays = periodDays,
            meanGlucoseMgDl = roundDec(meanMgDl, 1),
            gmiPercent = roundDec(gmiPercent, 1),
            gmiMmolMol = roundDec(gmiMmolMol, 1),
            dataSufficiencyDays = distinctDays,
            isSufficient = isSufficient,
            disclaimerText = GMI_LEGAL_DISCLAIMER
        )
    }

    /**
     * 7. Sensor Usage (Uso y Rendimiento del Sensor)
     */
    fun calculateSensorUsage(
        readings: List<GlucoseMeasurement>,
        sensor: SensorInfo?,
        periodDays: Int
    ): SensorUsageReport {
        val valid = filterReadingsByPeriod(readings, periodDays)
        val expected = max(1, periodDays) * 96 // 96 lecturas por dia cada 15 min
        val actual = valid.size
        val coverage = min(100.0, (actual.toDouble() / expected.toDouble()) * 100.0)

        val remainingDays = sensor?.getRemainingDays() ?: 14
        val model = sensor?.sensorModelName ?: "FreeStyle Libre 3"
        val serial = sensor?.serialNumber ?: "SN-LIBRE-AUTO"
        val active = remainingDays > 0

        return SensorUsageReport(
            periodDays = periodDays,
            totalExpectedReadings = expected,
            totalCapturedReadings = actual,
            coveragePercentage = roundDec(coverage, 1),
            sensorModelName = model,
            sensorSerialNumber = serial,
            daysRemaining = remainingDays,
            isActive = active
        )
    }

    // --- Helpers de Calculo Estadistico ---

    private fun calculatePercentile(sortedValues: List<Double>, percentile: Double): Double {
        if (sortedValues.isEmpty()) return 0.0
        if (sortedValues.size == 1) return sortedValues[0]

        // Metodo Hyndman-Fan Tipo 7 (estandar internacional)
        val rank = (percentile / 100.0) * (sortedValues.size - 1)
        val lowerIndex = rank.toInt()
        val upperIndex = min(lowerIndex + 1, sortedValues.size - 1)
        val weight = rank - lowerIndex

        val result = sortedValues[lowerIndex] + weight * (sortedValues[upperIndex] - sortedValues[lowerIndex])
        return roundDec(result, 1)
    }

    private fun extractHour(timestamp: String?, sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)): Int {
        if (timestamp.isNullOrBlank()) return 12
        return try {
            if (timestamp.contains(":") && timestamp.length >= 13) {
                // Formato ISO "YYYY-MM-DD HH:mm:ss" -> substring en posicion 11..12
                val hourSub = timestamp.substring(11, 13)
                hourSub.toIntOrNull() ?: 12
            } else {
                val d = sdf.parse(timestamp)
                val cal = Calendar.getInstance()
                if (d != null) cal.time = d
                cal.get(Calendar.HOUR_OF_DAY)
            }
        } catch (e: Exception) {
            12
        }
    }

    private fun roundDec(value: Double, decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (value * multiplier).roundToInt() / multiplier
    }
}
