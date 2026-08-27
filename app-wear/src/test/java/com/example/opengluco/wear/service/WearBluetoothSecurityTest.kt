package com.example.opengluco.wear.service

import com.example.opengluco.core.data.KeystoreCryptoHelper
import com.example.opengluco.core.model.GlucoseMeasurement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WearBluetoothSecurityTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun normalizeMac(mac: String): String {
        return mac.trim().replace("-", ":").uppercase()
    }

    private fun verifyMacMatch(trustedMac: String, incomingMac: String): Boolean {
        if (incomingMac.isBlank() || trustedMac.isBlank()) return false
        return normalizeMac(trustedMac) == normalizeMac(incomingMac)
    }

    @Test
    fun testMacVerification_exactMatchSucceeds() {
        val trusted = "AA:BB:CC:11:22:33"
        val incoming = "AA:BB:CC:11:22:33"
        assertTrue(verifyMacMatch(trusted, incoming))
    }

    @Test
    fun testMacVerification_caseAndDelimiterNormalization() {
        val trusted = "aa:bb:cc:11:22:33"
        val incomingColon = "AA:BB:CC:11:22:33"
        val incomingHyphen = "aa-bb-cc-11-22-33"

        assertTrue(verifyMacMatch(trusted, incomingColon))
        assertTrue(verifyMacMatch(trusted, incomingHyphen))
    }

    @Test
    fun testMacVerification_unauthorizedDeviceRejected() {
        val trusted = "AA:BB:CC:11:22:33"
        val attackerMac = "99:88:77:66:55:44"

        assertFalse(verifyMacMatch(trusted, attackerMac))
    }

    @Test
    fun testMacVerification_emptyOrBlankRejected() {
        val trusted = "AA:BB:CC:11:22:33"
        assertFalse(verifyMacMatch(trusted, ""))
        assertFalse(verifyMacMatch("", "AA:BB:CC:11:22:33"))
        assertFalse(verifyMacMatch("", ""))
    }

    @Test
    fun testStreamPayload_glucoseMeasurementSerializationRoundtrip() {
        val measurement = GlucoseMeasurement(
            factoryTimestamp = "2026-08-27T23:00:00Z",
            timestamp = "2026-08-27 23:00:00",
            valueInMgPerDl = 135.0,
            value = 135.0,
            trendArrow = 3,
            trendMessage = "Estable",
            isHigh = false,
            isLow = false
        )

        val rawJson = json.encodeToString(measurement)
        val keyHex = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"
        val ivHex = "000102030405060708090a0b"
        val encryptedHex = com.example.opengluco.core.data.QrAuthHelper.encryptAesGcm(rawJson, keyHex, ivHex)

        assertTrue(encryptedHex.isNotEmpty())

        val decryptedJson = com.example.opengluco.core.data.QrAuthHelper.decryptAesGcm(encryptedHex, keyHex, ivHex)
        val parsed = json.decodeFromString<GlucoseMeasurement>(decryptedJson)

        assertNotNull(parsed)
        assertEquals(135.0, parsed.numericValue, 0.001)
        assertEquals("Estable", parsed.trendText)
        assertEquals(3, parsed.trendArrow)
    }

    @Test
    fun testRfcommUuid_standardSppCompliance() {
        assertEquals("00001101-0000-1000-8000-00805f9b34fb", WearBluetoothRfcommService.OPENGLUCO_RFCOMM_UUID.toString().lowercase())
    }
}
