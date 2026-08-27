package com.example.opengluco.mobile.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.opengluco.mobile.ui.theme.ClinicalTheme

enum class LegalNoticeType {
    NONE,
    MEDICAL_DISCLAIMER,
    TRADEMARKS,
    PRIVACY_GDPR,
    DELETE_CONFIRMATION
}

@Composable
fun LegalNoticeDialog(
    type: LegalNoticeType,
    onDismiss: () -> Unit,
    onConfirmDelete: (() -> Unit)? = null
) {
    if (type == LegalNoticeType.NONE) return
    val colors = ClinicalTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val headerTitle = when (type) {
                        LegalNoticeType.MEDICAL_DISCLAIMER -> "Aviso Médico Importante"
                        LegalNoticeType.TRADEMARKS -> "Marcas y No Afiliación"
                        LegalNoticeType.PRIVACY_GDPR -> "Privacidad y Salud (RGPD)"
                        LegalNoticeType.DELETE_CONFIRMATION -> "Borrar Todos los Datos"
                        LegalNoticeType.NONE -> ""
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = headerTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (type) {
                        LegalNoticeType.MEDICAL_DISCLAIMER -> {
                            LegalSectionBox(
                                badge = "MDR UE 2017/745 / FDA MDDS",
                                title = "Visualizador Secundario de Conveniencia",
                                content = "Esta aplicación es un visor secundario pasivo desarrollado con fines exclusivamente informativos y de seguimiento personal.\n\n" +
                                        "1. NO es un Dispositivo Médico: Este software no está certificado como dispositivo médico bajo el Reglamento de la UE (MDR 2017/745) ni por la FDA.\n\n" +
                                        "2. Prohibido para Dosificación: NUNCA use las lecturas, tendencias o gráficas de esta app para calcular dosis de insulina, ajustar tratamientos o tomar decisiones médicas críticas.\n\n" +
                                        "3. Comprobación Capilar Obligatoria: Ante cualquier síntoma, duda o discrepancia, confirme siempre su nivel con una prueba capilar de glucosa en sangre o mediante su lector oficial FreeStyle Libre."
                            )
                        }

                        LegalNoticeType.TRADEMARKS -> {
                            LegalSectionBox(
                                badge = "Uso Legítimo Nominativo",
                                title = "Titularidad de Marcas de Terceros",
                                content = "FreeStyle, Libre, LibreLink, LibreLinkUp, LibreView y sus logotipos son marcas registradas de Abbott Laboratories y/o Abbott Diabetes Care Inc.\n\n" +
                                        "• OpenGluco es un proyecto comunitario independiente y NO está patrocinado, afiliado, autorizado ni respaldado de ninguna manera por Abbott.\n\n" +
                                        "• La mención a dichas marcas se realiza exclusivamente para describir la compatibilidad técnica e interoperabilidad del software con el servicio del usuario."
                            )
                        }

                        LegalNoticeType.PRIVACY_GDPR -> {
                            LegalSectionBox(
                                badge = "RGPD Art. 9 / LOPDGDD",
                                title = "Tratamiento y Protección de Datos de Salud",
                                content = "Sus lecturas de glucosa constituyen datos relativos a la salud protegidos bajo el Artículo 9 del RGPD.\n\n" +
                                        "1. Arquitectura 100% Local (Local-First): Sus credenciales y telemetría histórica se guardan cifradas exclusivamente en la memoria segura de su propio dispositivo mediante Android Keystore.\n\n" +
                                        "2. Cero Servidores Intermediarios: La app se comunica directamente con los servidores oficiales de Abbott. Ningún tercero ni el desarrollador recopila, retransmite ni almacena sus datos.\n\n" +
                                        "3. Derechos de Portabilidad y Supresión: Dispone de funciones integradas para exportar su historial a CSV o borrarlo de forma definitiva e irreversible."
                            )
                        }

                        LegalNoticeType.DELETE_CONFIRMATION -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.lowCoral.copy(alpha = 0.12f))
                                    .border(1.dp, colors.lowCoral.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "[Aviso] Acción Destructiva Irreversible",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.lowCoral
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Se eliminarán permanentemente todas las lecturas de glucosa acumuladas de los últimos 90 días, la caché local y las credenciales de sesión guardadas en este dispositivo.\n\n¿Está seguro de que desea continuar?",
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = colors.textPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.surfaceCard)
                                        .border(1.dp, colors.surfaceBorder, RoundedCornerShape(10.dp))
                                        .clickable { onDismiss() }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Cancelar", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.lowCoral)
                                        .clickable {
                                            onDismiss()
                                            onConfirmDelete?.invoke()
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Borrar Todo", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        LegalNoticeType.NONE -> {}
                    }
                }

                if (type != LegalNoticeType.DELETE_CONFIRMATION) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.mint,
                            contentColor = if (colors.isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Entendido", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegalSectionBox(
    badge: String,
    title: String,
    content: String
) {
    val colors = ClinicalTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceCard)
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.mint.copy(alpha = 0.15f))
                    .border(1.dp, colors.mint.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.mint
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = content,
                fontSize = 11.5.sp,
                lineHeight = 17.sp,
                color = colors.textSecondary
            )
        }
    }
}
