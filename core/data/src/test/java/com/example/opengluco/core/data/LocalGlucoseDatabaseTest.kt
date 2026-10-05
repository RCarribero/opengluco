package com.example.opengluco.core.data

import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.ReportTimeBlock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalGlucoseDatabaseTest {

    private lateinit var db: LocalGlucoseDatabase

    @Before
    fun setUp() {
        db = LocalGlucoseDatabase(null, "test_glucose.db")
        db.clearData()
    }

    private fun createReading(
        valMg: Double,
        hour: Int,
        minute: Int = 0,
        dayOffset: Int = 0,
        patientId: String = "patient-1"
    ): GlucoseMeasurement {
        val hStr = String.format("%02d", hour)
        val mStr = String.format("%02d", minute)
        val dStr = String.format("%02d", 10 + dayOffset)
        val ts = "2026-08-${dStr}T${hStr}:${mStr}:00.000Z"
        return GlucoseMeasurement(
            factoryTimestamp = ts,
            timestamp = "2026-08-${dStr} ${hStr}:${mStr}:00",
            valueInMgPerDl = valMg,
            value = valMg,
            trendArrow = 3,
            trendMessage = "Estable",
            measurementColor = 1,
            glucoseUnits = 1,
            isHigh = valMg > 180.0,
            isLow = valMg < 70.0
        )
    }

    @Test
    fun testInsertAndDeduplication() {
        val r1 = createReading(110.0, 10, 0, 0)
        val r2 = createReading(115.0, 10, 0, 0) // Mismo timestamp y paciente -> debe ignorarse por PK
        val r3 = createReading(120.0, 10, 15, 0) // Distinto timestamp

        val count1 = db.insertReadings(listOf(r1, r2), "patient-1")
        assertEquals("Solo 1 registro insertado debido a clave primaria duplicada", 1, count1)

        val count2 = db.insertReadings(listOf(r3), "patient-1")
        assertEquals(1, count2)

        val all = db.getReadingsBetween("patient-1", 0L, Long.MAX_VALUE)
        assertEquals(2, all.size)
        assertEquals(110.0, all[0].numericValue, 0.01)
        assertEquals(120.0, all[1].numericValue, 0.01)
    }

    @Test
    fun testPeriodSummaryCalculation_includingTirAndGri() {
        val readings = listOf(
            createReading(45.0, 1, 0, 0),   // VERY_LOW (<54)
            createReading(60.0, 2, 0, 0),   // LOW (54-69)
            createReading(100.0, 3, 0, 0),  // IN_RANGE (70-180) y TIGHT_RANGE (70-140)
            createReading(120.0, 4, 0, 0),  // IN_RANGE y TIGHT_RANGE
            createReading(160.0, 5, 0, 0),  // IN_RANGE pero no tight (>140)
            createReading(200.0, 6, 0, 0),  // HIGH (181-250)
            createReading(300.0, 7, 0, 0)   // VERY_HIGH (>250)
        )

        db.insertReadings(readings, "patient-1")
        val summary = db.getPeriodSummary("patient-1", 7)

        assertEquals(7, summary.totalCount)
        assertEquals(1, summary.veryLowCount)
        assertEquals(1, summary.lowCount)
        assertEquals(3, summary.inRangeCount)
        assertEquals(1, summary.highCount)
        assertEquals(1, summary.veryHighCount)
        assertEquals(2, summary.tightRangeCount)

        assertEquals(45.0, summary.min, 0.1)
        assertEquals(300.0, summary.max, 0.1)
        assertTrue("GRI must be > 0", summary.gri > 0.0)
        assertTrue("GRI category must be defined", summary.griCategory.startsWith("Zona"))
    }

    @Test
    fun testTimeBlockSummary() {
        val readings = listOf(
            createReading(90.0, 2, 0, 0),   // NIGHT
            createReading(110.0, 8, 0, 0),  // MORNING
            createReading(130.0, 14, 0, 0), // AFTERNOON
            createReading(150.0, 20, 0, 0)  // EVENING
        )

        db.insertReadings(readings, "patient-1")
        val blocks = db.getTimeBlockSummary("patient-1", 7)

        assertEquals(4, blocks.size)
        val night = blocks.find { it.block == ReportTimeBlock.NIGHT }!!
        val morning = blocks.find { it.block == ReportTimeBlock.MORNING }!!
        val afternoon = blocks.find { it.block == ReportTimeBlock.AFTERNOON }!!
        val evening = blocks.find { it.block == ReportTimeBlock.EVENING }!!

        assertEquals(90.0, night.averageGlucose, 0.1)
        assertEquals(110.0, morning.averageGlucose, 0.1)
        assertEquals(130.0, afternoon.averageGlucose, 0.1)
        assertEquals(150.0, evening.averageGlucose, 0.1)
    }

    @Test
    fun testDailyPatternsHourlyPercentiles() {
        val readings = mutableListOf<GlucoseMeasurement>()
        for (i in 1..10) {
            readings.add(createReading(100.0 + (i * 10.0), 8, i * 5, 0))
        }

        db.insertReadings(readings, "patient-1")
        val patterns = db.getDailyPatterns("patient-1", 7)

        assertEquals(24, patterns.hourlyPercentiles.size)
        val h8 = patterns.hourlyPercentiles[8]
        assertEquals(8, h8.hour)
        assertEquals(10, h8.sampleCount)
        assertTrue(h8.p10 < h8.p90)
        assertTrue(patterns.coefficientOfVariation > 0.0)
    }

    @Test
    fun testImportLibreViewCsv_standardFormat() {
        val csv = """
            Dispositivo,Número de serie,Hora del dispositivo,Tipo de registro,Historial de glucosa (mg/dL),Glucosa escaneada (mg/dL)
            FreeStyle Libre 3,0M001A8934,22-09-2026 10:00,0,118,
            FreeStyle Libre 3,0M001A8934,22-09-2026 10:15,0,122,
            FreeStyle Libre 3,0M001A8934,22-09-2026 10:20,1,,125
        """.trimIndent()

        val imported = db.importLibreViewCsv("patient-1", csv)
        assertEquals(3, imported)

        val list = db.getReadingsBetween("patient-1", 0L, Long.MAX_VALUE)
        assertEquals(3, list.size)
        assertEquals(118.0, list[0].numericValue, 0.1)
        assertEquals(122.0, list[1].numericValue, 0.1)
        assertEquals(125.0, list[2].numericValue, 0.1)
    }

    @Test
    fun testImportLibreViewCsv_openGlucoFormat() {
        val csv = """
            Timestamp,Glucosa (mg/dL),Tendencia,Estado Clinico
            "2026-09-22 14:00:00",105,"Estable","En Rango"
            "2026-09-22 14:15:00",112,"Estable","En Rango"
        """.trimIndent()

        val imported = db.importLibreViewCsv("patient-1", csv)
        assertEquals(2, imported)

        val list = db.getReadingsBetween("patient-1", 0L, Long.MAX_VALUE)
        assertEquals(2, list.size)
    }

    @Test
    fun testPeriodSummaryCalculation_boundaryFloats() {
        val readings = listOf(
            createReading(53.99, 1, 0, 0),  // VERY_LOW (<54)
            createReading(54.0, 2, 0, 0),   // LOW (>=54 && <70)
            createReading(69.995, 3, 0, 0), // LOW (>=54 && <70)
            createReading(70.0, 4, 0, 0),   // IN_RANGE (>=70 && <=180)
            createReading(180.0, 5, 0, 0),  // IN_RANGE (>=70 && <=180)
            createReading(180.005, 6, 0, 0),// HIGH (>180 && <=250)
            createReading(250.0, 7, 0, 0),  // HIGH (>180 && <=250)
            createReading(250.01, 8, 0, 0)  // VERY_HIGH (>250)
        )

        db.insertReadings(readings, "patient-1")
        val summary = db.getPeriodSummary("patient-1", 7)

        assertEquals(8, summary.totalCount)
        assertEquals(1, summary.veryLowCount)
        assertEquals(2, summary.lowCount)
        assertEquals(2, summary.inRangeCount)
        assertEquals(2, summary.highCount)
        assertEquals(1, summary.veryHighCount)
    }

    @Test
    fun testImportLibreViewCsv_europeanSemicolonAndCommaFormat() {
        val csv = """
            Dispositivo;Número de serie;Hora del dispositivo;Tipo de registro;Historial de glucosa (mmol/L);Glucosa escaneada (mmol/L)
            FreeStyle Libre 3;0M001A8934;22-09-2026 10:00;0;5,8;
            FreeStyle Libre 3;0M001A8934;22-09-2026 10:15;0;6,2;
            FreeStyle Libre 3;0M001A8934;22-09-2026 10:20;1;;7,0
        """.trimIndent()

        val imported = db.importLibreViewCsv("patient-1", csv)
        assertEquals(3, imported)

        val list = db.getReadingsBetween("patient-1", 0L, Long.MAX_VALUE)
        assertEquals(3, list.size)
        // 5.8 * 18.0182 = 104.5 mg/dL
        assertEquals(104.5, list[0].numericValue, 0.5)
        // 6.2 * 18.0182 = 111.7 mg/dL
        assertEquals(111.7, list[1].numericValue, 0.5)
        // 7.0 * 18.0182 = 126.1 mg/dL
        assertEquals(126.1, list[2].numericValue, 0.5)
        // Ensure timestamp parses to positive epoch
        assertTrue(list[0].getEpochMillis() > 0L)
    }

    @Test
    fun testPurgeOldReadings() {
        val oldReading = GlucoseMeasurement(
            timestamp = "1/1/2020 10:00:00 AM",
            valueInMgPerDl = 110.0
        )
        val newReading = GlucoseMeasurement(
            timestamp = "9/22/2026 10:00:00 AM",
            valueInMgPerDl = 120.0
        )

        db.insertReadings(listOf(oldReading, newReading), "patient-1")
        val purged = db.purgeOldReadings("patient-1", 90)
        assertEquals(1, purged)

        val remaining = db.getReadingsBetween("patient-1", 0L, Long.MAX_VALUE)
        assertEquals(1, remaining.size)
        assertEquals(120.0, remaining[0].numericValue, 0.1)
    }

    @Test
    fun testFlowEmission() = runBlocking {
        val flow = db.getReadingsFlow("patient-1", 90)
        val initial = flow.first()
        assertTrue(initial.isEmpty())

        db.insertReadings(listOf(createReading(115.0, 10, 0, 0)), "patient-1")
        val updated = flow.first()
        assertEquals(1, updated.size)
    }

    @Test
    fun testImportLibreViewCsv_officialSpanishHeadersWithBom() {
        val csv = "\uFEFF" + """
            Identificador del dispositivo;Número de serie;Marca de tiempo del dispositivo;Tipo de registro;Glucosa histórica (mg/dL);Glucosa escaneada (mg/dL)
            FreeStyle Libre 3;0M001A8934;22-09-2026 08:30;0;108;
            FreeStyle Libre 3;0M001A8934;22-09-2026 08:45;0;112;
            FreeStyle Libre 3;0M001A8934;22-09-2026 09:00;1;;116
        """.trimIndent()

        val count = db.importLibreViewCsv("patient-es", csv)
        assertEquals(3, count)

        val readings = db.getReadingsBetween("patient-es", 0L, Long.MAX_VALUE)
        assertEquals(3, readings.size)
        assertEquals(108.0, readings[0].numericValue, 0.1)
        assertEquals(112.0, readings[1].numericValue, 0.1)
        assertEquals(116.0, readings[2].numericValue, 0.1)
    }

    @Test
    fun testFlowEmission_withHistoricalDataOlderThanCurrentTime() = runBlocking {
        val oldReading = GlucoseMeasurement(
            timestamp = "1/1/2025 10:00:00 AM",
            valueInMgPerDl = 125.0
        )
        db.insertReadings(listOf(oldReading), "patient-hist")

        val flow = db.getReadingsFlow("patient-hist", 90)
        val emitted = flow.first()
        assertEquals(1, emitted.size)
        assertEquals(125.0, emitted[0].numericValue, 0.1)
    }
}

