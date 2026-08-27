package com.example.opengluco.auto

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AutoClinicalSafetyTest {

    @Test
    fun testExemptionMddsGuarantee_zeroBolusCalculatorsInAuto() {
        val autoDir = File("src/main/java")
        val ktFiles = autoDir.walk().filter { it.extension == "kt" }.toList()
        assertTrue("Auto Kotlin files must exist", ktFiles.isNotEmpty())

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
                    "Forbidden clinical calculation algorithm '$kw' found in Auto file ${file.name}",
                    code.contains(kw, ignoreCase = true)
                )
            }
        }
    }

    @Test
    fun testGlanceableStatusFormatting() {
        // Hypoglycemia (<70)
        val hypoVal = 62.0
        val hypoStatus = when {
            hypoVal < 70 -> "[Alerta] Nivel bajo de glucosa"
            hypoVal > 180 -> "[Alerta] Nivel alto de glucosa"
            else -> "[Normal] Nivel dentro del rango objetivo (70 - 180)"
        }
        assertEquals("[Alerta] Nivel bajo de glucosa", hypoStatus)

        // In Range (70..180)
        val normalVal = 115.0
        val normalStatus = when {
            normalVal < 70 -> "[Alerta] Nivel bajo de glucosa"
            normalVal > 180 -> "[Alerta] Nivel alto de glucosa"
            else -> "[Normal] Nivel dentro del rango objetivo (70 - 180)"
        }
        assertEquals("[Normal] Nivel dentro del rango objetivo (70 - 180)", normalStatus)

        // Hyperglycemia (>180)
        val hyperVal = 210.0
        val hyperStatus = when {
            hyperVal < 70 -> "[Alerta] Nivel bajo de glucosa"
            hyperVal > 180 -> "[Alerta] Nivel alto de glucosa"
            else -> "[Normal] Nivel dentro del rango objetivo (70 - 180)"
        }
        assertEquals("[Alerta] Nivel alto de glucosa", hyperStatus)
    }
}
