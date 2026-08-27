package com.example.opengluco.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelSanityTest {
    @Test
    fun testGlucoseMeasurementNumericValue() {
        val measurement = GlucoseMeasurement(
            valueInMgPerDl = 142.0,
            trendArrow = 3
        )
        assertEquals(142.0, measurement.numericValue, 0.001)
        assertEquals("→", measurement.trendSymbol)
        assertEquals("Estable", measurement.trendText)
    }
}
