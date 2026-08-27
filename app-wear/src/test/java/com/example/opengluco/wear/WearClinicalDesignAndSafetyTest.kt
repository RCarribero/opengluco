package com.example.opengluco.wear

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class WearClinicalDesignAndSafetyTest {

    @Test
    fun testExemptionMddsGuarantee_zeroBolusCalculatorsInWear() {
        val wearDir = File("src/main/java")
        val ktFiles = wearDir.walk().filter { it.extension == "kt" }.toList()
        assertTrue("Wear Kotlin files must exist", ktFiles.isNotEmpty())

        val forbiddenKeywords = listOf(
            "calculateBolus",
            "bolusCalculator",
            "insulinDose",
            "carbRatio",
            "sensitivityFactor",
            "suggestedUnits",
            "dosisInsulina"
        )

        for (file in ktFiles) {
            val code = file.readText()
            for (kw in forbiddenKeywords) {
                assertFalse(
                    "Forbidden clinical calculation algorithm '$kw' found in Wear file ${file.name}",
                    code.contains(kw, ignoreCase = true)
                )
            }
        }
    }

    @Test
    fun testWearManifestSecurity() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        assertTrue("Wear AndroidManifest.xml must exist", manifestFile.exists())
        val manifestContent = manifestFile.readText()

        assertTrue("android:allowBackup must be false in Wear", manifestContent.contains("android:allowBackup=\"false\""))
        assertTrue("networkSecurityConfig must be set in Wear", manifestContent.contains("android:networkSecurityConfig=\"@xml/network_security_config\""))
    }

    @Test
    fun testWearClinicalColorTokens() {
        val colorFile = File("src/main/java/com/example/opengluco/wear/ui/theme/Color.kt")
        assertTrue("Wear Color.kt must exist", colorFile.exists())
        val colorContent = colorFile.readText()

        assertTrue("Pure OLED Black #000000", colorContent.contains("0xFF000000"))
        assertTrue("Surface Orb #1E232D", colorContent.contains("0xFF1E232D"))
        assertTrue("Surface Border #2D3748", colorContent.contains("0xFF2D3748"))
        assertTrue("Clinical Mint #4ADE80", colorContent.contains("0xFF4ADE80"))
        assertTrue("Clinical Coral #F87171", colorContent.contains("0xFFF87171"))
        assertTrue("Clinical Crimson #EF4444", colorContent.contains("0xFFEF4444"))
        assertTrue("Clinical Amber #FBBF24", colorContent.contains("0xFFFBBF24"))
        assertTrue("Clinical Cyan #38BDF8", colorContent.contains("0xFF38BDF8"))
    }
}
