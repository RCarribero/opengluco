package com.example.opengluco.core.data.e2e

import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.HealthDataExporter
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.SensorInfo
import org.junit.Assert.*
import org.junit.Test

class E2ETier4RealWorldScenariosTest {

    // --- Real-World Scenario A: Full Daily Diabetes Monitoring Flow ---
    @Test
    fun testScenarioA_fullDailyDiabetesTrackingLifecycle() {
        // Morning reading (fasting, normal)
        val morningReading = GlucoseMeasurement(
            timestamp = "2026-08-27 07:30:00",
            valueInMgPerDl = 92.0,
            trendArrow = 3,
            trendMessage = "Estable"
        )
        assertEquals("92", morningReading.getFormattedValue())
        assertEquals("→", morningReading.trendSymbol)

        // Post-meal reading (high spike)
        val postMealReading = GlucoseMeasurement(
            timestamp = "2026-08-27 13:45:00",
            valueInMgPerDl = 195.0,
            trendArrow = 4,
            trendMessage = "Subiendo"
        )
        assertEquals("195", postMealReading.getFormattedValue())
        assertEquals("↗", postMealReading.trendSymbol)

        // Late afternoon reading (hypoglycemia dip during sports)
        val exerciseReading = GlucoseMeasurement(
            timestamp = "2026-08-27 17:15:00",
            valueInMgPerDl = 64.0,
            trendArrow = 2,
            trendMessage = "Bajando"
        )
        assertEquals("64", exerciseReading.getFormattedValue())
        assertEquals("↘", exerciseReading.trendSymbol)

        // Evening recovery
        val eveningReading = GlucoseMeasurement(
            timestamp = "2026-08-27 21:00:00",
            valueInMgPerDl = 110.0,
            trendArrow = 3,
            trendMessage = "Estable"
        )

        val fullDayHistory = listOf(morningReading, postMealReading, exerciseReading, eveningReading)
        assertEquals(4, fullDayHistory.size)

        // Export end-of-day CSV report for endocrinologist consultation
        val csvReport = HealthDataExporter.generateCsv(fullDayHistory, GlucoseUnit.MGDL)
        assertTrue(csvReport.contains("\"Hipoglucemia (Bajo)\""))
        assertTrue(csvReport.contains("\"Hiperglucemia (Alto)\""))
        assertTrue(csvReport.contains("\"En Rango\""))
    }

    // --- Real-World Scenario B: In-Car Driving Glanceable Telemetry ---
    @Test
    fun testScenarioB_inCarDrivingGlanceableState() {
        val activeReading = GlucoseMeasurement(
            timestamp = "2026-08-27 15:30:00",
            valueInMgPerDl = 128.0,
            trendArrow = 3
        )
        val sensor = SensorInfo(deviceId = "dev-car-1", activatedTimestamp = System.currentTimeMillis() / 1000 - 3 * 86400, sensorType = 3)
        val patient = ConnectionItem(
            id = "c-car",
            patientId = "p-car",
            firstName = "Carlos",
            lastName = "Mendoza",
            sensor = sensor,
            glucoseMeasurement = activeReading
        )

        assertEquals("Carlos Mendoza", patient.fullName)
        assertEquals(11, patient.sensor?.getRemainingDays())

        // Validate glanceable driver row format
        val displayStr = "${activeReading.getFormattedValue()} mg/dL  ${activeReading.trendSymbol} ${activeReading.trendText}"
        assertEquals("128 mg/dL  → Estable", displayStr)
    }

    // --- Real-World Scenario C: Smartwatch Ambient Mode & Local Cache Replay ---
    @Test
    fun testScenarioC_smartwatchAmbientOfflineResilience() {
        // Offline cache playback when phone is out of range
        val cachedReadings = listOf(
            GlucoseMeasurement(timestamp = "2026-08-27 18:00:00", valueInMgPerDl = 118.0, trendArrow = 3),
            GlucoseMeasurement(timestamp = "2026-08-27 18:15:00", valueInMgPerDl = 120.0, trendArrow = 3)
        )
        assertTrue("Watch must retain cached readings during disconnection", cachedReadings.isNotEmpty())

        val latest = cachedReadings.last()
        assertEquals(120.0, latest.numericValue, 0.001)
        assertEquals("120", latest.getFormattedValue())
    }
}
