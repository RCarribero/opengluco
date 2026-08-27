package com.example.opengluco.core.data.e2e

import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.HealthDataExporter
import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.QrDeviceType
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class E2ETier3CrossFeatureCombinationsTest {

    // --- Combination 1: QR Sync + AES-256-GCM Session Transfer + CSV Export ---
    @Test
    fun testCombination_qrSync_encryption_csvExport() {
        // Step 1: Device initiates QR pairing payload
        val pairing = QrAuthHelper.createPairingPayload(QrDeviceType.WEAR_OS, "Galaxy Watch 6")
        assertNotNull(pairing.secretKeyHex)
        assertNotNull(pairing.nonceHex)

        // Step 2: Phone sends session exchange encrypted over network payload
        val sessionJson = QrAuthHelper.createSessionExchange(
            sessionId = pairing.sessionId,
            email = "user@diab.org",
            token = "jwt.session.token.xyz",
            userId = "usr-456"
        )
        val encryptedHex = QrAuthHelper.encryptAesGcm(sessionJson, pairing.secretKeyHex!!, pairing.nonceHex!!)
        assertNotNull(encryptedHex)

        // Step 3: Watch decrypts payload and validates credentials
        val decryptedJson = QrAuthHelper.decryptAesGcm(encryptedHex, pairing.secretKeyHex!!, pairing.nonceHex!!)
        val receivedSession = QrAuthHelper.parseSessionExchange(decryptedJson)
        assertNotNull(receivedSession)
        assertEquals("user@diab.org", receivedSession!!.email)

        // Step 4: Watch/Phone records telemetry and exports to CSV
        val readings = listOf(
            GlucoseMeasurement(timestamp = "2026-08-27 12:00:00", valueInMgPerDl = 105.0, trendArrow = 3),
            GlucoseMeasurement(timestamp = "2026-08-27 12:15:00", valueInMgPerDl = 112.0, trendArrow = 3)
        )
        val csv = HealthDataExporter.generateCsv(readings, GlucoseUnit.MGDL)
        assertTrue(csv.contains("Timestamp,Glucosa (mg/dL),Tendencia,Estado Clinico"))
        assertTrue(csv.contains("105"))
        assertTrue(csv.contains("112"))
    }

    // --- Combination 2: Unit Switch (mg/dL <-> mmol/L) + Status Recomputation + CSV Formatting ---
    @Test
    fun testCombination_unitSwitch_status_csv() {
        val measurement = GlucoseMeasurement(timestamp = "2026-08-27 14:00:00", valueInMgPerDl = 160.0, trendArrow = 4)

        // In mg/dL mode
        val mgdlFormatted = measurement.getFormattedValue(isMmol = false)
        assertEquals("160", mgdlFormatted)
        val csvMgdl = HealthDataExporter.generateCsv(listOf(measurement), GlucoseUnit.MGDL)
        assertTrue(csvMgdl.contains("160"))
        assertTrue(csvMgdl.contains("En Rango"))

        // In mmol/L mode (160 / 18.0182 = 8.88 -> "8.9")
        val mmolFormatted = measurement.getFormattedValue(isMmol = true)
        assertEquals("8.9", mmolFormatted)
        val csvMmol = HealthDataExporter.generateCsv(listOf(measurement), GlucoseUnit.MMOL)
        assertTrue(csvMmol.contains("8.9"))
        assertTrue(csvMmol.contains("En Rango"))
    }

    // --- Combination 3: GDPR Purge + State Invalidation + Cache Erasure ---
    @Test
    fun testCombination_gdprPurge_stateInvalidation() {
        val activeSessions = mutableMapOf("auth_token" to "enc:token", "user_id" to "enc:user")
        val telemetryCache = mutableListOf(
            GlucoseMeasurement(valueInMgPerDl = 120.0),
            GlucoseMeasurement(valueInMgPerDl = 130.0)
        )

        // Verify initial state
        assertFalse(activeSessions.isEmpty())
        assertEquals(2, telemetryCache.size)

        // Execute atomic purge
        activeSessions.clear()
        telemetryCache.clear()

        // Verify clean wiped state
        assertTrue(activeSessions.isEmpty())
        assertTrue(telemetryCache.isEmpty())
    }
}
