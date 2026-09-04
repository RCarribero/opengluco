package com.example.opengluco.core.data

import com.example.opengluco.core.model.AlarmSeverity
import com.example.opengluco.core.model.AlarmType
import com.example.opengluco.core.model.GlucoseAlarm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AlarmEvaluatorTest {

    @Test
    fun testLowAlarmTriggersWhenBelowThreshold() {
        val alarm = GlucoseAlarm(
            id = "alarm-low-70",
            type = AlarmType.LOW,
            thresholdMgDl = 70,
            severity = AlarmSeverity.ALERT,
            cooldownMinutes = 15,
            enabled = true,
            isAllDay = true
        )

        val result = AlarmEvaluator.evaluate(
            currentValueMgDl = 65.0,
            alarms = listOf(alarm),
            lastFiredTimestamps = emptyMap()
        )

        assertNotNull(result.triggeredAlarm)
        assertEquals("alarm-low-70", result.triggeredAlarm?.id)
        assertFalse(result.isInCooldown)
    }

    @Test
    fun testHighAlarmTriggersWhenAboveThreshold() {
        val alarm = GlucoseAlarm(
            id = "alarm-high-200",
            type = AlarmType.HIGH,
            thresholdMgDl = 200,
            severity = AlarmSeverity.ALERT,
            cooldownMinutes = 15,
            enabled = true,
            isAllDay = true
        )

        val result = AlarmEvaluator.evaluate(
            currentValueMgDl = 210.0,
            alarms = listOf(alarm),
            lastFiredTimestamps = emptyMap()
        )

        assertNotNull(result.triggeredAlarm)
        assertEquals("alarm-high-200", result.triggeredAlarm?.id)
        assertFalse(result.isInCooldown)
    }

    @Test
    fun testDisabledAlarmIsIgnored() {
        val alarm = GlucoseAlarm(
            id = "alarm-disabled",
            type = AlarmType.LOW,
            thresholdMgDl = 70,
            severity = AlarmSeverity.ALERT,
            enabled = false,
            isAllDay = true
        )

        val result = AlarmEvaluator.evaluate(
            currentValueMgDl = 50.0,
            alarms = listOf(alarm),
            lastFiredTimestamps = emptyMap()
        )

        assertNull(result.triggeredAlarm)
        assertFalse(result.isInCooldown)
    }

    @Test
    fun testMostCriticalSeverityWins() {
        val alertAlarm = GlucoseAlarm(
            id = "alarm-alert-70",
            type = AlarmType.LOW,
            thresholdMgDl = 70,
            severity = AlarmSeverity.ALERT,
            isAllDay = true
        )
        val urgentAlarm = GlucoseAlarm(
            id = "alarm-urgent-55",
            type = AlarmType.LOW,
            thresholdMgDl = 55,
            severity = AlarmSeverity.URGENT,
            isAllDay = true
        )

        // Reading is 50 -> both 70 and 55 thresholds are crossed
        val result = AlarmEvaluator.evaluate(
            currentValueMgDl = 50.0,
            alarms = listOf(alertAlarm, urgentAlarm),
            lastFiredTimestamps = emptyMap()
        )

        assertNotNull(result.triggeredAlarm)
        assertEquals("alarm-urgent-55", result.triggeredAlarm?.id)
    }

    @Test
    fun testCooldownSuppressesReTrigger() {
        val alarm = GlucoseAlarm(
            id = "alarm-cooldown",
            type = AlarmType.LOW,
            thresholdMgDl = 70,
            severity = AlarmSeverity.ALERT,
            cooldownMinutes = 15,
            isAllDay = true
        )

        val now = 1000000000L
        val lastFired = now - (5 * 60 * 1000L) // 5 minutes ago, cooldown is 15 min

        val result = AlarmEvaluator.evaluate(
            currentValueMgDl = 60.0,
            alarms = listOf(alarm),
            lastFiredTimestamps = mapOf("alarm-cooldown" to lastFired),
            nowMillis = now
        )

        assertNull(result.triggeredAlarm)
        assertTrue(result.isInCooldown)
    }

    @Test
    fun testCooldownExpiredAllowsReTrigger() {
        val alarm = GlucoseAlarm(
            id = "alarm-cooldown-expired",
            type = AlarmType.LOW,
            thresholdMgDl = 70,
            severity = AlarmSeverity.ALERT,
            cooldownMinutes = 15,
            isAllDay = true
        )

        val now = 1000000000L
        val lastFired = now - (16 * 60 * 1000L) // 16 minutes ago, cooldown is 15 min

        val result = AlarmEvaluator.evaluate(
            currentValueMgDl = 60.0,
            alarms = listOf(alarm),
            lastFiredTimestamps = mapOf("alarm-cooldown-expired" to lastFired),
            nowMillis = now
        )

        assertNotNull(result.triggeredAlarm)
        assertEquals("alarm-cooldown-expired", result.triggeredAlarm?.id)
        assertFalse(result.isInCooldown)
    }

    @Test
    fun testOneMinuteCooldownBehavior() {
        val alarm = GlucoseAlarm(
            id = "alarm-1min",
            type = AlarmType.LOW,
            thresholdMgDl = 60,
            severity = AlarmSeverity.URGENT,
            cooldownMinutes = 1,
            isAllDay = true
        )

        val now = 1000000000L
        // 45 seconds ago -> should be suppressed
        val suppressedResult = AlarmEvaluator.evaluate(
            currentValueMgDl = 55.0,
            alarms = listOf(alarm),
            lastFiredTimestamps = mapOf("alarm-1min" to now - 45_000L),
            nowMillis = now
        )
        assertNull(suppressedResult.triggeredAlarm)
        assertTrue(suppressedResult.isInCooldown)

        // 65 seconds ago -> should trigger
        val triggeredResult = AlarmEvaluator.evaluate(
            currentValueMgDl = 55.0,
            alarms = listOf(alarm),
            lastFiredTimestamps = mapOf("alarm-1min" to now - 65_000L),
            nowMillis = now
        )
        assertNotNull(triggeredResult.triggeredAlarm)
        assertEquals("alarm-1min", triggeredResult.triggeredAlarm?.id)
        assertFalse(triggeredResult.isInCooldown)
    }

    @Test
    fun testTwoMinuteCooldownBehavior() {
        val alarm = GlucoseAlarm(
            id = "alarm-2min",
            type = AlarmType.HIGH,
            thresholdMgDl = 200,
            severity = AlarmSeverity.ALERT,
            cooldownMinutes = 2,
            isAllDay = true
        )

        val now = 1000000000L
        // 90 seconds ago (1.5 min) -> suppressed
        val suppressed = AlarmEvaluator.evaluate(
            currentValueMgDl = 220.0,
            alarms = listOf(alarm),
            lastFiredTimestamps = mapOf("alarm-2min" to now - 90_000L),
            nowMillis = now
        )
        assertTrue(suppressed.isInCooldown)

        // 121 seconds ago (2 min 1 sec) -> fires
        val triggered = AlarmEvaluator.evaluate(
            currentValueMgDl = 220.0,
            alarms = listOf(alarm),
            lastFiredTimestamps = mapOf("alarm-2min" to now - 121_000L),
            nowMillis = now
        )
        assertNotNull(triggered.triggeredAlarm)
        assertEquals("alarm-2min", triggered.triggeredAlarm?.id)
    }

    @Test
    fun testThreeAndFourMinuteCooldownBehavior() {
        val alarm3m = GlucoseAlarm(
            id = "alarm-3m",
            type = AlarmType.LOW,
            thresholdMgDl = 70,
            severity = AlarmSeverity.ALERT,
            cooldownMinutes = 3,
            isAllDay = true
        )
        val alarm4m = GlucoseAlarm(
            id = "alarm-4m",
            type = AlarmType.HIGH,
            thresholdMgDl = 180,
            severity = AlarmSeverity.ALERT,
            cooldownMinutes = 4,
            isAllDay = true
        )

        val now = 1000000000L
        // 3m alarm: 170s ago (< 180s) -> suppressed
        assertTrue(AlarmEvaluator.evaluate(65.0, listOf(alarm3m), mapOf("alarm-3m" to now - 170_000L), now).isInCooldown)
        // 3m alarm: 190s ago (> 180s) -> fires
        assertNotNull(AlarmEvaluator.evaluate(65.0, listOf(alarm3m), mapOf("alarm-3m" to now - 190_000L), now).triggeredAlarm)

        // 4m alarm: 230s ago (< 240s) -> suppressed
        assertTrue(AlarmEvaluator.evaluate(190.0, listOf(alarm4m), mapOf("alarm-4m" to now - 230_000L), now).isInCooldown)
        // 4m alarm: 245s ago (> 240s) -> fires
        assertNotNull(AlarmEvaluator.evaluate(190.0, listOf(alarm4m), mapOf("alarm-4m" to now - 245_000L), now).triggeredAlarm)
    }

    @Test
    fun testOvernightScheduleEvaluation() {
        val nightAlarm = GlucoseAlarm(
            id = "night-alarm",
            type = AlarmType.LOW,
            thresholdMgDl = 70,
            severity = AlarmSeverity.ALERT,
            isAllDay = false,
            activeStartHour = 22,
            activeStartMinute = 0,
            activeEndHour = 8,
            activeEndMinute = 0
        )

        // Test at 23:30 (should be active)
        assertTrue(AlarmEvaluator.isWithinActiveSchedule(nightAlarm, 23, 30))

        // Test at 04:00 (should be active)
        assertTrue(AlarmEvaluator.isWithinActiveSchedule(nightAlarm, 4, 0))

        // Test at 14:00 (should NOT be active)
        assertFalse(AlarmEvaluator.isWithinActiveSchedule(nightAlarm, 14, 0))
    }

    @Test
    fun testDaytimeScheduleEvaluation() {
        val dayAlarm = GlucoseAlarm(
            id = "day-alarm",
            type = AlarmType.HIGH,
            thresholdMgDl = 200,
            severity = AlarmSeverity.ALERT,
            isAllDay = false,
            activeStartHour = 8,
            activeStartMinute = 0,
            activeEndHour = 20,
            activeEndMinute = 0
        )

        // Test at 12:00 (should be active)
        assertTrue(AlarmEvaluator.isWithinActiveSchedule(dayAlarm, 12, 0))

        // Test at 22:00 (should NOT be active)
        assertFalse(AlarmEvaluator.isWithinActiveSchedule(dayAlarm, 22, 0))
    }
}
