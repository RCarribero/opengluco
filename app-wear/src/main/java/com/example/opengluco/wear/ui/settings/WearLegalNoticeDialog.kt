package com.example.opengluco.wear.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import com.example.opengluco.wear.ui.theme.ClinicalBackground
import com.example.opengluco.wear.ui.theme.ClinicalLowCoral
import com.example.opengluco.wear.ui.theme.ClinicalMint
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceBorder
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceCard
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceOrb
import com.example.opengluco.wear.ui.theme.ClinicalTextMuted
import com.example.opengluco.wear.ui.theme.ClinicalTextPrimary
import com.example.opengluco.wear.ui.theme.ClinicalTextSecondary

enum class WearLegalNoticeType {
    NONE,
    MEDICAL_DISCLAIMER,
    TRADEMARKS,
    PRIVACY_GDPR,
    DELETE_CONFIRMATION
}

object WearLegalTexts {
    const val PASSIVE_FOOTER = "Visualizador secundario pasivo. No es un dispositivo médico y no sustituye al lector oficial ni a decisiones clínicas profesionales."

    // Descargo Médico (MDR UE 2017/745 / FDA MDDS)
    const val MEDICAL_BADGE = "MDR UE 2017/745 / FDA MDDS"
    const val MEDICAL_TITLE = "Visualizador Secundario de Conveniencia"
    const val MEDICAL_CONTENT = "Esta aplicación es un visor secundario pasivo desarrollado con fines exclusivamente informativos y de seguimiento personal.\n\n" +
            "1. NO es un Dispositivo Médico: Este software no está certificado como dispositivo médico bajo el Reglamento de la UE (MDR 2017/745) ni por la FDA.\n\n" +
            "2. Prohibido para Dosificación: NUNCA use las lecturas, tendencias o gráficas de esta app para calcular dosis de insulina, ajustar tratamientos o tomar decisiones médicas críticas.\n\n" +
            "3. Comprobación Capilar Obligatoria: Ante cualquier síntoma, duda o discrepancia, confirme siempre su nivel con una prueba capilar de glucosa en sangre o mediante su lector oficial FreeStyle Libre."

    // Marcas y No Afiliación
    const val TRADEMARK_BADGE = "Uso Legítimo Nominativo"
    const val TRADEMARK_TITLE = "Titularidad de Marcas y No Afiliación"
    const val TRADEMARK_CONTENT = "FreeStyle, Libre, LibreLink, LibreLinkUp, LibreView y sus logotipos asociados son marcas registradas de Abbott Laboratories y/o Abbott Diabetes Care Inc.\n\n" +
            "• OpenGluco es un proyecto comunitario independiente y NO está patrocinado, afiliado, autorizado ni respaldado de ninguna manera por Abbott Laboratories.\n\n" +
            "• La mención a dichas marcas se realiza exclusivamente para describir la compatibilidad técnica e interoperabilidad del software con el servicio del usuario."

    // Privacidad y Salud (RGPD Art. 9)
    const val GDPR_BADGE = "RGPD Art. 9 / LOPDGDD"
    const val GDPR_TITLE = "Tratamiento y Protección de Datos de Salud"
    const val GDPR_CONTENT = "Sus lecturas de glucosa constituyen datos relativos a la salud protegidos bajo el Artículo 9 del RGPD.\n\n" +
            "1. Arquitectura 100% Local (Local-First): Sus credenciales y telemetría histórica se guardan cifradas exclusivamente en la memoria segura de su propio dispositivo mediante Android Keystore.\n\n" +
            "2. Cero Servidores Intermediarios: La app se comunica directamente con los servidores oficiales de Abbott. Ningún tercero ni el desarrollador recopila, retransmite ni almacena sus datos.\n\n" +
            "3. Derechos de Portabilidad y Supresión: Dispone de funciones integradas para exportar su historial a CSV o borrarlo de forma definitiva e irreversible."

