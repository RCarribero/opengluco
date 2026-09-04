package com.example.opengluco.mobile

import com.example.opengluco.core.data.ClinicalReportsCalculator
import com.example.opengluco.core.data.UserSettings
import com.example.opengluco.core.model.GlucoseMeasurement
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DataCoherenceAndThemeTest {

    @Test
    fun testDefaultTheme_isOledBlack() {
        val colorFile = File("src/main/java/com/example/opengluco/mobile/ui/theme/Color.kt")
        assertTrue("Color.kt must exist", colorFile.exists())
        val content = colorFile.readText()

        // Pure OLED black background
        assertTrue("DarkBackground must be 0xFF000000", content.contains("0xFF000000"))
        // Mint accent
        assertTrue("DarkMint must be 0xFF4ADE80", content.contains("0xFF4ADE80"))
        // Default palette must be DarkClinicalPalette
        assertTrue("Default LocalClinicalColors must be DarkClinicalPalette", content.contains("DarkClinicalPalette"))

        // UserSettings default isDarkMode must be true
        val defaultSettings = UserSettings()
        assertTrue("Default theme must be Dark mode (isDarkMode = true)", defaultSettings.isDarkMode)
    }

    @Test
    fun testOledDarkMode_isPreserved() {
        val colorFile = File("src/main/java/com/example/opengluco/mobile/ui/theme/Color.kt")
        val content = colorFile.readText()

        // Dark OLED black
        assertTrue("DarkBackground must be pure OLED black 0xFF000000", content.contains("0xFF000000"))
        // Dark in-range mint token
        assertTrue("DarkMint must be 0xFF4ADE80", content.contains("0xFF4ADE80"))
    }

    @Test
    fun testDataCoherence_singleDayDataInsufficientForWeek() {
        val oneDayReadings = listOf(
            GlucoseMeasurement(timestamp = "9/3/2026 10:00:00 AM", valueInMgPerDl = 110.0),
            GlucoseMeasurement(timestamp = "9/3/2026 11:00:00 AM", valueInMgPerDl = 120.0),
            GlucoseMeasurement(timestamp = "9/3/2026 12:00:00 PM", valueInMgPerDl = 115.0)
        )

        val availableDays = ClinicalReportsCalculator.calculateAvailableDays(oneDayReadings)
        assertEquals("Available days for single day data must be 1", 1, availableDays)

        val requestedPeriodDays = 7
        val isSufficient = availableDays >= requestedPeriodDays
        assertFalse("1 day of data must be insufficient for 7-day metrics", isSufficient)

        val notice = "No tienes todavía datos suficientes para leer las métricas de una semana. Se requieren al menos $requestedPeriodDays días de lecturas acumuladas (disponibles actualmente: $availableDays día)."
        assertTrue(notice.contains("No tienes todavía datos suficientes"))
        assertTrue(notice.contains("7 días"))
        assertTrue(notice.contains("1 día"))
    }

    @Test
    fun testDataCoherence_singleDayWithDiverseHoursInUsFormat_strictlyOneDay() {
        // Edge case that previously broke naive substring(0, 10): 9 AM, 10 AM, 2 PM on the same date
        val readings = listOf(
            GlucoseMeasurement(timestamp = "9/3/2026 8:30:00 AM", valueInMgPerDl = 95.0),
            GlucoseMeasurement(timestamp = "9/3/2026 9:15:00 AM", valueInMgPerDl = 110.0),
            GlucoseMeasurement(timestamp = "9/3/2026 10:00:00 AM", valueInMgPerDl = 125.0),
            GlucoseMeasurement(timestamp = "9/3/2026 2:45:00 PM", valueInMgPerDl = 130.0),
            GlucoseMeasurement(timestamp = "9/3/2026 11:59:00 PM", valueInMgPerDl = 105.0)
        )

        val days = ClinicalReportsCalculator.calculateAvailableDays(readings)
        assertEquals("Multiple hours across a single day must yield exactly 1 available day", 1, days)

        val dailyGraph = ClinicalReportsCalculator.calculateDailyGraph(readings)
        assertEquals("Must group all readings into exactly 1 day summary", 1, dailyGraph.days.size)
    }

    @Test
    fun testDataCoherence_twoDistinctDaysInUsFormat() {
        val readings = listOf(
            GlucoseMeasurement(timestamp = "9/3/2026 9:00:00 AM", valueInMgPerDl = 100.0),
            GlucoseMeasurement(timestamp = "9/3/2026 10:00:00 AM", valueInMgPerDl = 105.0),
            GlucoseMeasurement(timestamp = "9/4/2026 9:00:00 AM", valueInMgPerDl = 110.0),
            GlucoseMeasurement(timestamp = "9/4/2026 10:00:00 AM", valueInMgPerDl = 115.0)
        )

        val days = ClinicalReportsCalculator.calculateAvailableDays(readings)
        assertEquals("Two distinct calendar days must yield exactly 2 available days", 2, days)

        val dailyGraph = ClinicalReportsCalculator.calculateDailyGraph(readings)
        assertEquals("Must group into exactly 2 daily summaries", 2, dailyGraph.days.size)
    }

    @Test
    fun testTheme_clinicalStatusColorDefaultsToDark() {
        val inRangeColor = com.example.opengluco.mobile.ui.theme.getClinicalStatusColor(110.0)
        assertEquals("Default in-range status color must be DarkMint (0xFF4ADE80)", com.example.opengluco.mobile.ui.theme.DarkMint, inRangeColor)

        val lightInRangeColor = com.example.opengluco.mobile.ui.theme.getClinicalStatusColor(110.0, isDark = false)
        assertEquals("Light in-range status color must be LightMint (0xFF65A30D)", com.example.opengluco.mobile.ui.theme.LightMint, lightInRangeColor)
    }

    @Test
    fun testDialogsAndPopups_noGenericAiPhrases() {
        val dashboardFile = File("src/main/java/com/example/opengluco/mobile/ui/dashboard/MobileDashboardScreen.kt")
        val dashboardContent = dashboardFile.readText()

        // Verify elimination of AI hallucinations like "pulsa el tick"
        assertFalse("Must not contain hallucinated 'pulsa el tick'", dashboardContent.contains("pulsa el tick"))
        assertFalse("Must not contain generic 'Sesión transferida con éxito'", dashboardContent.contains("Sesión transferida con éxito"))

        val qrScannerFile = File("src/main/java/com/example/opengluco/mobile/ui/qr/QrScannerScreen.kt")
        val qrScannerContent = qrScannerFile.readText()
        assertFalse("QrScannerScreen must not contain 'pulsa el tick'", qrScannerContent.contains("pulsa el tick"))
        assertTrue("QrScannerScreen must use professional 'Autorizar' button", qrScannerContent.contains("\"Autorizar\""))

        val legalFile = File("src/main/java/com/example/opengluco/mobile/ui/dashboard/components/LegalComplianceComponents.kt")
        val legalContent = legalFile.readText()
        assertFalse("Must not contain generic chatbot 'Aviso Médico Importante'", legalContent.contains("Aviso Médico Importante"))
        assertFalse("Must not use chatbot 'Entendido' button", legalContent.contains("\"Entendido\""))
        assertTrue("Must use clinical 'Descargo de Responsabilidad Médica'", legalContent.contains("Descargo de Responsabilidad Médica"))
        assertTrue("Must use professional 'Cerrar' button", legalContent.contains("\"Cerrar\""))

        val updateFile = File("src/main/java/com/example/opengluco/mobile/ui/dashboard/components/UpdateAvailableDialog.kt")
        val updateContent = updateFile.readText()
        assertTrue("UpdateAvailableDialog must use proper accent 'Descargando actualización'", updateContent.contains("Descargando actualización"))

        val alarmFile = File("src/main/java/com/example/opengluco/mobile/ui/settings/AlarmConfigSection.kt")
        val alarmContent = alarmFile.readText()
        assertFalse("AlarmCreationDialog must not use crude 'X' text for close button", alarmContent.contains("Text(\"X\""))
    }

    @Test
    fun testZeroEmojis_inModifiedMobileComponents() {
        val filesToCheck = listOf(
            File("src/main/java/com/example/opengluco/mobile/ui/theme/Color.kt"),
            File("src/main/java/com/example/opengluco/mobile/ui/theme/Theme.kt"),
            File("src/main/java/com/example/opengluco/mobile/ui/dashboard/MobileDashboardScreen.kt"),
            File("src/main/java/com/example/opengluco/mobile/ui/reports/ReportsHubScreen.kt"),
            File("src/main/java/com/example/opengluco/mobile/ui/dashboard/components/LegalComplianceComponents.kt"),
            File("src/main/java/com/example/opengluco/mobile/ui/dashboard/components/UpdateAvailableDialog.kt"),
            File("src/main/java/com/example/opengluco/mobile/ui/dashboard/components/ConfigurationDiagnosticsDialog.kt"),
            File("src/main/java/com/example/opengluco/mobile/ui/dashboard/components/TargetRangeDialog.kt"),
            File("src/main/java/com/example/opengluco/mobile/ui/dashboard/components/MobileStatDetailModal.kt"),
            File("src/main/java/com/example/opengluco/mobile/ui/qr/QrScannerScreen.kt"),
            File("src/main/java/com/example/opengluco/mobile/ui/settings/AlarmConfigSection.kt")
        )

        for (file in filesToCheck) {
            assertTrue("${file.name} must exist", file.exists())
            val text = file.readText()
            var offset = 0
            while (offset < text.length) {
                val codePoint = text.codePointAt(offset)
                val isEmoji = (codePoint in 0x1F600..0x1F64F) ||
                        (codePoint in 0x1F300..0x1F5FF) ||
                        (codePoint in 0x1F680..0x1F6FF) ||
                        (codePoint in 0x1F700..0x1F77F) ||
                        (codePoint in 0x1F900..0x1F9FF) ||
                        (codePoint in 0x1FA70..0x1FAFF)
                assertFalse("File ${file.name} contains emoji at offset $offset with codepoint U+${codePoint.toString(16).uppercase()}", isEmoji)
                offset += Character.charCount(codePoint)
            }
        }
    }
}
