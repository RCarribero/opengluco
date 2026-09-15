package com.example.opengluco.core.model

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class ClinicalModelsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // --- Tier 1 & Tier 2: GlucoseMeasurement Numeric Values & Units ---

    @Test
    fun testNumericValuePriority_prefersValueInMgPerDl() {
        val m = GlucoseMeasurement(valueInMgPerDl = 135.5, value = 120.0)
        assertEquals(135.5, m.numericValue, 0.001)
    }

    @Test
    fun testNumericValuePriority_fallbackToValue() {
        val m = GlucoseMeasurement(valueInMgPerDl = null, value = 110.0)
        assertEquals(110.0, m.numericValue, 0.001)
    }

    @Test
    fun testNumericValuePriority_nullDefaultsToZero() {
        val m = GlucoseMeasurement(valueInMgPerDl = null, value = null)
        assertEquals(0.0, m.numericValue, 0.001)
    }

    @Test
    fun testFormattedValue_mgdlIntegerRounding() {
        val m1 = GlucoseMeasurement(valueInMgPerDl = 142.4)
        val m2 = GlucoseMeasurement(valueInMgPerDl = 142.6)
        assertEquals("142", m1.getFormattedValue(isMmol = false))
        assertEquals("143", m2.getFormattedValue(isMmol = false))
    }

    @Test
    fun testFormattedValue_mmolConversionUSLocale() {
        val m = GlucoseMeasurement(valueInMgPerDl = 180.0) // 180 / 18.0182 = 9.9898 -> "10.0"
        assertEquals("10.0", m.getFormattedValue(isMmol = true))

        val mLow = GlucoseMeasurement(valueInMgPerDl = 70.0) // 70 / 18.0182 = 3.885 -> "3.9"
        assertEquals("3.9", mLow.getFormattedValue(isMmol = true))

        val mUrgent = GlucoseMeasurement(valueInMgPerDl = 54.0) // 54 / 18.0182 = 2.997 -> "3.0"
        assertEquals("3.0", mUrgent.getFormattedValue(isMmol = true))
    }

    // --- Tier 1 & Tier 2: Trend Symbols and Trend Text ---

    @Test
    fun testTrendSymbolAndText_allArrows() {
        val arrows = listOf(
            1 to Pair("↓", "Cayendo rápido"),
            2 to Pair("↘", "Bajando"),
            3 to Pair("→", "Estable"),
            4 to Pair("↗", "Subiendo"),
            5 to Pair("↑", "Subiendo rápido")
        )
        for ((arrow, expected) in arrows) {
            val m = GlucoseMeasurement(valueInMgPerDl = 100.0, trendArrow = arrow)
            assertEquals("Symbol mismatch for arrow $arrow", expected.first, m.trendSymbol)
            assertEquals("Text mismatch for arrow $arrow", expected.second, m.trendText)
        }
    }

    @Test
    fun testTrendSymbolAndText_invalidOrNullArrows_fallbackToStable() {
        val invalidArrows = listOf(null, 0, -1, 6, 99)
        for (arrow in invalidArrows) {
            val m = GlucoseMeasurement(valueInMgPerDl = 100.0, trendArrow = arrow)
            assertEquals("Default symbol should be → for $arrow", "→", m.trendSymbol)
            assertEquals("Default text should be 'Estable' for $arrow", "Estable", m.trendText)
        }
    }

    // --- Tier 1 & Tier 2: Timestamp Parsing & Display Time ---

    @Test
    fun testTimestampParsing_isoUtcZ() {
        val m = GlucoseMeasurement(timestamp = "2026-08-27T10:15:30.000Z")
        val epoch = m.getEpochMillis()
        assertTrue("Epoch should be positive for ISO UTC string", epoch > 0L)
    }

    @Test
    fun testTimestampParsing_us12HourFormat() {
        val m = GlucoseMeasurement(timestamp = "8/27/2026 10:15:30 AM")
        val epoch = m.getEpochMillis()
        assertTrue("Epoch should be positive for US 12h format", epoch > 0L)
    }

    @Test
    fun testTimestampParsing_isoNoZ() {
        val m = GlucoseMeasurement(timestamp = "2026-08-27T10:15:30")
        val epoch = m.getEpochMillis()
        assertTrue("Epoch should be positive for ISO without Z", epoch > 0L)
    }

    @Test
    fun testTimestampParsing_factoryTimestampFallback() {
        val m = GlucoseMeasurement(timestamp = null, factoryTimestamp = "8/27/2026 2:30:00 PM")
        val epoch = m.getEpochMillis()
        assertTrue("Epoch should parse from factoryTimestamp", epoch > 0L)
    }

    @Test
    fun testTimestampParsing_invalidOrNullReturnsZero() {
        val m1 = GlucoseMeasurement(timestamp = null, factoryTimestamp = null)
        val m2 = GlucoseMeasurement(timestamp = "invalid-date-string")
        assertEquals(0L, m1.getEpochMillis())
        assertEquals(0L, m2.getEpochMillis())
    }

    @Test
    fun testDisplayTime_validTimestampFormats24Hours() {
        val m = GlucoseMeasurement(timestamp = "8/27/2026 14:45:00")
        val displayTime = m.getDisplayTime()
        assertTrue("Display time should be in HH:mm format (5 chars)", displayTime.matches(Regex("\\d{2}:\\d{2}")))
    }

    @Test
    fun testDisplayTime_nullOrInvalidFallback() {
        val m = GlucoseMeasurement(timestamp = null, factoryTimestamp = null)
        assertEquals("Ahora", m.getDisplayTime())
    }

    // --- Tier 1 & Tier 2: SensorInfo & Patient Connections ---

    @Test
    fun testSensorRemainingDays_activeSensor() {
        val nowSec = System.currentTimeMillis() / 1000
        val activatedTwoDaysAgo = nowSec - (2 * 24 * 3600)
        val sensor = SensorInfo(
            deviceId = "sensor-123",
            serialNumber = "SN-987654",
            activatedTimestamp = activatedTwoDaysAgo,
            sensorType = 3
        )
        val remaining = sensor.getRemainingDays()
        assertNotNull(remaining)
        assertEquals(12, remaining!!)
        assertEquals("FreeStyle Libre 3", sensor.sensorModelName)
    }

    @Test
    fun testSensorRemainingDays_expiredSensor() {
        val nowSec = System.currentTimeMillis() / 1000
        val activatedTwentyDaysAgo = nowSec - (20 * 24 * 3600)
        val sensor = SensorInfo(
            deviceId = "sensor-old",
            activatedTimestamp = activatedTwentyDaysAgo,
            sensorType = 2
        )
        assertEquals(0, sensor.getRemainingDays())
        assertEquals("FreeStyle Libre 2", sensor.sensorModelName)
    }

    @Test
    fun testSensorRemainingDays_nullActivationTimestamp() {
        val sensor = SensorInfo(deviceId = "sensor-null", activatedTimestamp = null, sensorType = 1)
        assertNull(sensor.getRemainingDays())
        assertEquals("FreeStyle Libre 1", sensor.sensorModelName)
    }

    @Test
    fun testSensorModelName_defaultType() {
        val sensor = SensorInfo(sensorType = null)
        assertEquals("FreeStyle Libre Sensor", sensor.sensorModelName)
    }

    @Test
    fun testConnectionItem_fullName_combinations() {
        val c1 = ConnectionItem(id = "1", patientId = "p1", firstName = "John", lastName = "Doe")
        assertEquals("John Doe", c1.fullName)

        val c2 = ConnectionItem(id = "2", patientId = "p2", firstName = "Maria", lastName = null)
        assertEquals("Maria", c2.fullName)

        val c3 = ConnectionItem(id = "3", patientId = "p3", firstName = null, lastName = "Garcia")
        assertEquals("Garcia", c3.fullName)

        val c4 = ConnectionItem(id = "4", patientId = "p4", firstName = "", lastName = "   ")
        assertEquals("Paciente", c4.fullName)
    }

    @Test
    fun testConnectionItem_effectiveMeasurement() {
        val m1 = GlucoseMeasurement(valueInMgPerDl = 120.0)
        val m2 = GlucoseMeasurement(valueInMgPerDl = 130.0)

        val cWithBoth = ConnectionItem(id = "1", patientId = "p1", glucoseMeasurement = m1, glucoseItem = m2)
        assertEquals(120.0, cWithBoth.effectiveMeasurement?.numericValue ?: 0.0, 0.001)

        val cWithItemOnly = ConnectionItem(id = "2", patientId = "p2", glucoseMeasurement = null, glucoseItem = m2)
        assertEquals(130.0, cWithItemOnly.effectiveMeasurement?.numericValue ?: 0.0, 0.001)

        val cWithNeither = ConnectionItem(id = "3", patientId = "p3", glucoseMeasurement = null, glucoseItem = null)
        assertNull(cWithNeither.effectiveMeasurement)
    }

    // --- Tier 1 & Tier 2: Serialization / Deserialization ---

    @Test
    fun testBaseResponseSerialization() {
        val loginData = LoginData(
            user = UserProfile(id = "usr-1", firstName = "Jane", email = "jane@example.com"),
            authTicket = AuthTicket(token = "jwt-sample-token", expires = 1750000000L, duration = 3600000L),
            region = "eu"
        )
        val response = BaseResponse(status = 0, data = loginData)
        val jsonStr = json.encodeToString(BaseResponse.serializer(LoginData.serializer()), response)
        assertTrue(jsonStr.contains("jwt-sample-token"))

        val deserialized = json.decodeFromString(BaseResponse.serializer(LoginData.serializer()), jsonStr)
        assertEquals(0, deserialized.status)
        assertEquals("jane@example.com", deserialized.data?.user?.email)
    }

    // --- Sensor Lifecycle State & Staleness Tests ---

    @Test
    fun testSensorLifecycleState_nullOrZeroTimestamp_returnsNoSensor() {
        val nullSensor: SensorInfo? = null
        assertEquals(SensorLifecycleState.NoSensor, nullSensor.lifecycleState())

        val zeroSensor = SensorInfo(activatedTimestamp = 0L)
        assertEquals(SensorLifecycleState.NoSensor, zeroSensor.getLifecycleState())

        val negativeSensor = SensorInfo(activatedTimestamp = -100L)
        assertEquals(SensorLifecycleState.NoSensor, negativeSensor.getLifecycleState())
    }

    @Test
    fun testSensorLifecycleState_warmingUp_detectsRemainingMinutes() {
        val nowMs = System.currentTimeMillis()
        val activatedMs = nowMs - (15 * 60 * 1000L) // 15 min de 60 min de calentamiento
        val sensor = SensorInfo(
            activatedTimestamp = activatedMs,
            warmupDurationMinutes = 60,
            lifetimeDays = 14
        )
        val state = sensor.getLifecycleState(nowMs)
        assertTrue(state is SensorLifecycleState.WarmingUp)
        assertEquals(45, (state as SensorLifecycleState.WarmingUp).remainingMinutes)
    }

    @Test
    fun testSensorLifecycleState_active_calculatesRemainingDays() {
        val nowMs = System.currentTimeMillis()
        val activatedMs = nowMs - (2 * 24 * 3600 * 1000L) // 2 días de 14
        val sensor = SensorInfo(
            activatedTimestamp = activatedMs,
            warmupDurationMinutes = 60,
            lifetimeDays = 14
        )
        val state = sensor.getLifecycleState(nowMs)
        assertTrue(state is SensorLifecycleState.Active)
        assertEquals(12, (state as SensorLifecycleState.Active).remainingDays)
    }

    @Test
    fun testSensorLifecycleState_expired_byTimeOrFlag() {
        val nowMs = System.currentTimeMillis()
        // Vencido por tiempo (15 días de 14)
        val expiredByTime = SensorInfo(
            activatedTimestamp = nowMs - (15 * 24 * 3600 * 1000L),
            lifetimeDays = 14
        )
        assertEquals(SensorLifecycleState.Expired, expiredByTime.getLifecycleState(nowMs))
        assertEquals(0, expiredByTime.getRemainingDays(nowMs))

        // Vencido por flag isSensorActive = false
        val inactiveFlag = SensorInfo(
            activatedTimestamp = nowMs - (2 * 24 * 3600 * 1000L),
            isSensorActive = false,
            lifetimeDays = 14
        )
        assertEquals(SensorLifecycleState.Expired, inactiveFlag.getLifecycleState(nowMs))
        assertEquals(0, inactiveFlag.getRemainingDays(nowMs))
    }

    @Test
    fun testAbbottActiveSensorsDeserialization_resolvesSensorCorrectly() {
        val rawJson = """
            {
                "connection": null,
                "activeSensors": [
                    {
                        "sensor": {
                            "a": 1700000000,
                            "sn": "0M001A2B3C",
                            "pt": 3
                        },
                        "device": {
                            "did": "device-uuid-1234",
                            "dtid": 1,
                            "v": "3.4.0"
                        }
                    }
                ],
                "graphData": []
            }
        """.trimIndent()

        val graphData = json.decodeFromString<GraphData>(rawJson)
        assertNotNull(graphData.activeSensors)
        assertEquals(1, graphData.activeSensors!!.size)

        val resolved = graphData.resolvedSensor
        assertNotNull("resolvedSensor should extract the nested sensor object", resolved)
        assertEquals("0M001A2B3C", resolved!!.serialNumber)
        assertEquals(3, resolved.sensorType)
        assertEquals(1700000000L, resolved.activatedTimestamp)
        assertEquals("FreeStyle Libre 3", resolved.sensorModelName)
    }

    @Test
    fun testGlucoseMeasurement_isStaleThreshold() {
        val nowMs = System.currentTimeMillis()
        val recentDate = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(java.util.Date(nowMs - 5 * 60 * 1000L))
        val recentMeasurement = GlucoseMeasurement(timestamp = recentDate)
        assertFalse("Measurement from 5 min ago should not be stale", recentMeasurement.isStale(nowMs))

        val staleDate = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(java.util.Date(nowMs - 22 * 60 * 1000L))
        val staleMeasurement = GlucoseMeasurement(timestamp = staleDate)
        assertTrue("Measurement from 22 min ago must be stale", staleMeasurement.isStale(nowMs))

        val emptyMeasurement = GlucoseMeasurement(timestamp = null)
        assertTrue("Measurement with null timestamp must be stale", emptyMeasurement.isStale(nowMs))
    }

    @Test
    fun testAbbottActiveSensors_multipleSensors_selectsActiveOne() {
        val rawJson = """
            {
                "connection": null,
                "activeSensors": [
                    {
                        "sensor": {
                            "a": 1690000000,
                            "sn": "OLD_EXPIRED",
                            "pt": 2,
                            "s": false
                        }
                    },
                    {
                        "sensor": {
                            "a": 1700000000,
                            "sn": "NEW_ACTIVE",
                            "pt": 3,
                            "s": true
                        }
                    }
                ],
                "graphData": []
            }
        """.trimIndent()

        val graphData = json.decodeFromString<GraphData>(rawJson)
        val resolved = graphData.resolvedSensor
        assertNotNull(resolved)
        assertEquals("NEW_ACTIVE", resolved!!.serialNumber)
        assertEquals(true, resolved.isSensorActive)
    }

    @Test
    fun testAbbottActiveSensors_emptyList_marksConnectionSensorAsExpired() {
        val rawJson = """
            {
                "connection": {
                    "id": "c1",
                    "patientId": "p1",
                    "sensor": {
                        "a": 1700000000,
                        "sn": "HISTORICAL_SENSOR",
                        "pt": 2,
                        "l": 14
                    }
                },
                "activeSensors": [],
                "graphData": []
            }
        """.trimIndent()

        val graphData = json.decodeFromString<GraphData>(rawJson)
        val resolved = graphData.resolvedSensor
        assertNotNull(resolved)
        assertEquals("HISTORICAL_SENSOR", resolved!!.serialNumber)
        assertEquals(false, resolved.isSensorActive)
        assertEquals(0, resolved.getRemainingDays())
        assertEquals(SensorLifecycleState.Expired, resolved.getLifecycleState())
    }

    @Test
    fun testAbbottActiveSensors_emptyList_withNullConnection_returnsNull() {
        val rawJson = """
            {
                "connection": null,
                "activeSensors": [],
                "graphData": []
            }
        """.trimIndent()

        val graphData = json.decodeFromString<GraphData>(rawJson)
        assertNull(graphData.resolvedSensor)
        assertEquals(SensorLifecycleState.NoSensor, graphData.resolvedSensor.lifecycleState())
    }

    @Test
    fun testAbbottActiveSensors_nullSensorEntry_skipsNull() {
        val rawJson = """
            {
                "connection": null,
                "activeSensors": [
                    {
                        "sensor": null,
                        "device": { "did": "dev1" }
                    },
                    {
                        "sensor": {
                            "a": 1700000000,
                            "sn": "VALID_SN",
                            "pt": 3,
                            "s": true
                        }
                    }
                ],
                "graphData": []
            }
        """.trimIndent()

        val graphData = json.decodeFromString<GraphData>(rawJson)
        val resolved = graphData.resolvedSensor
        assertNotNull(resolved)
        assertEquals("VALID_SN", resolved!!.serialNumber)
    }
}

