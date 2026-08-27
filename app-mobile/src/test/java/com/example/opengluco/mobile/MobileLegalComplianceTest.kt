package com.example.opengluco.mobile

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class MobileLegalComplianceTest {

    // --- Tier 1 & Tier 2: Legal & Regulatory Texts Verification ---

    @Test
    fun testMdrFdaMedicalDisclaimerString() {
        val expectedBadge = "MDR UE 2017/745 / FDA MDDS"
        val expectedTitle = "Visualizador Secundario de Conveniencia"

        // Read LegalComplianceComponents.kt to verify exact legal string presence
        val legalFile = File("src/main/java/com/example/opengluco/mobile/ui/dashboard/components/LegalComplianceComponents.kt")
        assertTrue("LegalComplianceComponents.kt must exist", legalFile.exists())
        val content = legalFile.readText()

        assertTrue("Must contain MDR/FDA badge", content.contains(expectedBadge))
        assertTrue("Must contain secondary viewer title", content.contains(expectedTitle))
        assertTrue("Must prohibit insulin dosing", content.contains("Prohibido para Dosificación") || content.contains("calcular dosis de insulina"))
        assertTrue("Must mandate fingerstick blood glucose check", content.contains("Comprobación Capilar Obligatoria") || content.contains("prueba capilar"))
    }

    @Test
    fun testTrademarkNoticeString() {
        val expectedBadge = "Uso Legítimo Nominativo"
        val expectedTitle = "Titularidad de Marcas"

        val legalFile = File("src/main/java/com/example/opengluco/mobile/ui/dashboard/components/LegalComplianceComponents.kt")
        val content = legalFile.readText()

        assertTrue("Must contain Trademark badge", content.contains(expectedBadge))
        assertTrue("Must contain Trademark title", content.contains(expectedTitle))
        assertTrue("Must mention FreeStyle and Libre trademarks", content.contains("FreeStyle, Libre, LibreLink"))
        assertTrue("Must mention OpenGluco", content.contains("OpenGluco"))
        assertTrue("Must mention Abbott Laboratories", content.contains("Abbott Laboratories"))
        assertTrue("Must declare independent client non-affiliation", content.contains("NO está patrocinado, afiliado, autorizado ni respaldado"))
    }

    @Test
    fun testGdprArt9HealthDataPrivacyString() {
        val legalFile = File("src/main/java/com/example/opengluco/mobile/ui/dashboard/components/LegalComplianceComponents.kt")
        val content = legalFile.readText()

        assertTrue("Must mention GDPR Art. 9", content.contains("RGPD Art. 9") || content.contains("Artículo 9 del RGPD"))
        assertTrue("Must mention Local-First architecture", content.contains("Local-First") || content.contains("Arquitectura 100% Local"))
        assertTrue("Must mention Android Keystore", content.contains("Android Keystore"))
        assertTrue("Must declare zero intermediary servers", content.contains("Cero Servidores Intermediarios"))
    }

    @Test
    fun testGdprArt17DeleteConfirmationStrings() {
        val legalFile = File("src/main/java/com/example/opengluco/mobile/ui/dashboard/components/LegalComplianceComponents.kt")
        val content = legalFile.readText()

        assertTrue("Must contain destructive action title", content.contains("Acción Destructiva Irreversible"))
        assertTrue("Must warn about 90 days history deletion", content.contains("últimos 90 días"))
        assertTrue("Must have Confirm button text", content.contains("Borrar Todo"))
        assertTrue("Must have Cancel button text", content.contains("Cancelar"))
    }

    @Test
    fun testPassiveLegalFooterPresence() {
        val expectedCore = "Visualizador secundario pasivo. No es un dispositivo médico"

        // Verify across mobile dashboard screen
        val dashboardFile = File("src/main/java/com/example/opengluco/mobile/ui/dashboard/MobileDashboardScreen.kt")
        assertTrue("MobileDashboardScreen.kt must exist", dashboardFile.exists())
        val dashboardContent = dashboardFile.readText()
        assertTrue("Passive legal footer must be present on dashboard", dashboardContent.contains(expectedCore))
    }

    // --- Tier 1 & Tier 2: MDDS Safety Invariant (Zero Bolus Calculators) ---

    @Test
    fun testExemptionMddsGuarantee_absenceOfBolusCalculators() {
        // Inspect all mobile Kotlin source files
        val mobileDir = File("src/main/java")
        val ktFiles = mobileDir.walk().filter { it.extension == "kt" }.toList()
        assertTrue("Mobile Kotlin files must be scanned", ktFiles.isNotEmpty())

        val forbiddenKeywords = listOf(
            "calculateBolus",
            "bolusCalculator",
            "insulinDose",
            "carbRatio",
            "sensitivityFactor",
            "suggestedUnits",
            "insulinCorrection",
            "dosisInsulina"
        )

        for (file in ktFiles) {
            val code = file.readText()
            for (kw in forbiddenKeywords) {
                assertFalse(
                    "Forbidden clinical calculation algorithm '$kw' found in ${file.name}",
                    code.contains(kw, ignoreCase = true)
                )
            }
        }
    }

    // --- Tier 1 & Tier 2: Manifest & Security Config Verification ---

    @Test
    fun testManifestSecurity_allowBackupIsFalse() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        assertTrue("AndroidManifest.xml must exist", manifestFile.exists())
        val manifestContent = manifestFile.readText()

        assertTrue(
            "android:allowBackup must be explicitly set to 'false'",
            manifestContent.contains("android:allowBackup=\"false\"")
        )
    }

    @Test
    fun testManifestSecurity_networkSecurityConfigReferenced() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        val manifestContent = manifestFile.readText()

        assertTrue(
            "networkSecurityConfig must be declared in AndroidManifest.xml",
            manifestContent.contains("android:networkSecurityConfig=\"@xml/network_security_config\"")
        )

        val netSecFile = File("src/main/res/xml/network_security_config.xml")
        assertTrue("network_security_config.xml must exist", netSecFile.exists())
        val netSecContent = netSecFile.readText()
        assertTrue(
            "Base config must disable cleartext traffic",
            netSecContent.contains("cleartextTrafficPermitted=\"false\"")
        )
    }

    // --- Tier 1 & Tier 2: Clinical Design System Color Tokens ---

    @Test
    fun testClinicalThemeColors_hexValues() {
        val colorFile = File("src/main/java/com/example/opengluco/mobile/ui/theme/Color.kt")
        assertTrue("Color.kt must exist", colorFile.exists())
        val colorContent = colorFile.readText()

        assertTrue("OLED pure black background #000000", colorContent.contains("0xFF000000"))
        assertTrue("In-range Mint #4ADE80", colorContent.contains("0xFF4ADE80"))
        assertTrue("Low Coral #F87171", colorContent.contains("0xFFF87171"))
        assertTrue("Urgent Crimson #EF4444", colorContent.contains("0xFFEF4444"))
        assertTrue("High Amber #FBBF24", colorContent.contains("0xFFFBBF24"))
        assertTrue("Very High Tangerine #FB923C", colorContent.contains("0xFFFB923C"))
    }
}
