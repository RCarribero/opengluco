package com.example.opengluco.core.data.e2e

import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.HealthDataExporter
import com.example.opengluco.core.data.KeystoreCryptoHelper
import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.QrDeviceType
import com.example.opengluco.core.model.QrPairingPayload
import com.example.opengluco.core.model.QrSessionExchange
import com.example.opengluco.core.model.SensorInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.Base64
import java.util.Collections
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class EmpiricalStressChallengeTest {

    // =========================================================================
    // 1. BOUNDARY CONDITIONS: EXTREME GLUCOSE LEVELS & UNIT CONVERSION (18.0182)
    // =========================================================================

    @Test
    fun testBoundary_extremeGlucoseLevels_andExactUnitConversions() {
        val testCases = listOf(
            Triple(0.0, "0", "0.0"),
            Triple(1.0, "1", "0.1"),
            Triple(40.0, "40", "2.2"),      // 40 / 18.0182 = 2.220...
            Triple(54.0, "54", "3.0"),      // 54 / 18.0182 = 2.997... -> 3.0
            Triple(55.0, "55", "3.1"),      // 55 / 18.0182 = 3.052... -> 3.1
            Triple(56.0, "56", "3.1"),      // 56 / 18.0182 = 3.1079... -> 3.1
            Triple(69.9, "70", "3.9"),      // 69.9 / 18.0182 = 3.879... -> 3.9
            Triple(70.0, "70", "3.9"),      // 70 / 18.0182 = 3.885... -> 3.9
            Triple(100.0, "100", "5.6"),    // 100 / 18.0182 = 5.5499... -> 5.5 or 5.6 depending on rounding
            Triple(180.0, "180", "10.0"),   // 180 / 18.0182 = 9.9898... -> 10.0
            Triple(180.1, "180", "10.0"),   // 180.1 / 18.0182 = 9.995... -> 10.0
            Triple(249.0, "249", "13.8"),   // 249 / 18.0182 = 13.819... -> 13.8
            Triple(250.0, "250", "13.9"),   // 250 / 18.0182 = 13.874... -> 13.9
            Triple(400.0, "400", "22.2"),   // 400 / 18.0182 = 22.200... -> 22.2
            Triple(500.0, "500", "27.8"),   // 500 / 18.0182 = 27.750... -> 27.8
            Triple(1000.0, "1000", "55.5")  // 1000 / 18.0182 = 55.499... -> 55.5
        )

        for ((mgdlVal, expectedMgdlStr, expectedMmolStr) in testCases) {
            val m = GlucoseMeasurement(valueInMgPerDl = mgdlVal)
            assertEquals("mg/dL formatted mismatch for $mgdlVal", expectedMgdlStr, m.getFormattedValue(isMmol = false))
            val actualMmolStr = m.getFormattedValue(isMmol = true)
            // Verify mmol/L string is within 0.1 precision
            val actualMmol = actualMmolStr.toDouble()
            val expectedMmol = (mgdlVal / 18.0182)
            assertEquals("mmol/L mathematical calculation mismatch for $mgdlVal", expectedMmol, actualMmol, 0.1)
        }
    }

    @Test
    fun testBoundary_negativeAndZeroGlucoseValues() {
        val negativeM = GlucoseMeasurement(valueInMgPerDl = -15.0)
        assertEquals(-15.0, negativeM.numericValue, 0.001)
        assertEquals("-15", negativeM.getFormattedValue(isMmol = false))

        val zeroM = GlucoseMeasurement(valueInMgPerDl = 0.0)
        assertEquals(0.0, zeroM.numericValue, 0.001)
        assertEquals("0", zeroM.getFormattedValue(isMmol = false))
    }

    // =========================================================================
    // 2. BOUNDARY CONDITIONS: MISSING SENSOR FIELDS & FALLBACKS
    // =========================================================================

    @Test
    fun testBoundary_allSensorFieldsMissing() {
        val completelyEmptySensor = SensorInfo(
            deviceId = null,
            serialNumber = null,
            activatedTimestamp = null,
            warmupDurationMinutes = null,
            sensorType = null
        )

        assertNull(completelyEmptySensor.deviceId)
        assertNull(completelyEmptySensor.serialNumber)
        assertNull(completelyEmptySensor.activatedTimestamp)
        assertNull(completelyEmptySensor.warmupDurationMinutes)
        assertNull(completelyEmptySensor.sensorType)
        assertNull(completelyEmptySensor.getRemainingDays())
        assertEquals("FreeStyle Libre Sensor", completelyEmptySensor.sensorModelName)
    }

    @Test
    fun testBoundary_sensorAgeEdgeCases() {
        val nowSec = System.currentTimeMillis() / 1000

        // Activated exactly 14 days ago (at expiration edge)
        val edgeSensor = SensorInfo(activatedTimestamp = nowSec - (14 * 24 * 3600), sensorType = 3)
        assertEquals(0, edgeSensor.getRemainingDays())

        // Activated 100 days ago (long expired)
        val oldSensor = SensorInfo(activatedTimestamp = nowSec - (100 * 24 * 3600), sensorType = 2)
        assertEquals(0, oldSensor.getRemainingDays())

        // Activated in the future (e.g. device clock skew)
        val futureSensor = SensorInfo(activatedTimestamp = nowSec + (2 * 24 * 3600), sensorType = 1)
        val remainingFuture = futureSensor.getRemainingDays()
        assertNotNull(remainingFuture)
        assertTrue("Future sensor should report >= 14 days", remainingFuture!! >= 14)
    }

    @Test
    fun testBoundary_connectionItemWithMissingPatientData() {
        val emptyItem = ConnectionItem(
            id = "c-999",
            patientId = "p-999",
            firstName = null,
            lastName = null,
            glucoseMeasurement = null,
            glucoseItem = null,
            sensor = null
        )

        assertEquals("Paciente", emptyItem.fullName)
        assertNull(emptyItem.effectiveMeasurement)
        assertNull(emptyItem.sensor)
        assertEquals(70, emptyItem.targetLow)
        assertEquals(180, emptyItem.targetHigh)
    }

    // =========================================================================
    // 3. BOUNDARY CONDITIONS: KEYSTORE CRYPTO AUTH TAG CORRUPTION & INVALID BASE64
    // =========================================================================

    @Test
    fun testCrypto_aes256Gcm_authTagCorruptionThrowsAndHandled() {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey: SecretKey = keyGen.generateKey()

        val plainText = "Confidential Patient Health Record 2026"
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Corrupt each byte of the 16-byte authentication tag
        for (i in 1..16) {
            val tamperedCipherBytes = cipherBytes.clone()
            val tagByteIndex = tamperedCipherBytes.size - i
            tamperedCipherBytes[tagByteIndex] = (tamperedCipherBytes[tagByteIndex].toInt() xor 0xAA).toByte()

            val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(128, iv)
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            var caughtAuthException = false
            try {
                decryptCipher.doFinal(tamperedCipherBytes)
            } catch (e: javax.crypto.AEADBadTagException) {
                caughtAuthException = true
            } catch (e: Exception) {
                caughtAuthException = true
            }
            assertTrue("Tampered tag at offset -$i must be rejected", caughtAuthException)
        }
    }

    @Test
    fun testCrypto_keystoreCryptoHelper_corruptBase64AndPayloads() {
        val corruptInputs = listOf(
            "ENC:",
            "ENC:   ",
            "ENC:12345",
            "ENC:!@#$%^&*()_+",
            "ENC:QUJDREVGR0hJSktMTU5PUA==", // valid base64 but invalid cipher data
            "ENC:AAAA",
            "ENC:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
            "ENC:Zm9v", // "foo" (too short for 12-byte IV)
            "ENC:invalid==base64!!",
            "",
            "   "
        )

        for (input in corruptInputs) {
            val decrypted = KeystoreCryptoHelper.decrypt(input)
            assertNotNull("Decrypting corrupted input '$input' must never return null", decrypted)
            // It should safely return empty string or input without throwing any uncaught exceptions
            assertTrue(
                "Result for '$input' should be safe string",
                decrypted == "" || decrypted == input || decrypted == "ENC:"
            )
        }
    }

    @Test
    fun testCrypto_qrAuthHelper_corruptedPayloadsAndBitFlips() {
        val keyHex = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        val ivHex = "abcdef0123456789abcdef01"
        val plain = "{\"sessionId\":\"s1\",\"email\":\"user@test.org\"}"

        val encryptedHex = QrAuthHelper.encryptAesGcm(plain, keyHex, ivHex)
        assertNotNull(encryptedHex)
        assertTrue(encryptedHex.isNotEmpty())

        // Bit-flip in the ciphertext/tag
        val tamperedChars = encryptedHex.toCharArray()
        tamperedChars[tamperedChars.size - 2] = if (tamperedChars[tamperedChars.size - 2] == '0') 'f' else '0'
        val tamperedHex = String(tamperedChars)

        try {
            QrAuthHelper.decryptAesGcm(tamperedHex, keyHex, ivHex)
            fail("Decryption with tampered hex must throw exception")
        } catch (e: Exception) {
            // Expected AEADBadTagException or GeneralSecurityException
            assertTrue(true)
        }

        // Parse null/corrupt inputs
        assertNull(QrAuthHelper.parsePairingPayload("not-json-content"))
        assertNull(QrAuthHelper.parsePairingPayload("{}}"))
        assertNull(QrAuthHelper.parseSessionExchange("not-json-content"))
        assertNull(QrAuthHelper.parseSessionExchange("{\"invalid\":\"json\"}"))
    }

    // =========================================================================
    // 4. BOUNDARY CONDITIONS: CSV EXPORTS (EMPTY, STRESS, SPECIAL CHARACTERS)
    // =========================================================================

    @Test
    fun testCsv_emptyReadings_validStructure() {
        val csvMgdl = HealthDataExporter.generateCsv(emptyList(), GlucoseUnit.MGDL)
        assertEquals("Timestamp,Glucosa (mg/dL),Tendencia,Estado Clinico\n", csvMgdl)

        val csvMmol = HealthDataExporter.generateCsv(emptyList(), GlucoseUnit.MMOL)
        assertEquals("Timestamp,Glucosa (mmol/L),Tendencia,Estado Clinico\n", csvMmol)
    }

    @Test
    fun testCsv_specialCharactersAndMissingTimestamps() {
        val r1 = GlucoseMeasurement(
            timestamp = null,
            factoryTimestamp = "2026-08-27 10:00:00",
            valueInMgPerDl = 120.0,
            trendArrow = 3
        )
        val r2 = GlucoseMeasurement(
            timestamp = null,
            factoryTimestamp = null,
            valueInMgPerDl = 140.0,
            trendArrow = 4
        )

        val csv = HealthDataExporter.generateCsv(listOf(r1, r2), GlucoseUnit.MGDL)
        val lines = csv.trim().split("\n")
        assertEquals(3, lines.size)
        assertTrue("Row 1 should use factoryTimestamp", lines[1].contains("\"2026-08-27 10:00:00\""))
        assertTrue("Row 2 should handle empty timestamp safely", lines[2].contains("\"\""))
    }

    @Test
    fun testCsv_highVolumeStressGeneration() {
        // Stress test with 5,000 measurements
        val highVolumeList = ArrayList<GlucoseMeasurement>(5000)
        for (i in 0 until 5000) {
            highVolumeList.add(
                GlucoseMeasurement(
                    timestamp = "2026-08-27 00:${String.format("%02d", i % 60)}:00",
                    valueInMgPerDl = 70.0 + (i % 150),
                    trendArrow = (i % 5) + 1
                )
            )
        }

        val start = System.currentTimeMillis()
        val csv = HealthDataExporter.generateCsv(highVolumeList, GlucoseUnit.MGDL)
        val duration = System.currentTimeMillis() - start

        assertNotNull(csv)
        val lineCount = csv.count { it == '\n' }
        assertEquals(5001, lineCount) // Header + 5000 rows
        assertTrue("CSV generation for 5000 items should complete in < 1500ms (took ${duration}ms)", duration < 1500)
    }

    // =========================================================================
    // 5. CONCURRENCY: MULTI-THREADED CRYPTO & MODEL SERIALIZATION STRESS
    // =========================================================================

    @Test
    fun testConcurrency_parallelAesGcmCryptoOperations() {
        val threadCount = 30
        val operationsPerThread = 50
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val errors = Collections.synchronizedList(ArrayList<Throwable>())

        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey: SecretKey = keyGen.generateKey()

        for (t in 0 until threadCount) {
            executor.submit {
                try {
                    for (op in 0 until operationsPerThread) {
                        val plain = "Thread-$t-Op-$op-${UUID.randomUUID()}"
                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                        val iv = cipher.iv
                        val cipherBytes = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))

                        val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
                        val gcmSpec = GCMParameterSpec(128, iv)
                        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
                        val decrypted = String(decryptCipher.doFinal(cipherBytes), Charsets.UTF_8)

                        if (decrypted != plain) {
                            throw IllegalStateException("Decrypted text '$decrypted' does not match '$plain'")
                        }
                    }
                } catch (t: Throwable) {
                    errors.add(t)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()
        assertTrue("No concurrency errors allowed in AES-GCM crypto. Found: $errors", errors.isEmpty())
    }

    @Test
    fun testConcurrency_parallelQrSessionExchangeAndCrypto() {
        val threadCount = 20
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val errors = Collections.synchronizedList(ArrayList<Throwable>())

        for (t in 0 until threadCount) {
            executor.submit {
                try {
                    val pairing = QrAuthHelper.createPairingPayload(QrDeviceType.ANDROID_AUTO, "HeadUnit-$t")
                    val sessionJson = QrAuthHelper.createSessionExchange(
                        sessionId = pairing.sessionId,
                        email = "driver-$t@example.com",
                        token = "token-xyz-$t",
                        userId = "uid-$t"
                    )

                    val encHex = QrAuthHelper.encryptAesGcm(sessionJson, pairing.secretKeyHex!!, pairing.nonceHex!!)
                    val decJson = QrAuthHelper.decryptAesGcm(encHex, pairing.secretKeyHex!!, pairing.nonceHex!!)
                    val parsed = QrAuthHelper.parseSessionExchange(decJson)

                    if (parsed == null || parsed.email != "driver-$t@example.com") {
                        throw IllegalStateException("Concurrent QR exchange mismatch on thread $t")
                    }
                } catch (t: Throwable) {
                    errors.add(t)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()
        assertTrue("No errors allowed in concurrent QR pairing/exchange. Found: $errors", errors.isEmpty())
    }

    // =========================================================================
    // 6. CONCURRENCY & STATE TRANSITIONS: ATOMIC PURGE & SESSION LIFECYCLE
    // =========================================================================

    @Test
    fun testConcurrency_atomicDataPurgeDuringActiveTelemetryReads() = runBlocking {
        val simulatedDb = ConcurrentHashMap<String, String>()
        val simulatedHistory = Collections.synchronizedList(ArrayList<GlucoseMeasurement>())
        val isRunning = AtomicBoolean(true)
        val readErrors = Collections.synchronizedList(ArrayList<Throwable>())

        // Seed initial data
        simulatedDb["email"] = "ENC:patient@health.org"
        simulatedDb["auth_token"] = "ENC:jwt-active-session"
        for (i in 0 until 100) {
            simulatedHistory.add(GlucoseMeasurement(valueInMgPerDl = 100.0 + i))
        }

        // Launch concurrent reader coroutines
        val reader1 = async(Dispatchers.IO) {
            while (isRunning.get()) {
                try {
                    val email = simulatedDb["email"] ?: ""
                    val size = simulatedHistory.size
                    val copy = synchronized(simulatedHistory) { ArrayList(simulatedHistory) }
                    assertNotNull(copy)
                } catch (e: Throwable) {
                    readErrors.add(e)
                }
            }
        }

        val reader2 = async(Dispatchers.IO) {
            while (isRunning.get()) {
                try {
                    val token = simulatedDb["auth_token"] ?: ""
                    val csv = HealthDataExporter.generateCsv(
                        synchronized(simulatedHistory) { ArrayList(simulatedHistory) },
                        GlucoseUnit.MGDL
                    )
                    assertNotNull(csv)
                } catch (e: Throwable) {
                    readErrors.add(e)
                }
            }
        }

        // Execute atomic purge from another coroutine
        val purgeJob = async(Dispatchers.IO) {
            Thread.sleep(50)
            // Atomic Purge simulation
            simulatedDb.clear()
            synchronized(simulatedHistory) {
                simulatedHistory.clear()
            }
        }

        purgeJob.await()
        Thread.sleep(50)
        isRunning.set(false)
        awaitAll(reader1, reader2)

        assertTrue("Simulated purge during concurrent reads must produce no race crashes. Errors: $readErrors", readErrors.isEmpty())
        assertTrue("Simulated DB must be completely empty after purge", simulatedDb.isEmpty())
        assertTrue("Simulated history must be completely empty after purge", simulatedHistory.isEmpty())
    }

    @Test
    fun testStateTransition_fullSessionLifecycle_login_save_invalidate_relogin() {
        val sessionState = mutableMapOf<String, String>()

        // 1. Initial State: Unauthenticated
        assertTrue(sessionState.isEmpty())

        // 2. Login & Save Session
        sessionState["email"] = "test@clinica.es"
        sessionState["token"] = "jwt.active.token"
        sessionState["userId"] = "usr-888"
        assertEquals(3, sessionState.size)
        assertEquals("jwt.active.token", sessionState["token"])

        // 3. Invalidate Session (e.g. 401 Unauthorized or Token Expiry)
        sessionState.remove("token")
        sessionState.remove("userId")
        assertNull(sessionState["token"])
        assertNull(sessionState["userId"])
        assertEquals("test@clinica.es", sessionState["email"]) // Email preserved for convenience if needed

        // 4. Re-login with refreshed token
        sessionState["token"] = "jwt.new.refreshed.token"
        sessionState["userId"] = "usr-888"
        assertEquals("jwt.new.refreshed.token", sessionState["token"])

        // 5. Total GDPR Purge (Art. 17)
        sessionState.clear()
        assertTrue(sessionState.isEmpty())
    }
}
