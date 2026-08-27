package com.example.opengluco.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class QrAuthModelsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testQrPairingPayload_defaultValues() {
        val payload = QrPairingPayload(
            sessionId = "test-session-123",
            deviceType = QrDeviceType.WEAR_OS,
            deviceName = "Galaxy Watch 6"
        )
        assertEquals("OPENGLUCO_QR_AUTH_V2_ENCRYPTED", payload.protocol)
        assertEquals(8888, payload.port)
        assertEquals(QrDeviceType.WEAR_OS, payload.deviceType)
        assertTrue(payload.timestamp > 0L)
    }

    @Test
    fun testQrPairingPayload_serializationRoundtrip() {
        val payload = QrPairingPayload(
            sessionId = "sess-abc",
            deviceType = QrDeviceType.ANDROID_AUTO,
            deviceName = "Android Auto Head Unit",
            ip = "192.168.1.150",
            port = 9090,
            secretKeyHex = "0123456789abcdef0123456789abcdef",
            nonceHex = "abcdef012345"
        )
        val serialized = json.encodeToString(payload)
        assertTrue(serialized.contains("ANDROID_AUTO") || serialized.contains("android_auto"))
        assertTrue(serialized.contains("sess-abc"))

        val deserialized = json.decodeFromString<QrPairingPayload>(serialized)
        assertEquals(payload.sessionId, deserialized.sessionId)
        assertEquals(payload.deviceType, deserialized.deviceType)
        assertEquals(payload.deviceName, deserialized.deviceName)
        assertEquals(payload.ip, deserialized.ip)
        assertEquals(payload.port, deserialized.port)
        assertEquals(payload.secretKeyHex, deserialized.secretKeyHex)
        assertEquals(payload.nonceHex, deserialized.nonceHex)
    }

    @Test
    fun testQrSessionExchange_serializationRoundtrip() {
        val exchange = QrSessionExchange(
            sessionId = "sess-xyz",
            email = "patient@domain.org",
            token = "jwt-super-secret-token",
            userId = "user-uuid-999",
            region = "eu"
        )
        val serialized = json.encodeToString(exchange)
        assertTrue(serialized.contains("patient@domain.org"))

        val deserialized = json.decodeFromString<QrSessionExchange>(serialized)
        assertEquals("sess-xyz", deserialized.sessionId)
        assertEquals("patient@domain.org", deserialized.email)
        assertEquals("jwt-super-secret-token", deserialized.token)
        assertEquals("user-uuid-999", deserialized.userId)
        assertEquals("eu", deserialized.region)
    }

    @Test
    fun testQrEncryptedPayload_serializationRoundtrip() {
        val encrypted = QrEncryptedPayload(
            sessionId = "sess-enc-1",
            encryptedDataHex = "aabbccddeeff0011223344",
            ivHex = "0102030405060708090a0b0c"
        )
        val serialized = json.encodeToString(encrypted)
        val deserialized = json.decodeFromString<QrEncryptedPayload>(serialized)
        assertEquals("sess-enc-1", deserialized.sessionId)
        assertEquals("aabbccddeeff0011223344", deserialized.encryptedDataHex)
        assertEquals("0102030405060708090a0b0c", deserialized.ivHex)
    }
}
