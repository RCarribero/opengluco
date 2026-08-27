package com.example.opengluco.wear.ui.settings

import com.example.opengluco.wear.ui.theme.ClinicalHighAmber
import com.example.opengluco.wear.ui.theme.ClinicalLowCoral
import com.example.opengluco.wear.ui.theme.ClinicalMint
import com.example.opengluco.wear.ui.theme.ClinicalUrgentCrimson
import com.example.opengluco.wear.ui.theme.ClinicalVeryHighOrange
import com.example.opengluco.wear.ui.theme.getClinicalStatusColor
import com.example.opengluco.wear.ui.theme.getGlucoseStatusColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WearSettingsAndDashboardContractTest {

    @Test
    fun testClinicalStatusColorThresholds() {
        // Urgent low: <= 55
        assertEquals(ClinicalUrgentCrimson, getClinicalStatusColor(55.0))
        assertEquals(ClinicalUrgentCrimson, getClinicalStatusColor(40.0))

        // Low: 56..69
        assertEquals(ClinicalLowCoral, getClinicalStatusColor(56.0))
        assertEquals(ClinicalLowCoral, getClinicalStatusColor(69.0))

        // Normal in range: 70..180
        assertEquals(ClinicalMint, getClinicalStatusColor(70.0))
        assertEquals(ClinicalMint, getClinicalStatusColor(120.0))
        assertEquals(ClinicalMint, getClinicalStatusColor(180.0))

        // High: 181..250
        assertEquals(ClinicalHighAmber, getClinicalStatusColor(181.0))
        assertEquals(ClinicalHighAmber, getClinicalStatusColor(250.0))

        // Very high: > 250
        assertEquals(ClinicalVeryHighOrange, getClinicalStatusColor(251.0))
        assertEquals(ClinicalVeryHighOrange, getClinicalStatusColor(350.0))

        // Alias parity
        assertEquals(getClinicalStatusColor(110.0), getGlucoseStatusColor(110.0))
    }

    @Test
    fun testLegalDisclaimerTextsNonEmptyAndFormatted() {
        assertTrue(WearLegalTexts.PASSIVE_FOOTER.isNotBlank())
        assertTrue(WearLegalTexts.MEDICAL_TITLE.isNotBlank())
        assertTrue(WearLegalTexts.MEDICAL_CONTENT.isNotBlank())
        assertTrue(WearLegalTexts.TRADEMARK_TITLE.isNotBlank())
        assertTrue(WearLegalTexts.TRADEMARK_CONTENT.isNotBlank())
        assertTrue(WearLegalTexts.GDPR_TITLE.isNotBlank())
        assertTrue(WearLegalTexts.GDPR_CONTENT.isNotBlank())
        assertTrue(WearLegalTexts.DELETE_CONFIRM_TITLE.isNotBlank())
        assertTrue(WearLegalTexts.DELETE_CONFIRM_BODY.isNotBlank())

        // No placeholder tokens left in strings
        assertFalse(WearLegalTexts.PASSIVE_FOOTER.contains("TODO"))
        assertFalse(WearLegalTexts.MEDICAL_CONTENT.contains("TODO"))
        assertFalse(WearLegalTexts.TRADEMARK_CONTENT.contains("TODO"))
        assertFalse(WearLegalTexts.GDPR_CONTENT.contains("TODO"))
        assertFalse(WearLegalTexts.DELETE_CONFIRM_BODY.contains("TODO"))
    }
}
