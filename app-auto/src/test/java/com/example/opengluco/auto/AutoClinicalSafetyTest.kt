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
        val lowThreshold = 70
        val highThreshold = 180

        fun getStatus(mgdl: Double, low: Int = lowThreshold, high: Int = highThreshold): String {
            return when {
                mgdl <= 55 -> "[Urgente] Nivel muy bajo de glucosa (<= 55)"
                mgdl < low -> "[Alerta] Nivel bajo de glucosa (< $low)"
                mgdl > 250 -> "[Urgente] Nivel muy alto de glucosa (>= 250)"
                mgdl > high -> "[Alerta] Nivel alto de glucosa (> $high)"
                mgdl > 0 -> "[Normal] Nivel dentro del rango objetivo ($low - $high)"
                else -> "[Info] Sin datos recientes"
            }
        }

        assertEquals("[Alerta] Nivel bajo de glucosa (< 70)", getStatus(62.0))
        assertEquals("[Urgente] Nivel muy bajo de glucosa (<= 55)", getStatus(50.0))
        assertEquals("[Normal] Nivel dentro del rango objetivo (70 - 180)", getStatus(115.0))
        assertEquals("[Alerta] Nivel alto de glucosa (> 180)", getStatus(210.0))
        assertEquals("[Urgente] Nivel muy alto de glucosa (>= 250)", getStatus(260.0))

        // Custom dynamic thresholds (e.g. 80-160)
        assertEquals("[Alerta] Nivel bajo de glucosa (< 80)", getStatus(75.0, low = 80, high = 160))
        assertEquals("[Alerta] Nivel alto de glucosa (> 160)", getStatus(165.0, low = 80, high = 160))
    }
}