    // Purga de Datos (RGPD Art. 17)
    const val DELETE_CONFIRM_TITLE = "[Aviso] Acción Destructiva Irreversible"
    const val DELETE_CONFIRM_BODY = "Se eliminarán permanentemente todas las lecturas de glucosa acumuladas de los últimos 90 días, la caché local y las credenciales de sesión guardadas en este dispositivo.\n\n¿Está seguro de que desea continuar?"
    const val DELETE_CONFIRM_BTN_CANCEL = "Cancelar"
    const val DELETE_CONFIRM_BTN_CONFIRM = "Borrar Todo"
}

@Composable
fun WearPassiveLegalFooter(modifier: Modifier = Modifier) {
    Text(
        text = WearLegalTexts.PASSIVE_FOOTER,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        color = ClinicalTextMuted,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
fun WearLegalNoticeDialog(
    type: WearLegalNoticeType,
    onDismiss: () -> Unit,
    onConfirmDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (type == WearLegalNoticeType.NONE) return

    val scrollState = rememberScalingLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicalBackground.copy(alpha = 0.97f)),
        contentAlignment = Alignment.Center
    ) {
        ScalingLazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Header Title
            item {
                val (title, color) = when (type) {
                    WearLegalNoticeType.MEDICAL_DISCLAIMER -> Pair("Descargo Médico", ClinicalMint)
                    WearLegalNoticeType.TRADEMARKS -> Pair("Marcas Registradas", ClinicalMint)
                    WearLegalNoticeType.PRIVACY_GDPR -> Pair("Privacidad RGPD", ClinicalMint)
                    WearLegalNoticeType.DELETE_CONFIRMATION -> Pair("Borrar Datos Locales", ClinicalLowCoral)
                    WearLegalNoticeType.NONE -> Pair("", ClinicalTextPrimary)
                }

                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Body Content
            when (type) {
                WearLegalNoticeType.MEDICAL_DISCLAIMER -> {
                    item {
                        WearLegalSectionBox(
                            badge = WearLegalTexts.MEDICAL_BADGE,
                            title = WearLegalTexts.MEDICAL_TITLE,
                            content = WearLegalTexts.MEDICAL_CONTENT
                        )
                    }
                }

                WearLegalNoticeType.TRADEMARKS -> {
                    item {
                        WearLegalSectionBox(
                            badge = WearLegalTexts.TRADEMARK_BADGE,
                            title = WearLegalTexts.TRADEMARK_TITLE,
                            content = WearLegalTexts.TRADEMARK_CONTENT
                        )
                    }
                }

                WearLegalNoticeType.PRIVACY_GDPR -> {
                    item {
                        WearLegalSectionBox(
                            badge = WearLegalTexts.GDPR_BADGE,
                            title = WearLegalTexts.GDPR_TITLE,
                            content = WearLegalTexts.GDPR_CONTENT
                        )
                    }
                }

                WearLegalNoticeType.DELETE_CONFIRMATION -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClinicalLowCoral.copy(alpha = 0.12f))
                                .border(1.dp, ClinicalLowCoral.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = WearLegalTexts.DELETE_CONFIRM_TITLE,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = ClinicalLowCoral,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = WearLegalTexts.DELETE_CONFIRM_BODY,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = ClinicalTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ClinicalLowCoral)
                                    .clickable {
                                        onDismiss()
                                        onConfirmDelete?.invoke()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = WearLegalTexts.DELETE_CONFIRM_BTN_CONFIRM,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ClinicalSurfaceCard)
                                    .border(1.dp, ClinicalSurfaceBorder, RoundedCornerShape(12.dp))
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = WearLegalTexts.DELETE_CONFIRM_BTN_CANCEL,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ClinicalTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                WearLegalNoticeType.NONE -> {}
            }

            if (type != WearLegalNoticeType.DELETE_CONFIRMATION) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ClinicalSurfaceCard)
                            .border(1.dp, ClinicalSurfaceBorder, RoundedCornerShape(12.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Entendido",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClinicalMint,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun WearLegalSectionBox(
    badge: String,
    title: String,
    content: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ClinicalSurfaceOrb)
            .border(1.dp, ClinicalSurfaceBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ClinicalMint.copy(alpha = 0.15f))
                    .border(1.dp, ClinicalMint.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClinicalMint,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ClinicalTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = content,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = ClinicalTextSecondary,
                textAlign = TextAlign.Start
            )
        }
    }
}
