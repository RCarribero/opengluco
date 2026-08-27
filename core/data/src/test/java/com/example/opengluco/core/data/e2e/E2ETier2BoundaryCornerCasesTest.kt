package com.example.opengluco.core.data.e2e

import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.HealthDataExporter
import com.example.opengluco.core.data.KeystoreCryptoHelper
import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.SensorInfo
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class E2ETier2BoundaryCornerCasesTest {

    // --- Boundary Case 1: Extreme Hypoglycemia (<= 55 mg/dL) ---
    @Test
    fun testUrgentHypoglycemiaThreshold() {
        val urgentM = GlucoseMeasurement(valueInMgPerDl = 54.0, trendArrow = 1)
        assertEquals(54.0, urgentM.numericValue, 0.001)
        assertEquals("↓", urgentM.trendSymbol)
        assertEquals("Cayendo rápido", urgentM.trendText)
        assertEquals("54", urgentM.getFormattedValue(isMmol = false))
        assertEquals("3.0", urgentM.getFormattedValue(isMmol = true))
    }

    // --- Boundary Case 2: Extreme Hyperglycemia (>= 250 mg/dL) ---
    @Test
    fun testVeryHighHyperglycemiaThreshold() {
        val highM = GlucoseMeasurement(valueInMgPerDl = 350.0, trendArrow = 5)
        assertEquals(350.0, highM.numericValue, 0.001)
        assertEquals("↑", highM.trendSymbol)
        assertEquals("Subiendo rápido", highM.trendText)
        assertEquals("350", highM.getFormattedValue(isMmol = false))
        assertEquals("19.4", highM.getFormattedValue(isMmol = true)) // 350 / 18.0182 = 19.42 -> 19.4
    }

    // --- Boundary Case 3: Empty & Corrupted Payloads in Crypto ---
    @Test
    fun testCorruptCryptoPayloads() {
        assertEquals("", KeystoreCryptoHelper.decrypt("ENC:"))
        assertEquals("", KeystoreCryptoHelper.decrypt("ENC:???invalid???"))
        assertEquals("", KeystoreCryptoHelper.decrypt(""))
        assertEquals("plain_text", KeystoreCryptoHelper.decrypt("plain_text"))
    }

    // --- Boundary Case 4: Missing Sensor Information ---
    @Test
    fun testMissingSensorInfoGracefulDegradation() {
        val sensorNull = SensorInfo(deviceId = null, activatedTimestamp = null, sensorType = null)
        assertNull(sensorNull.getRemainingDays())
        assertEquals("FreeStyle Libre Sensor", sensorNull.sensorModelName)

        val itemNoSensor = ConnectionItem(id = "c1", patientId = "p1", sensor = null)
        assertNull(itemNoSensor.sensor)
        assertEquals("Paciente", itemNoSensor.fullName)
    }

    // --- Boundary Case 5: Empty CSV Export Structure ---
    @Test
    fun testEmptyCsvExportMaintainsCompliance() {
        val csv = HealthDataExporter.generateCsv(emptyList(), GlucoseUnit.MGDL)
        assertNotNull(csv)
        assertTrue("CSV must contain valid header even if data is empty", csv.contains("Timestamp,Glucosa (mg/dL),Tendencia,Estado Clinico"))
        val lines = csv.trim().split("\n")
        assertEquals(1, lines.size)
    }

    // --- Boundary Case 6: Special Characters in Auth & QR Data ---
    @Test
    fun testSpecialCharactersInQrPayload() {
        val emailWithPlus = "patient+libre@sub.domain-clinic.com"
        val specialToken = "jwt.eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.special-token_with-dashes_and.dots"
        val json = QrAuthHelper.createSessionExchange("sess-1", emailWithPlus, specialToken, "uid-123")
        val parsed = QrAuthHelper.parseSessionExchange(json)
        assertNotNull(parsed)
        assertEquals(emailWithPlus, parsed!!.email)
        assertEquals(specialToken, parsed.token)
    }
}
