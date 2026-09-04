package com.example.opengluco.core.model

import org.junit.Assert.*
import org.junit.Test

class CgmCurveSmootherTest {

    @Test
    fun testSubsampleOneOfThree_smallListsPreserved() {
        val empty = emptyList<GlucoseMeasurement>()
        assertEquals(0, CgmCurveSmoother.subsampleOneOfThree(empty).size)

        val one = listOf(
            GlucoseMeasurement(valueInMgPerDl = 110.0, timestamp = "8/27/2026 10:00:00 AM")
        )
        val resOne = CgmCurveSmoother.subsampleOneOfThree(one)
        assertEquals(1, resOne.size)
        assertEquals(110.0, resOne[0].numericValue, 0.001)

        val two = listOf(
            GlucoseMeasurement(valueInMgPerDl = 110.0, timestamp = "8/27/2026 10:00:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 115.0, timestamp = "8/27/2026 10:01:00 AM")
        )
        val resTwo = CgmCurveSmoother.subsampleOneOfThree(two)
        assertEquals(2, resTwo.size)

        val three = listOf(
            GlucoseMeasurement(valueInMgPerDl = 110.0, timestamp = "8/27/2026 10:00:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 115.0, timestamp = "8/27/2026 10:01:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 120.0, timestamp = "8/27/2026 10:02:00 AM")
        )
        val resThree = CgmCurveSmoother.subsampleOneOfThree(three)
        assertEquals(3, resThree.size)
    }

    @Test
    fun testSubsampleOneOfThree_reducesReadingCountToOneOfThree() {
        val readings = (0..8).map { i ->
            GlucoseMeasurement(
                valueInMgPerDl = 100.0 + (i * 2.0),
                timestamp = "8/27/2026 10:0$i:00 AM"
            )
        }

        val subsampled = CgmCurveSmoother.subsampleOneOfThree(readings, useWeightedAverage = true)

        assertTrue(subsampled.size in 3..4)
        assertEquals(116.0, subsampled.last().numericValue, 0.001)
        assertEquals(readings.last().timestamp, subsampled.last().timestamp)
    }

    @Test
    fun testSubsampleOneOfThree_preservesExactLiveMeasurementAtTheTip() {
        val readings = listOf(
            GlucoseMeasurement(valueInMgPerDl = 95.0, timestamp = "8/27/2026 10:00:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 98.0, timestamp = "8/27/2026 10:01:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 102.0, timestamp = "8/27/2026 10:02:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 105.0, timestamp = "8/27/2026 10:03:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 110.0, timestamp = "8/27/2026 10:04:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 115.0, timestamp = "8/27/2026 10:05:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 120.0, timestamp = "8/27/2026 10:06:00 AM")
        )

        val subsampled = CgmCurveSmoother.subsampleOneOfThree(readings)

        val lastOriginal = readings.last()
        val lastSubsampled = subsampled.last()
        assertEquals(lastOriginal.numericValue, lastSubsampled.numericValue, 0.001)
        assertEquals(lastOriginal.timestamp, lastSubsampled.timestamp)
    }

    @Test
    fun testSubsampleOneOfThree_eliminatesMicroOscillationSpikes() {
        val noisyReadings = listOf(
            GlucoseMeasurement(valueInMgPerDl = 100.0, timestamp = "8/27/2026 10:00:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 106.0, timestamp = "8/27/2026 10:01:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 99.0, timestamp = "8/27/2026 10:02:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 105.0, timestamp = "8/27/2026 10:03:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 98.0, timestamp = "8/27/2026 10:04:00 AM"),
            GlucoseMeasurement(valueInMgPerDl = 104.0, timestamp = "8/27/2026 10:05:00 AM")
        )

        val subsampled = CgmCurveSmoother.subsampleOneOfThree(noisyReadings, useWeightedAverage = true)

        assertEquals(102.75, subsampled.first().numericValue, 0.1)
        assertTrue(subsampled.size < noisyReadings.size)
    }

    @Test
    fun testCatmullRomSpline_generatesValidCurveSegments() {
        val points = listOf(
            Pair(0f, 100f),
            Pair(50f, 110f),
            Pair(100f, 120f),
            Pair(150f, 105f)
        )

        val segments = CgmCurveSmoother.computeCatmullRomSpline(points)
        assertEquals(3, segments.size)

        for (seg in segments) {
            assertTrue(seg.startX.isFinite())
            assertTrue(seg.startY.isFinite())
            assertTrue(seg.endX.isFinite())
            assertTrue(seg.endY.isFinite())
        }
    }
}
