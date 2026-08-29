package com.example.opengluco.core.data

import com.example.opengluco.core.model.AlarmSeverity
import com.example.opengluco.core.model.AlarmType
import com.example.opengluco.core.model.GlucoseAlarm
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmSerializationSyncTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun testAlarmListSerializationAndDeserializationForWearSync() {
        val originalAlarms = listOf(
            GlucoseAlarm(
                id = "alarm-custom-low",
                type = AlarmType.LOW,
                thresholdMgDl = 65,
                severity = AlarmSeverity.URGENT,
                enabled = true,
                cooldownMinutes = 20,
                isAllDay = true
            ),
            GlucoseAlarm(
                id = "alarm-custom-high",
                type = AlarmType.HIGH,
                thresholdMgDl = 195,
                severity = AlarmSeverity.ALERT,
                enabled = true,
                cooldownMinutes = 45,
                isAllDay = false,
                activeStartHour = 8,
                activeStartMinute = 30,
                activeEndHour = 22,
                activeEndMinute = 0
            )
        )

        val jsonStr = json.encodeToString(originalAlarms)
        assertNotNull(jsonStr)
        assertTrue(jsonStr.contains("alarm-custom-low"))
        assertTrue(jsonStr.contains("alarm-custom-high"))

        val deserialized = json.decodeFromString<List<GlucoseAlarm>>(jsonStr)
        assertEquals(2, deserialized.size)
        assertEquals("alarm-custom-low", deserialized[0].id)
        assertEquals(65, deserialized[0].thresholdMgDl)
        assertEquals(AlarmSeverity.URGENT, deserialized[0].severity)
        assertEquals("alarm-custom-high", deserialized[1].id)
        assertEquals(195, deserialized[1].thresholdMgDl)
        assertEquals(8, deserialized[1].activeStartHour)
        assertEquals(30, deserialized[1].activeStartMinute)
    }
}