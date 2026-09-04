package com.example.opengluco.core.data

import com.example.opengluco.core.model.AlarmCooldown
import com.example.opengluco.core.model.AlarmSeverity
import com.example.opengluco.core.model.AlarmSoundType
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

    @Test
    fun testAlarmCooldownEnumValues() {
        assertEquals(1, AlarmCooldown.MIN_1.minutes)
        assertEquals(2, AlarmCooldown.MIN_2.minutes)
        assertEquals(3, AlarmCooldown.MIN_3.minutes)
        assertEquals(4, AlarmCooldown.MIN_4.minutes)
        assertEquals(5, AlarmCooldown.MIN_5.minutes)
        assertEquals(10, AlarmCooldown.MIN_10.minutes)
        assertEquals(15, AlarmCooldown.MIN_15.minutes)
        assertEquals(30, AlarmCooldown.MIN_30.minutes)
        assertEquals(0, AlarmCooldown.NONE.minutes)
    }

    @Test
    fun testAlarmListSerializationWith1To4MinuteCooldownsAndSoundType() {
        val alarms = listOf(
            GlucoseAlarm(
                id = "alarm-1min-extreme",
                type = AlarmType.LOW,
                thresholdMgDl = 54,
                severity = AlarmSeverity.URGENT,
                enabled = true,
                cooldownMinutes = 1,
                soundType = AlarmSoundType.URGENT_EXTREME
            ),
            GlucoseAlarm(
                id = "alarm-2min-medical",
                type = AlarmType.LOW,
                thresholdMgDl = 68,
                severity = AlarmSeverity.ALERT,
                enabled = true,
                cooldownMinutes = 2,
                soundType = AlarmSoundType.URGENT_MEDICAL
            ),
            GlucoseAlarm(
                id = "alarm-3min-alert",
                type = AlarmType.HIGH,
                thresholdMgDl = 185,
                severity = AlarmSeverity.INFORMATIVE,
                enabled = true,
                cooldownMinutes = 3,
                soundType = AlarmSoundType.ALERT_STANDARD
            ),
            GlucoseAlarm(
                id = "alarm-4min-silent",
                type = AlarmType.HIGH,
                thresholdMgDl = 260,
                severity = AlarmSeverity.URGENT,
                enabled = true,
                cooldownMinutes = 4,
                soundType = AlarmSoundType.SILENT
            )
        )

        val jsonStr = json.encodeToString(alarms)
        val deserialized = json.decodeFromString<List<GlucoseAlarm>>(jsonStr)

        assertEquals(4, deserialized.size)
        assertEquals(1, deserialized[0].cooldownMinutes)
        assertEquals(AlarmSoundType.URGENT_EXTREME, deserialized[0].soundType)

        assertEquals(2, deserialized[1].cooldownMinutes)
        assertEquals(AlarmSoundType.URGENT_MEDICAL, deserialized[1].soundType)

        assertEquals(3, deserialized[2].cooldownMinutes)
        assertEquals(AlarmSoundType.ALERT_STANDARD, deserialized[2].soundType)

        assertEquals(4, deserialized[3].cooldownMinutes)
        assertEquals(AlarmSoundType.SILENT, deserialized[3].soundType)
    }

    @Test
    fun testBackwardsCompatibilityWithoutSoundTypeField() {
        val legacyJson = """
            [
                {
                    "id": "legacy-alarm",
                    "type": "LOW",
                    "thresholdMgDl": 70,
                    "severity": "ALERT",
                    "cooldownMinutes": 15,
                    "enabled": true,
                    "activeStartHour": 0,
                    "activeStartMinute": 0,
                    "activeEndHour": 23,
                    "activeEndMinute": 59,
                    "isAllDay": true
                }
            ]
        """.trimIndent()

        val deserialized = json.decodeFromString<List<GlucoseAlarm>>(legacyJson)
        assertEquals(1, deserialized.size)
        assertEquals("legacy-alarm", deserialized[0].id)
        assertEquals(AlarmSoundType.DEFAULT, deserialized[0].soundType)
        org.junit.Assert.assertNull(deserialized[0].customSoundUri)
        org.junit.Assert.assertNull(deserialized[0].customSoundName)
    }

    @Test
    fun testCustomSoundAlarmSerializationAndDeserialization() {
        val alarm = GlucoseAlarm(
            id = "alarm-custom-audio",
            type = AlarmType.LOW,
            thresholdMgDl = 60,
            severity = AlarmSeverity.URGENT,
            enabled = true,
            cooldownMinutes = 2,
            soundType = AlarmSoundType.CUSTOM,
            customSoundUri = "file:///data/user/0/com.example.opengluco/files/custom_alarms/my_sound.mp3",
            customSoundName = "my_sound.mp3"
        )

        val encoded = json.encodeToString(alarm)
        assertTrue(encoded.contains("\"soundType\":\"CUSTOM\""))
        assertTrue(encoded.contains("my_sound.mp3"))

        val decoded = json.decodeFromString<GlucoseAlarm>(encoded)
        assertEquals("alarm-custom-audio", decoded.id)
        assertEquals(AlarmSoundType.CUSTOM, decoded.soundType)
        assertEquals("file:///data/user/0/com.example.opengluco/files/custom_alarms/my_sound.mp3", decoded.customSoundUri)
        assertEquals("my_sound.mp3", decoded.customSoundName)
    }
}