package com.example.opengluco.core.data

import com.example.opengluco.core.model.AverageGlucoseReport
import com.example.opengluco.core.model.DailyGraphDaySummary
import com.example.opengluco.core.model.DailyGraphReport
import com.example.opengluco.core.model.DailyPatternsReport
import com.example.opengluco.core.model.DataQualityReport
import com.example.opengluco.core.model.EstimatedA1cReport
import com.example.opengluco.core.model.FastDateParser
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
        if (readings.isEmpty()) return 0

        val tz = TimeZone.getDefault()
        val distinctDays = HashSet<Long>(readings.size)
        var validCount = 0
        var minEpoch = Long.MAX_VALUE
        var maxEpoch = Long.MIN_VALUE

        for (i in readings.indices) {
            val m = readings[i]
            if (m.numericValue <= 0.0) continue
            validCount++
            val epoch = m.getEpochMillis()
            if (epoch > 0L) {
                distinctDays.add((epoch + tz.getOffset(epoch)) / (24L * 3600L * 1000L))
                if (epoch < minEpoch) minEpoch = epoch
                if (epoch > maxEpoch) maxEpoch = epoch
            } else {
                val ts = m.timestamp ?: m.factoryTimestamp
                if (!ts.isNullOrBlank() && ts.length >= 10 && ts[4] == '-' && ts[7] == '-') {
                    val fallbackEpoch = FastDateParser.parseEpoch(ts)
                    if (fallbackEpoch > 0L) {
                        distinctDays.add((fallbackEpoch + tz.getOffset(fallbackEpoch)) / (24L * 3600L * 1000L))
                        if (fallbackEpoch < minEpoch) minEpoch = fallbackEpoch
                        if (fallbackEpoch > maxEpoch) maxEpoch = fallbackEpoch
                    }
                }
            }
        }

        if (validCount == 0) return 0
        if (distinctDays.isNotEmpty()) {
            return distinctDays.size
        }

        if (maxEpoch >= minEpoch && minEpoch != Long.MAX_VALUE) {
            val spanDays = ((maxEpoch - minEpoch) / (24 * 3600 * 1000L)).toInt() + 1
            return maxOf(1, spanDays)
        }
        return 1
    }

    /**
     * Filtra las lecturas para conservar unicamente las correspondientes a la ventana de periodDays.
     * Utiliza System.currentTimeMillis() como anclaje por defecto para que la ventana (p. ej. 24h) sea exacta
     * y se base en el reloj real del sistema, con fallback a la lectura mas reciente si el dataset es historico o de prueba.
     */
    fun filterReadingsByPeriod(
        readings: List<GlucoseMeasurement>,
        periodDays: Int,
        anchorTimeMs: Long = System.currentTimeMillis()
    ): List<GlucoseMeasurement> {
        if (readings.isEmpty()) return emptyList()

        var validCount = 0
        var maxEpoch = Long.MIN_VALUE
        for (i in readings.indices) {
            val m = readings[i]
            if (m.numericValue > 0.0) {
                validCount++
                val ep = m.getEpochMillis()
                if (ep > maxEpoch) maxEpoch = ep
            }
        }
        if (validCount == 0) return emptyList()

        val effectiveMax = if (maxEpoch > 0L) maxEpoch else anchorTimeMs
        val windowMs = periodDays.toLong() * 24L * 3600L * 1000L
        val effectiveAnchor = if (effectiveMax < anchorTimeMs - windowMs) effectiveMax else anchorTimeMs
        val cutoffEpoch = effectiveAnchor - windowMs
        val endEpoch = effectiveAnchor + 300_000L

        val result = ArrayList<GlucoseMeasurement>(validCount)
        for (i in readings.indices) {
            val m = readings[i]
            if (m.numericValue > 0.0) {
                val ep = m.getEpochMillis()
                val effectiveEp = if (ep > 0L) ep else effectiveAnchor
                if (effectiveEp in cutoffEpoch..endEpoch) {
                    result.add(m)
                }
            }
        }
        return result
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

        var sum = 0.0
        for (i in valid.indices) {
            val r = valid[i]
            sum += r.numericValue
            val hour = extractHour(r, sdf)
            if (hour in 0..23) {
                hourlyBuckets[hour].add(r.numericValue)
            }
        }

        val mean = sum / valid.size

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

        var sumSqDiff = 0.0
        for (i in valid.indices) {
            val diff = valid[i].numericValue - mean
            sumSqDiff += diff * diff
        }
        val variance = sumSqDiff / valid.size
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

        var vLowCount = 0
        var lowCount = 0
        var inRangeCount = 0
        var highCount = 0
        var vHighCount = 0
        var tightCount = 0

        for (r in valid) {
            val v = r.numericValue
            when {
                v < 54.0 -> vLowCount++
                v < 70.0 -> lowCount++
                v <= 180.0 -> inRangeCount++
                v <= 250.0 -> highCount++
                else -> vHighCount++
            }
            if (v in 70.0..140.0) {
                tightCount++
            }
        }

        val totalD = total.toDouble()
        val vLowPctRaw = (vLowCount.toDouble() / totalD) * 100.0
        val lowPctRaw = (lowCount.toDouble() / totalD) * 100.0
        val inRangePctRaw = (inRangeCount.toDouble() / totalD) * 100.0
        val highPctRaw = (highCount.toDouble() / totalD) * 100.0
        val vHighPctRaw = (vHighCount.toDouble() / totalD) * 100.0

        val vLowDuration = ((vLowPctRaw / 100.0) * 1440.0).roundToInt()
        val lowDuration = ((lowPctRaw / 100.0) * 1440.0).roundToInt()
        val inRangeDuration = ((inRangePctRaw / 100.0) * 1440.0).roundToInt()
        val highDuration = ((highPctRaw / 100.0) * 1440.0).roundToInt()
        val vHighDuration = ((vHighPctRaw / 100.0) * 1440.0).roundToInt()

        val inRange = roundDec(inRangePctRaw, 1)
        val low = roundDec(vLowPctRaw + lowPctRaw, 1)
        val high = roundDec(highPctRaw + vHighPctRaw, 1)

        val buckets = listOf(
            TirBucket(TirCategory.VERY_LOW, vLowCount, roundDec(vLowPctRaw, 1), vLowDuration),
            TirBucket(TirCategory.LOW, lowCount, roundDec(lowPctRaw, 1), lowDuration),
            TirBucket(TirCategory.IN_RANGE, inRangeCount, inRange, inRangeDuration),
            TirBucket(TirCategory.HIGH, highCount, roundDec(highPctRaw, 1), highDuration),
            TirBucket(TirCategory.VERY_HIGH, vHighCount, roundDec(vHighPctRaw, 1), vHighDuration)
        )

        val tightRangePct = roundDec((tightCount.toDouble() / totalD) * 100.0, 1)

        val rawGri = (3.0 * vLowPctRaw) + (2.4 * lowPctRaw) + (1.6 * vHighPctRaw) + (0.8 * highPctRaw)
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
        if (sensor == null || !sensor.isValid) {
            return com.example.opengluco.core.model.SensorExpirationAlert(
                daysRemaining = 0,
                hoursRemaining = 0,
                isCritical = true,
                title = "Sin Sensor Activo",
                message = "No se detecta ningun sensor activo vinculado. Inicia un nuevo sensor FreeStyle Libre."
            )
        }
        val daysRemaining = sensor.getRemainingDays() ?: 0
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
            val hour = extractHour(r, sdf)
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
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }

        for (r in valid) {
            val epoch = r.getEpochMillis()
            val ts = r.timestamp ?: r.factoryTimestamp ?: ""
            val dateKey = if (epoch > 0L) {
                sdfDate.format(Date(epoch))
            } else if (ts.length >= 10 && ts[4] == '-' && ts[7] == '-') {
                ts.substring(0, 10)
            } else {
                "Desconocido"
            }
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

        // El resumen estandar requiere al menos 14 dias con datos y un 70% de cobertura estimada.
        val quality = calculateDataQuality(readings, periodDays)
        val distinctDays = quality.daysWithReadings
        val isSufficient = quality.isSufficientForStandardSummary

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
        val quality = calculateDataQuality(readings, periodDays)
        val valid = filterReadingsByPeriod(readings, periodDays)
        val expected = if (quality.expectedReadingsInObservedSpan != null && quality.expectedReadingsInObservedSpan!! > 0) {
            quality.expectedReadingsInObservedSpan!!
        } else {
            max(1, periodDays) * 96
        }
        val actual = maxOf(quality.totalReadings, valid.size)
        val coverage = if (actual >= expected && expected > 0) {
            100.0
        } else {
            quality.coveragePercentage ?: min(100.0, (actual.toDouble() / expected.toDouble()) * 100.0)
        }

        val remainingDays = sensor?.getRemainingDays() ?: 0
        val model = if (sensor != null && sensor.isValid) sensor.sensorModelName else "Sin sensor"
        val serial = sensor?.serialNumber?.takeIf { it.isNotBlank() } ?: "Sin sensor"
        val active = sensor != null && sensor.isValid && remainingDays > 0 && sensor.isSensorActive != false

        return SensorUsageReport(
            periodDays = periodDays,
            totalExpectedReadings = expected,
            totalCapturedReadings = actual,
            coveragePercentage = roundDec(coverage, 1),
            sensorModelName = model,
            sensorSerialNumber = serial,
            daysRemaining = remainingDays,
            isActive = active,
            daysWithReadings = quality.daysWithReadings,
            samplingIntervalMinutes = quality.samplingIntervalMinutes,
            gapCount = quality.gapCount,
            longestGapMinutes = quality.longestGapMinutes
        )
    }

    /**
     * Estima continuidad a partir de la cadencia observada entre lecturas del mismo periodo.
     * La cobertura describe solo el tramo comprendido entre la primera y la ultima lectura;
     * no infiere el tiempo anterior a la primera lectura ni posterior a la ultima.
     */
    fun calculateDataQuality(
        readings: List<GlucoseMeasurement>,
        periodDays: Int,
        nowEpochMs: Long = System.currentTimeMillis()
    ): DataQualityReport {
        val valid = filterReadingsByPeriod(readings, periodDays, nowEpochMs)
            .filter { it.numericValue > 0.0 }
        val epochs = valid.mapNotNull { it.getEpochMillis().takeIf { epoch -> epoch > 0L } }
            .distinct()
            .sorted()

        if (epochs.isEmpty()) {
            return DataQualityReport(
                periodDays = periodDays,
                totalReadings = valid.size,
                daysWithReadings = 0,
                coveragePercentage = null,
                expectedReadingsInObservedSpan = null,
                samplingIntervalMinutes = null,
                gapCount = 0,
                longestGapMinutes = 0,
                latestReadingEpochMs = null,
                latestReadingAgeMinutes = null,
                isSufficientForStandardSummary = false
            )
        }

        val intervals = epochs.zipWithNext { earlier, later -> later - earlier }
            .filter { it > 0L }
        val cadenceCandidates = intervals.filter { it in MIN_CADENCE_MS..MAX_CADENCE_MS }.sorted()
        val cadenceMs = cadenceCandidates.takeIf { it.isNotEmpty() }?.let { it[it.size / 2] }
        val expectedReadings = if (cadenceMs != null && epochs.size > 1) {
            (((epochs.last() - epochs.first()).toDouble() / cadenceMs).toInt() + 1).coerceAtLeast(epochs.size)
        } else {
            null
        }
        val coverage = if (expectedReadings != null && expectedReadings > 0) {
            min(100.0, (epochs.size.toDouble() / expectedReadings.toDouble()) * 100.0)
        } else {
            null
        }
        val gapThresholdMs = cadenceMs?.let { max(it * 3L, MIN_GAP_MS) }
        val longGaps = if (gapThresholdMs == null) emptyList() else intervals.filter { it > gapThresholdMs }
        val tz = TimeZone.getDefault()
        val distinctDays = HashSet<Long>(epochs.size)
        for (ep in epochs) {
            distinctDays.add((ep + tz.getOffset(ep)) / (24L * 3600L * 1000L))
        }
        val daysWithReadings = distinctDays.size
        val latestEpoch = epochs.last()
        val ageMinutes = ((nowEpochMs - latestEpoch).coerceAtLeast(0L) / 60_000L)
        val sufficient = daysWithReadings >= MIN_STANDARD_REPORT_DAYS && (coverage ?: 0.0) >= MIN_STANDARD_REPORT_COVERAGE

        return DataQualityReport(
            periodDays = periodDays,
            totalReadings = epochs.size,
            daysWithReadings = daysWithReadings,
            coveragePercentage = coverage?.let { roundDec(it, 1) },
            expectedReadingsInObservedSpan = expectedReadings,
            samplingIntervalMinutes = cadenceMs?.let { (it / 60_000.0).roundToInt().coerceAtLeast(1) },
            gapCount = longGaps.size,
            longestGapMinutes = (longGaps.maxOrNull() ?: 0L).let { (it / 60_000.0).roundToInt() },
            latestReadingEpochMs = latestEpoch,
            latestReadingAgeMinutes = ageMinutes,
            isSufficientForStandardSummary = sufficient
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

    private fun extractHour(timestamp: String?, sdf: SimpleDateFormat? = null): Int {
        if (timestamp.isNullOrBlank()) return 12
        if (timestamp.length >= 13 && timestamp[4] == '-' && timestamp[7] == '-') {
            val hourSub = timestamp.substring(11, 13)
            val h = hourSub.toIntOrNull()
            if (h != null && h in 0..23) return h
        }
        val epoch = FastDateParser.parseEpoch(timestamp)
        if (epoch > 0L) {
            val localOffset = TimeZone.getDefault().getOffset(epoch)
            val totalSeconds = (epoch + localOffset) / 1000L
            val hour = ((totalSeconds % 86400L) / 3600L).toInt()
            return if (hour in 0..23) hour else 12
        }
        return try {
            val formatter = sdf ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getDefault()
            }
            val d = formatter.parse(timestamp)
            if (d != null) {
                val cal = Calendar.getInstance()
                cal.time = d
                cal.get(Calendar.HOUR_OF_DAY)
            } else 12
        } catch (_: Exception) {
            12
        }
    }

    private fun extractHour(reading: GlucoseMeasurement, sdf: SimpleDateFormat? = null): Int {
        val epoch = reading.getEpochMillis()
        if (epoch > 0L) {
            val localOffset = TimeZone.getDefault().getOffset(epoch)
            val totalSeconds = (epoch + localOffset) / 1000L
            val hour = ((totalSeconds % 86400L) / 3600L).toInt()
            return if (hour in 0..23) hour else 12
        }
        return extractHour(reading.timestamp ?: reading.factoryTimestamp, sdf)
    }

    private fun roundDec(value: Double, decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (value * multiplier).roundToInt() / multiplier
    }

    private const val MIN_CADENCE_MS = 30_000L
    private const val MAX_CADENCE_MS = 30L * 60_000L
    private const val MIN_GAP_MS = 15L * 60_000L
    private const val MIN_STANDARD_REPORT_DAYS = 14
    private const val MIN_STANDARD_REPORT_COVERAGE = 70.0
}
