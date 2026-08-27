package com.example.opengluco.auto

import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import com.example.opengluco.auto.screen.GlucoseDashboardCarScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GlucoseDashboardLegalTest {

    @Test
    fun testLegalDisclaimerConstants() {
        assertEquals(
            "Visualizador pasivo no médico",
            GlucoseDashboardCarScreen.LEGAL_DISCLAIMER_TITLE
        )
        assertEquals(
            "Uso informativo. Prohibido dosificar insulina en conducción.",
            GlucoseDashboardCarScreen.LEGAL_DISCLAIMER_SUBTEXT
        )
    }

    @Test
    fun testPaneRowCapacityWithLegalDisclaimer() {
        val paneBuilder = Pane.Builder()

        // Fila 1: Valor actual y tendencia
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("110 mg/dL  → Estable")
                .addText("Paciente: Principal")
                .build()
        )

        // Fila 2: Estado del rango
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("[Normal] Nivel dentro del rango objetivo (70 - 180)")
                .addText("Última actualización: 12:00")
                .build()
        )

        // Fila 3: Sensor
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Sensor FreeStyle Libre")
                .addText("Días restantes de uso: 10 días")
                .build()
        )

        // Fila 4: Descargo legal pasivo obligatorio
        paneBuilder.addRow(
            Row.Builder()
                .setTitle(GlucoseDashboardCarScreen.LEGAL_DISCLAIMER_TITLE)
                .addText(GlucoseDashboardCarScreen.LEGAL_DISCLAIMER_SUBTEXT)
                .build()
        )

        val pane = paneBuilder.build()
        assertEquals(4, pane.rows.size)
        assertTrue(pane.rows.size <= 4)

        val legalRow = pane.rows[3]
        assertNotNull(legalRow.title)
        assertEquals(GlucoseDashboardCarScreen.LEGAL_DISCLAIMER_TITLE, legalRow.title?.toString())
        assertEquals(1, legalRow.texts.size)
        assertEquals(GlucoseDashboardCarScreen.LEGAL_DISCLAIMER_SUBTEXT, legalRow.texts[0].toString())

        val template = PaneTemplate.Builder(pane)
            .setTitle("OpenGluco Auto")
            .build()
        assertNotNull(template)
        assertEquals(4, template.pane.rows.size)
    }
}
