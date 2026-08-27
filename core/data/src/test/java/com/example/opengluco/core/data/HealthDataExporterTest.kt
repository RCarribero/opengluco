package com.example.opengluco.core.data

import com.example.opengluco.core.model.GlucoseMeasurement
import org.junit.Assert.*
import org.junit.Test

class HealthDataExporterTest {

    @Test
    fun testGenerateCsv_mgdlHeaderAndRows() {
        val readings = listOf(
            GlucoseMeasurement(
                timestamp = "2026-08-27 08:00:00",
                valueInMgPerDl = 110.0,
                trendArrow = 3
            ),
            GlucoseMeasurement(
                timestamp = "2026-08-27 09:00:00",
                valueInMgPerDl = 65.0,
                trendArrow = 2
            ),
            GlucoseMeasurement(
                timestamp = "2026-08-27 10:00:00",
                valueInMgPerDl = 210.0,
                trendArrow = 5
            )
        )

        val csv = HealthDataExporter.generateCsv(readings, GlucoseUnit.MGDL)
        val lines = csv.trimEnd().split("\n")

        assertEquals("Timestamp,Glucosa (mg/dL),Tendencia,Estado Clinico", lines[0])
        assertEquals(4, lines.size) // Header + 3 rows

        assertEquals("\"2026-08-27 08:00:00\",110,\"Estable\",\"En Rango\"", lines[1])
        assertEquals("\"2026-08-27 09:00:00\",65,\"Bajando\",\"Hipoglucemia (Bajo)\"", lines[2])
        assertEquals("\"2026-08-27 10:00:00\",210,\"Subiendo rápido\",\"Hiperglucemia (Alto)\"", lines[3])
    }

    @Test
    fun testGenerateCsv_mmolHeaderAndRows() {
        val readings = listOf(
            GlucoseMeasurement(
                timestamp = "2026-08-27 08:00:00",
                valueInMgPerDl = 180.0, // 180 / 18.0182 = 9.9898 -> 10.0
                trendArrow = 3
            ),
            GlucoseMeasurement(
                timestamp = "2026-08-27 09:00:00",
                valueInMgPerDl = 54.0, // 54 / 18.0182 = 2.997 -> 3.0
                trendArrow = 1
            )
        )

        val csv = HealthDataExporter.generateCsv(readings, GlucoseUnit.MMOL)
        val lines = csv.trimEnd().split("\n")

        assertEquals("Timestamp,Glucosa (mmol/L),Tendencia,Estado Clinico", lines[0])
        assertEquals(3, lines.size)
        assertEquals("\"2026-08-27 08:00:00\",10.0,\"Estable\",\"En Rango\"", lines[1])
        assertEquals("\"2026-08-27 09:00:00\",3.0,\"Cayendo rápido\",\"Hipoglucemia (Bajo)\"", lines[2])
    }

    @Test
    fun testGenerateCsv_emptyList_producesValidHeaderOnly() {
        val csv = HealthDataExporter.generateCsv(emptyList(), GlucoseUnit.MGDL)
        assertEquals("Timestamp,Glucosa (mg/dL),Tendencia,Estado Clinico\n", csv)
    }

    @Test
    fun testGenerateCsv_sortingByTimestampAscending() {
        val r1 = GlucoseMeasurement(timestamp = "2026-08-27 12:00:00", valueInMgPerDl = 100.0, trendArrow = 3)
        val r2 = GlucoseMeasurement(timestamp = "2026-08-27 06:00:00", valueInMgPerDl = 120.0, trendArrow = 3)
        val r3 = GlucoseMeasurement(timestamp = "2026-08-27 09:00:00", valueInMgPerDl = 110.0, trendArrow = 3)

        val csv = HealthDataExporter.generateCsv(listOf(r1, r2, r3), GlucoseUnit.MGDL)
        val lines = csv.trimEnd().split("\n")

        assertTrue(lines[1].contains("2026-08-27 06:00:00"))
        assertTrue(lines[2].contains("2026-08-27 09:00:00"))
        assertTrue(lines[3].contains("2026-08-27 12:00:00"))
    }

    @Test
    fun testGenerateCsv_boundaryThresholds() {
        val rLowEdge = GlucoseMeasurement(timestamp = "2026-08-27 01:00:00", valueInMgPerDl = 69.9, trendArrow = 3)
        val rNormLow = GlucoseMeasurement(timestamp = "2026-08-27 02:00:00", valueInMgPerDl = 70.0, trendArrow = 3)
        val rNormHigh = GlucoseMeasurement(timestamp = "2026-08-27 03:00:00", valueInMgPerDl = 180.0, trendArrow = 3)
        val rHighEdge = GlucoseMeasurement(timestamp = "2026-08-27 04:00:00", valueInMgPerDl = 180.1, trendArrow = 3)

        val csv = HealthDataExporter.generateCsv(listOf(rLowEdge, rNormLow, rNormHigh, rHighEdge), GlucoseUnit.MGDL)
        val lines = csv.trimEnd().split("\n")

        assertTrue("69.9 should be Hipoglucemia", lines[1].contains("\"Hipoglucemia (Bajo)\""))
        assertTrue("70.0 should be En Rango", lines[2].contains("\"En Rango\""))
        assertTrue("180.0 should be En Rango", lines[3].contains("\"En Rango\""))
        assertTrue("180.1 should be Hiperglucemia", lines[4].contains("\"Hiperglucemia (Alto)\""))
    }
}
