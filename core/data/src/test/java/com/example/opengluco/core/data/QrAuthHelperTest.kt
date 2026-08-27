package com.example.opengluco.core.data

import com.example.opengluco.core.model.QrDeviceType
import org.junit.Assert.*
import org.junit.Test
import javax.crypto.AEADBadTagException

class QrAuthHelperTest {

    private val validKeyHex = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f" // 32 bytes (256 bits)
    private val validIvHex = "000102030405060708090a0b" // 12 bytes (96 bits)

    @Test
    fun testAesGcm_encryptAndDecryptRoundtrip() {
        val originalText = "{\"email\":\"patient@test.com\",\"token\":\"jwt-secret-12345\"}"
        val cipherHex = QrAuthHelper.encryptAesGcm(originalText, validKeyHex, validIvHex)
        assertNotNull(cipherHex)
        assertTrue(cipherHex.isNotEmpty())

        val decryptedText = QrAuthHelper.decryptAesGcm(cipherHex, validKeyHex, validIvHex)
        assertEquals(originalText, decryptedText)
    }

    @Test
    fun testAesGcm_invalidTag_throwsException() {
        val originalText = "Sensitive Medical Telemetry"
        val cipherHex = QrAuthHelper.encryptAesGcm(originalText, validKeyHex, validIvHex)

        // Tamper with the last byte (auth tag)
        val tamperedCipherHex = if (cipherHex.endsWith("00")) {
            cipherHex.dropLast(2) + "ff"
        } else {
            cipherHex.dropLast(2) + "00"
        }

        try {
            QrAuthHelper.decryptAesGcm(tamperedCipherHex, validKeyHex, validIvHex)
            fail("Decryption of tampered ciphertext must throw AEADBadTagException")
        } catch (e: Exception) {
            // Expected
            assertTrue(e is AEADBadTagException || e.cause is AEADBadTagException)
        }
    }

    @Test
    fun testAesGcm_wrongKey_failsDecryption() {
        val originalText = "SuperSecretToken"
        val cipherHex = QrAuthHelper.encryptAesGcm(originalText, validKeyHex, validIvHex)

        val wrongKeyHex = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        try {
            QrAuthHelper.decryptAesGcm(cipherHex, wrongKeyHex, validIvHex)
            fail("Decryption with wrong key must fail")
        } catch (e: Exception) {
            // Expected tag mismatch
            assertTrue(true)
        }
    }

    @Test
    fun testCreatePairingPayload_generatesValidHexKeys() {
        val payload = QrAuthHelper.createPairingPayload(
            deviceType = QrDeviceType.WEAR_OS,
            deviceName = "Galaxy Watch 6 Classic",
            port = 8888
        )

        assertEquals(QrDeviceType.WEAR_OS, payload.deviceType)
        assertEquals("Galaxy Watch 6 Classic", payload.deviceName)
        assertEquals(8888, payload.port)
        assertNotNull(payload.secretKeyHex)
        assertEquals(64, payload.secretKeyHex!!.length) // 32 bytes * 2 = 64 hex chars
        assertNotNull(payload.nonceHex)
        assertEquals(24, payload.nonceHex!!.length) // 12 bytes * 2 = 24 hex chars
    }

    @Test
    fun testPairingPayloadSerializationRoundtrip() {
        val payload = QrAuthHelper.createPairingPayload(
            deviceType = QrDeviceType.ANDROID_AUTO,
            deviceName = "Vehicle Head Unit",
            port = 8888
        )
        val jsonStr = QrAuthHelper.serializePairingPayload(payload)
        val parsed = QrAuthHelper.parsePairingPayload(jsonStr)

        assertNotNull(parsed)
        assertEquals(payload.sessionId, parsed!!.sessionId)
        assertEquals(payload.deviceType, parsed.deviceType)
        assertEquals(payload.secretKeyHex, parsed.secretKeyHex)
    }

    @Test
    fun testSessionExchangeSerializationRoundtrip() {
        val jsonStr = QrAuthHelper.createSessionExchange(
            sessionId = "sess-100",
            email = "user@health.io",
            token = "jwt.token.here",
            userId = "user-123",
            phoneBluetoothMac = "AA:BB:CC:11:22:33"
        )
        val parsed = QrAuthHelper.parseSessionExchange(jsonStr)

        assertNotNull(parsed)
        assertEquals("sess-100", parsed!!.sessionId)
        assertEquals("user@health.io", parsed.email)
        assertEquals("jwt.token.here", parsed.token)
        assertEquals("user-123", parsed.userId)
        assertEquals("AA:BB:CC:11:22:33", parsed.phoneBluetoothMac)
    }

    @Test
    fun testGenerateVerificationCode_formatAndLength() {
        val code = QrAuthHelper.generateVerificationCode("seed-12345")
        assertNotNull(code)
        assertEquals(7, code.length) // "123 456" = 7 chars
        assertTrue(code.matches(Regex("^\\d{3} \\d{3}$")))

        val randomCode = QrAuthHelper.generateVerificationCode()
        assertEquals(7, randomCode.length)
        assertTrue(randomCode.matches(Regex("^\\d{3} \\d{3}$")))
    }

    @Test
    fun testParsePairingPayload_malformedJsonReturnsNull() {
        val invalid1 = "{not valid json}"
        val invalid2 = ""
        assertNull(QrAuthHelper.parsePairingPayload(invalid1))
        assertNull(QrAuthHelper.parsePairingPayload(invalid2))
    }
}
