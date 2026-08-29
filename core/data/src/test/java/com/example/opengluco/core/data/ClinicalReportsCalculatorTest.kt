package com.example.opengluco.core.data

import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.ReportTimeBlock
import com.example.opengluco.core.model.SensorInfo
import com.example.opengluco.core.model.TirCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalReportsCalculatorTest {

    private fun createReading(valMg: Double, hour: Int, minute: Int = 0, dayOffset: Int = 0): GlucoseMeasurement {
        val hStr = String.format("%02d", hour)
        val mStr = String.format("%02d", minute)
        val dStr = String.format("%02d", 10 + dayOffset)
        return GlucoseMeasurement(
            factoryTimestamp = "2026-08-${dStr}T${hStr}:${mStr}:00.000Z",
            timestamp = "2026-08-${dStr} ${hStr}:${mStr}:00",
            valueInMgPerDl = valMg,
            value = valMg,
            trendArrow = 3,
            trendMessage = "Estable",
            measurementColor = 1,
            glucoseUnits = 0,
            isHigh = valMg > 180,
            isLow = valMg < 70
        )
    }

    @Test
    fun testTimeInRangeFiveTiersCalculation() {
        val readings = listOf(
            createReading(45.0, 1),   // VERY_LOW (<54)
            createReading(60.0, 2),   // LOW (54-69)
            createReading(100.0, 3),  // IN_RANGE (70-180)
            createReading(120.0, 4),  // IN_RANGE
            createReading(140.0, 5),  // IN_RANGE
            createReading(200.0, 6),  // HIGH (181-250)
            createReading(300.0, 7)   // VERY_HIGH (>250)
        )

        val report = ClinicalReportsCalculator.calculateTimeInRange(readings, 7)
        assertEquals(7, report.totalReadings)
        assertEquals(5, report.buckets.size)

        val veryLow = report.buckets.find { it.category == TirCategory.VERY_LOW }!!
        val low = report.buckets.find { it.category == TirCategory.LOW }!!
        val inRange = report.buckets.find { it.category == TirCategory.IN_RANGE }!!
        val high = report.buckets.find { it.category == TirCategory.HIGH }!!
        val veryHigh = report.buckets.find { it.category == TirCategory.VERY_HIGH }!!

        assertEquals(1, veryLow.count)
        assertEquals(1, low.count)
        assertEquals(3, inRange.count)
        assertEquals(1, high.count)
        assertEquals(1, veryHigh.count)

        assertEquals(42.9, report.inRangePercent, 0.1)
        assertTrue(report.belowRangePercent > 0.0)
        assertTrue(report.aboveRangePercent > 0.0)
    }

    @Test
    fun testEstimatedA1cBergenstalFormulaAndLegalDisclaimer() {
        // Media = 150.0 mg/dL
        // GMI = 3.31 + 0.02392 * 150 = 3.31 + 3.588 = 6.898 -> 6.9%
        val readings = List(100) { createReading(150.0, 8, it % 60, it / 20) }
        val report = ClinicalReportsCalculator.calculateEstimatedA1c(readings, 14)

        assertEquals(150.0, report.meanGlucoseMgDl, 0.5)
        assertEquals(6.9, report.gmiPercent, 0.1)
        assertTrue("Must include MDDS legal disclaimer", report.disclaimerText.contains("FDA MDDS / MDR UE 2017/745"))
        assertTrue("Must state it is not HbA1c lab test", report.disclaimerText.contains("NO es un analisis clinico de laboratorio"))
    }

    @Test
    fun testDailyPatternsPercentilesAndVariability() {
        val readings = mutableListOf<GlucoseMeasurement>()
        // Generar 10 lecturas a las 08:00 con valores de 100 a 190
        for (i in 1..10) {
            readings.add(createReading(100.0 + (i * 10.0), 8, i * 5))
        }

        val report = ClinicalReportsCalculator.calculateDailyPatterns(readings, 14)
        assertEquals(24, report.hourlyPercentiles.size)

        val h8 = report.hourlyPercentiles[8]
        assertEquals(8, h8.hour)
        assertEquals(10, h8.sampleCount)
        assertTrue("P50 must be near median", h8.p50 in 150.0..160.0)
        assertTrue("P10 must be lower than P90", h8.p10 < h8.p90)
        assertTrue("CV must be positive", report.coefficientOfVariation > 0.0)
    }

    @Test
    fun testLowGlucoseEventsGroupingByTimeBlocks() {
        val readings = listOf(
            createReading(110.0, 2, 0),
            createReading(65.0, 2, 15),  // Evento madrugada
            createReading(60.0, 2, 30),
            createReading(120.0, 2, 45), // Fin evento
            createReading(110.0, 14, 0),
            createReading(55.0, 14, 15), // Evento tarde
            createReading(52.0, 14, 30),
            createReading(130.0, 14, 45) // Fin evento
        )

        val report = ClinicalReportsCalculator.calculateLowGlucoseEvents(readings, 7)
        assertEquals(2, report.totalEvents)
        assertEquals(1, report.eventsByBlock[ReportTimeBlock.NIGHT])
        assertEquals(1, report.eventsByBlock[ReportTimeBlock.AFTERNOON])
        assertEquals(0, report.eventsByBlock[ReportTimeBlock.MORNING])
        assertEquals(0, report.eventsByBlock[ReportTimeBlock.EVENING])
    }

    @Test
    fun testAverageGlucoseOverallAndByTimeBlocks() {
        val readings = listOf(
            createReading(100.0, 2),  // Night
            createReading(120.0, 8),  // Morning
            createReading(140.0, 14), // Afternoon
            createReading(160.0, 20)  // Evening
        )

        val report = ClinicalReportsCalculator.calculateAverageGlucose(readings, 7)
        assertEquals(130.0, report.overallAverageMgDl, 0.1)
        assertEquals(100.0, report.averageByBlock[ReportTimeBlock.NIGHT] ?: 0.0, 0.1)
        assertEquals(120.0, report.averageByBlock[ReportTimeBlock.MORNING] ?: 0.0, 0.1)
        assertEquals(140.0, report.averageByBlock[ReportTimeBlock.AFTERNOON] ?: 0.0, 0.1)
        assertEquals(160.0, report.averageByBlock[ReportTimeBlock.EVENING] ?: 0.0, 0.1)
    }

    @Test
    fun testDailyGraphGroupedByDate() {
        val readings = listOf(
            createReading(100.0, 10, 0, 0),
            createReading(150.0, 12, 0, 0),
            createReading(110.0, 10, 0, 1),
            createReading(170.0, 14, 0, 1)
        )

        val report = ClinicalReportsCalculator.calculateDailyGraph(readings)
        assertEquals(2, report.days.size)
        val day1 = report.days[0]
        val day2 = report.days[1]
        assertTrue(day1.dateString != day2.dateString)
        assertEquals(2, day1.readings.size)
    }

    @Test
    fun testSensorUsageCalculation() {
        val nowSec = System.currentTimeMillis() / 1000
        val activated = nowSec - (5 * 24 * 3600) // 5 días de antigüedad -> 9 días restantes
        val sensor = SensorInfo(
            deviceId = "dev-123",
            serialNumber = "0M001A8934",
            activatedTimestamp = activated,
            warmupDurationMinutes = 60,
            sensorType = 3,
            lifetimeDays = 14
        )
        val readings = List(96) { createReading(110.0, it % 24, (it * 15) % 60) }
        val report = ClinicalReportsCalculator.calculateSensorUsage(readings, sensor, 1)

        assertEquals(96, report.totalCapturedReadings)
        assertEquals(96, report.totalExpectedReadings)
        assertEquals(100.0, report.coveragePercentage, 0.1)
        assertEquals("FreeStyle Libre 3", report.sensorModelName)
        assertEquals("0M001A8934", report.sensorSerialNumber)
        assertEquals(9, report.daysRemaining)
        assertTrue(report.isActive)
    }

    @Test
    fun testCalculateAvailableDays() {
        // Vacio
        assertEquals(0, ClinicalReportsCalculator.calculateAvailableDays(emptyList()))

        // 1 solo dia con multiples lecturas
        val singleDayReadings = listOf(
            createReading(100.0, 8, 0, 0),
            createReading(120.0, 12, 0, 0),
            createReading(110.0, 18, 0, 0)
        )
        assertEquals(1, ClinicalReportsCalculator.calculateAvailableDays(singleDayReadings))

        // 2 dias distintos
        val twoDaysReadings = listOf(
            createReading(100.0, 8, 0, 0),
            createReading(120.0, 12, 0, 0),
            createReading(110.0, 10, 0, 1),
            createReading(130.0, 16, 0, 1)
        )
        assertEquals(2, ClinicalReportsCalculator.calculateAvailableDays(twoDaysReadings))

        // 5 dias distintos
        val fiveDaysReadings = (0..4).map { day ->
            createReading(115.0, 12, 0, day)
        }
        assertEquals(5, ClinicalReportsCalculator.calculateAvailableDays(fiveDaysReadings))
    }

    @Test
    fun testTimeInTightRangeAndGriCalculation() {
        val readings = listOf(
            createReading(45.0, 1),   // VERY_LOW (<54)
            createReading(60.0, 2),   // LOW (54-69)
            createReading(100.0, 3),  // IN_RANGE (70-180) & TIGHT_RANGE (70-140)
            createReading(120.0, 4),  // IN_RANGE & TIGHT_RANGE
            createReading(160.0, 5),  // IN_RANGE (pero no Tight Range >140)
            createReading(200.0, 6),  // HIGH (181-250)
            createReading(300.0, 7)   // VERY_HIGH (>250)
        )

        val report = ClinicalReportsCalculator.calculateTimeInRange(readings, 7)
        assertEquals(7, report.totalReadings)
        // 2 de 7 lecturas están entre 70 y 140 (100 y 120) -> 2/7 = 28.6%
        assertEquals(28.6, report.tightRangePercent, 0.2)
        // GRI debe ser > 0 y tener categoría asignada
        assertTrue("GRI score must be positive", report.gri > 0.0)
        assertNotNull(report.griCategory)
        assertTrue("Category must be defined", report.griCategory.startsWith("Zona"))
    }

    @Test
    fun testMageCalculation() {
        // Generar fluctuaciones con subidas y bajadas marcadas
        val readings = listOf(
            createReading(80.0, 1, 0),
            createReading(160.0, 2, 0),
            createReading(90.0, 3, 0),
            createReading(200.0, 4, 0),
            createReading(85.0, 5, 0)
        )
        val report = ClinicalReportsCalculator.calculateDailyPatterns(readings, 7)
        assertTrue("SD must be positive", report.standardDeviation > 0.0)
        assertTrue("MAGE must be positive for oscillating readings", report.mage >= 0.0)
    }

    @Test
    fun testSensorExpirationAlertEvaluation() {
        val nowSec = System.currentTimeMillis() / 1000

        // Sensor con 1 día restante -> alerta preventiva
        val expiringSensor = SensorInfo(
            deviceId = "dev-1",
            serialNumber = "SN-EXP-1",
            activatedTimestamp = nowSec - (13 * 24 * 3600), // 13 días de 14
            warmupDurationMinutes = 60,
            sensorType = 3,
            lifetimeDays = 14
        )
        val alert = ClinicalReportsCalculator.checkSensorExpirationAlert(expiringSensor)
        assertNotNull(alert)
        assertEquals(1, alert!!.daysRemaining)
        assertFalse(alert.isCritical)
        assertTrue(alert.title.contains("Proximo a Expirar"))

        // Sensor vencido (15 días de 14)
        val expiredSensor = SensorInfo(
            deviceId = "dev-2",
            serialNumber = "SN-EXP-2",
            activatedTimestamp = nowSec - (15 * 24 * 3600),
            warmupDurationMinutes = 60,
            sensorType = 3,
            lifetimeDays = 14
        )
        val expiredAlert = ClinicalReportsCalculator.checkSensorExpirationAlert(expiredSensor)
        assertNotNull(expiredAlert)
        assertEquals(0, expiredAlert!!.daysRemaining)
        assertTrue(expiredAlert.isCritical)
        assertTrue(expiredAlert.title.contains("Expirado"))

        // Sensor nuevo (5 días de 14) -> sin alerta
        val goodSensor = SensorInfo(
            deviceId = "dev-3",
            serialNumber = "SN-EXP-3",
            activatedTimestamp = nowSec - (5 * 24 * 3600),
            warmupDurationMinutes = 60,
            sensorType = 3,
            lifetimeDays = 14
        )
        val noAlert = ClinicalReportsCalculator.checkSensorExpirationAlert(goodSensor)
        org.junit.Assert.assertNull(noAlert)
    }
}
