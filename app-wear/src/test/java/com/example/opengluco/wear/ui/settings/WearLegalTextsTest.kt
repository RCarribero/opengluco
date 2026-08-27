package com.example.opengluco.wear.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WearLegalTextsTest {

    @Test
    fun testPassiveLegalFooterExactContent() {
        val expectedFooter = "Visualizador secundario pasivo. No es un dispositivo médico y no sustituye al lector oficial ni a decisiones clínicas profesionales."
        assertEquals(expectedFooter, WearLegalTexts.PASSIVE_FOOTER)
    }

    @Test
    fun testMedicalDisclaimerCompliesWithMdrAndFdaMdds() {
        assertEquals("MDR UE 2017/745 / FDA MDDS", WearLegalTexts.MEDICAL_BADGE)
        assertEquals("Visualizador Secundario de Conveniencia", WearLegalTexts.MEDICAL_TITLE)
        
        val content = WearLegalTexts.MEDICAL_CONTENT
        assertNotNull(content)
        assertTrue("Must state it is not a medical device", content.contains("NO es un Dispositivo Médico"))
        assertTrue("Must reference MDR 2017/745", content.contains("MDR 2017/745"))
        assertTrue("Must reference FDA", content.contains("FDA"))
        assertTrue("Must prohibit insulin bolus/dosing calculations", content.contains("Prohibido para Dosificación") && content.contains("NUNCA use las lecturas"))
        assertTrue("Must require fingerstick/capillary verification on mismatch", content.contains("Comprobación Capilar Obligatoria") && content.contains("prueba capilar"))
    }

    @Test
    fun testTrademarkNoticeStatesFairUseAndNonAffiliation() {
        val content = WearLegalTexts.TRADEMARK_CONTENT
        assertNotNull(content)
        assertTrue("Must mention FreeStyle Libre trademarks", content.contains("FreeStyle, Libre, LibreLink"))
        assertTrue("Must mention OpenGluco", content.contains("OpenGluco"))
        assertTrue("Must acknowledge Abbott Laboratories ownership", content.contains("Abbott Laboratories"))
        assertTrue("Must declare independent client and no affiliation", content.contains("NO está patrocinado, afiliado"))
        assertTrue("Must declare nominative compatibility description", content.contains("compatibilidad técnica e interoperabilidad"))
    }

    @Test
    fun testPrivacyNoticeCoversGdprArt9LocalFirst() {
        assertEquals("RGPD Art. 9 / LOPDGDD", WearLegalTexts.GDPR_BADGE)
        assertEquals("Tratamiento y Protección de Datos de Salud", WearLegalTexts.GDPR_TITLE)

        val content = WearLegalTexts.GDPR_CONTENT
        assertNotNull(content)
        assertTrue("Must reference GDPR Art. 9 special category health data", content.contains("Artículo 9 del RGPD"))
        assertTrue("Must specify 100% local architecture and Keystore encryption", content.contains("100% Local (Local-First)") && content.contains("Android Keystore"))
        assertTrue("Must specify zero intermediary cloud servers", content.contains("Cero Servidores Intermediarios"))
        assertTrue("Must mention export and erase rights (Art. 17 & 20)", content.contains("Portabilidad") && content.contains("Supresión") && content.contains("CSV"))
    }

    @Test
    fun testDeleteConfirmationCoversGdprArt17RightToErasure() {
        assertTrue("Must indicate destructive action in title", WearLegalTexts.DELETE_CONFIRM_TITLE.contains("Acción Destructiva Irreversible"))
        val body = WearLegalTexts.DELETE_CONFIRM_BODY
        assertNotNull(body)
        assertTrue("Must mention 90-day history wipe", body.contains("90 días"))
        assertTrue("Must mention local cache and credentials", body.contains("caché local") && body.contains("credenciales de sesión"))
        assertEquals("Cancelar", WearLegalTexts.DELETE_CONFIRM_BTN_CANCEL)
        assertEquals("Borrar Todo", WearLegalTexts.DELETE_CONFIRM_BTN_CONFIRM)
    }

    @Test
    fun testWearLegalNoticeTypeEnumValues() {
        val types = WearLegalNoticeType.values()
        assertEquals(5, types.size)
        assertTrue(types.contains(WearLegalNoticeType.NONE))
        assertTrue(types.contains(WearLegalNoticeType.MEDICAL_DISCLAIMER))
        assertTrue(types.contains(WearLegalNoticeType.TRADEMARKS))
        assertTrue(types.contains(WearLegalNoticeType.PRIVACY_GDPR))
        assertTrue(types.contains(WearLegalNoticeType.DELETE_CONFIRMATION))
    }
}
