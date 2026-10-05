package com.example.opengluco.core.model

/**
 * Categorias clinicas de Tiempo en Rango segun el Consenso Internacional ATTD 2019.
 */
enum class TirCategory(
    val label: String,
    val targetPercent: String,
    val minMgDl: Double,
    val maxMgDl: Double
) {
    VERY_LOW("Muy Bajo", "< 1%", 0.0, 53.99),
    LOW("Bajo", "< 4%", 54.0, 69.99),
    IN_RANGE("En Rango", "> 70%", 70.0, 180.0),
    HIGH("Alto", "< 25%", 180.01, 250.0),
    VERY_HIGH("Muy Alto", "< 5%", 250.01, 1000.0)
}

data class TirBucket(
    val category: TirCategory,
    val count: Int,
    val percentage: Double,
    val durationMinutes: Int
) {
    fun getFormattedDuration(): String {
        val hours = durationMinutes / 60
        val mins = durationMinutes % 60
        return if (hours > 0) "${hours}h ${mins}min" else "${mins}min"
    }
}

data class TimeInRangeReport(
    val periodDays: Int,
    val totalReadings: Int,
    val buckets: List<TirBucket>,
    val inRangePercent: Double,
    val belowRangePercent: Double,
    val aboveRangePercent: Double,
    val tightRangePercent: Double = 0.0, // Time in Tight Range (70 - 140 mg/dL)
    val gri: Double = 0.0, // Glycemic Risk Index (Klonoff et al. 2022)
    val griCategory: String = "Bajo Riesgo"
)

data class HourlyPercentile(
    val hour: Int,
    val p10: Double,
    val p25: Double,
    val p50: Double, // Mediana
    val p75: Double,
    val p90: Double,
    val sampleCount: Int
)

data class DailyPatternsReport(
    val periodDays: Int,
    val hourlyPercentiles: List<HourlyPercentile>,
    val meanGlucose: Double,
    val standardDeviation: Double,
    val coefficientOfVariation: Double, // CV % = (SD / Mean) * 100
    val targetLow: Double = 70.0,
    val targetHigh: Double = 180.0,
    val mage: Double = 0.0 // Mean Amplitude of Glycemic Excursions
)

data class SensorExpirationAlert(
    val daysRemaining: Int = 0,
    val hoursRemaining: Int = 0,
    val isCritical: Boolean = false,
    val title: String,
    val message: String
)

enum class ReportTimeBlock(val label: String, val startHour: Int, val endHour: Int) {
    NIGHT("Madrugada (00:00 - 06:00)", 0, 5),
    MORNING("Manana (06:00 - 12:00)", 6, 11),
    AFTERNOON("Tarde (12:00 - 18:00)", 12, 17),
    EVENING("Noche (18:00 - 24:00)", 18, 23);

    val displayName: String
        get() = when (this) {
            NIGHT -> "Madrugada"
            MORNING -> "Manana"
            AFTERNOON -> "Tarde"
            EVENING -> "Noche"
        }

    val hourRange: String
        get() = when (this) {
            NIGHT -> "00:00 - 06:00"
            MORNING -> "06:00 - 12:00"
            AFTERNOON -> "12:00 - 18:00"
            EVENING -> "18:00 - 24:00"
        }

    companion object {
        fun fromHour(hour: Int): ReportTimeBlock {
            return when (hour) {
                in 0..5 -> NIGHT
                in 6..11 -> MORNING
                in 12..17 -> AFTERNOON
                else -> EVENING
            }
        }
    }
}

data class LowGlucoseEvent(
    val startTimestamp: String,
    val endTimestamp: String,
    val durationMinutes: Int,
    val lowestGlucose: Double,
    val timeBlock: ReportTimeBlock
)

data class LowGlucoseEventsReport(
    val periodDays: Int,
    val totalEvents: Int,
    val averageDurationMinutes: Int,
    val eventsByBlock: Map<ReportTimeBlock, Int>,
    val events: List<LowGlucoseEvent>
)

data class AverageGlucoseReport(
    val periodDays: Int,
    val overallAverageMgDl: Double,
    val overallAverageMmolL: Double,
    val averageByBlock: Map<ReportTimeBlock, Double>,
    val hourlyAverages: List<Double>, // 24 valores para grafico horario
    val targetLow: Double = 70.0,
    val targetHigh: Double = 180.0
)

data class DailyGraphDaySummary(
    val dateString: String, // YYYY-MM-DD
    val readings: List<GlucoseMeasurement>,
    val meanGlucose: Double,
    val minGlucose: Double,
    val maxGlucose: Double,
    val inRangePercent: Double,
    val lowEventsCount: Int
)

data class DailyGraphReport(
    val days: List<DailyGraphDaySummary>,
    val selectedDayIndex: Int = 0
)

data class EstimatedA1cReport(
    val periodDays: Int,
    val meanGlucoseMgDl: Double,
    val gmiPercent: Double, // Bergenstal formula: 3.31 + 0.02392 * mean
    val gmiMmolMol: Double, // 12.71 + 4.70587 * mean_mmol
    val dataSufficiencyDays: Int,
    val isSufficient: Boolean, // Requiere >= 14 dias con >= 70% cobertura
    val disclaimerText: String
)

data class SensorUsageReport(
    val periodDays: Int,
    val totalExpectedReadings: Int,
    val totalCapturedReadings: Int,
    val coveragePercentage: Double,
    val sensorModelName: String,
    val sensorSerialNumber: String,
    val daysRemaining: Int,
    val isActive: Boolean,
    val daysWithReadings: Int = 0,
    val samplingIntervalMinutes: Int? = null,
    val gapCount: Int = 0,
    val longestGapMinutes: Int = 0
)

data class DataQualityReport(
    val periodDays: Int,
    val totalReadings: Int,
    val daysWithReadings: Int,
    val coveragePercentage: Double?,
    val expectedReadingsInObservedSpan: Int?,
    val samplingIntervalMinutes: Int?,
    val gapCount: Int,
    val longestGapMinutes: Int,
    val latestReadingEpochMs: Long?,
    val latestReadingAgeMinutes: Long?,
    val isSufficientForStandardSummary: Boolean
)

enum class GlucoseEventType(val label: String) {
    MEAL("Comida"),
    ACTIVITY("Actividad"),
    ILLNESS("Malestar"),
    SENSOR_CHANGE("Cambio de sensor")
}

data class GlucoseEventMarker(
    val id: String,
    val patientId: String,
    val timestampMs: Long,
    val type: GlucoseEventType,
    val note: String? = null
)

data class PeriodSummary(
    val periodDays: Int = 1,
    val totalCount: Int = 0,
    val mean: Double = 0.0,
    val min: Double = 0.0,
    val max: Double = 0.0,
    val veryLowCount: Int = 0,
    val lowCount: Int = 0,
    val inRangeCount: Int = 0,
    val highCount: Int = 0,
    val veryHighCount: Int = 0,
    val tightRangeCount: Int = 0,
    val inRangePercent: Double = 0.0,
    val belowRangePercent: Double = 0.0,
    val aboveRangePercent: Double = 0.0,
    val tightRangePercent: Double = 0.0,
    val veryLowPercent: Double = 0.0,
    val lowPercent: Double = 0.0,
    val highPercent: Double = 0.0,
    val veryHighPercent: Double = 0.0,
    val gri: Double = 0.0,
    val griCategory: String = "Zona A (Muy Bajo Riesgo)"
)

data class BlockMetric(
    val block: ReportTimeBlock,
    val averageGlucose: Double = 0.0,
    val lowEventsCount: Int = 0
)
