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
                        LegalNoticeType.MEDICAL_DISCLAIMER -> "Descargo de Responsabilidad Médica"
                        LegalNoticeType.TRADEMARKS -> "Propiedad Intelectual y Marcas"
                        LegalNoticeType.PRIVACY_GDPR -> "Protección de Datos Sanitarios (RGPD)"
                        LegalNoticeType.DELETE_CONFIRMATION -> "Eliminar Historial Local"
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
                                content = "OpenGluco opera como un visor secundario pasivo desarrollado exclusivamente para monitorización informativa y de seguimiento personal.\n\n" +
                                        "1. Marco Regulatorio: No constituye un dispositivo médico certificado bajo el Reglamento Europeo MDR UE 2017/745 ni directrices FDA MDDS.\n\n" +
                                        "2. Prohibido para Dosificación: Está estrictamente prohibido utilizar lecturas, curvas o estimaciones para calcular dosis de insulina o pautas terapéuticas críticas.\n\n" +
                                        "3. Comprobación Capilar Obligatoria: Ante cualquier síntoma de malestar, sospecha o variación brusca, confirme siempre sus niveles mediante una prueba capilar de glucosa en sangre o lector oficial FreeStyle Libre."
                            )
                        }

                        LegalNoticeType.TRADEMARKS -> {
                            LegalSectionBox(
                                badge = "Uso Legítimo Nominativo",
                                title = "Titularidad de Marcas",
                                content = "FreeStyle, Libre, LibreLink, LibreLinkUp, LibreView y logotipos asociados son marcas registradas de Abbott Laboratories y/o Abbott Diabetes Care Inc.\n\n" +
                                        "• OpenGluco es un proyecto independiente y NO está patrocinado, afiliado, autorizado ni respaldado en forma alguna por Abbott Laboratories.\n\n" +
                                        "• Las referencias a dichas marcas se realizan conforme a la doctrina de Uso Legítimo Nominativo para describir la interoperabilidad técnica con el servicio del usuario."
                            )
                        }

                        LegalNoticeType.PRIVACY_GDPR -> {
                            LegalSectionBox(
                                badge = "RGPD Art. 9 / LOPDGDD",
                                title = "Tratamiento y Protección de Datos de Salud",
                                content = "Las lecturas de telemetría constituyen datos sensibles relativos a la salud amparados por el RGPD Art. 9 y normativas aplicables.\n\n" +
                                        "1. Arquitectura 100% Local (Local-First): Credenciales y mediciones se custodian exclusivamente en su dispositivo mediante Android Keystore con cifrado de hardware.\n\n" +
                                        "2. Cero Servidores Intermediarios: La comunicación se establece de forma directa con los servidores de Abbott. Ninguna infraestructura externa recopila ni almacena sus lecturas.\n\n" +
                                        "3. Control y Portabilidad: Puede exportar su histórico íntegro en formato CSV o destruirlo permanentemente en cualquier instante."
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
                                        text = "Se suprimirán de manera definitiva todas las lecturas de glucosa de los últimos 90 días, las sesiones y la caché de cifrado local almacenadas en este dispositivo.\n\n¿Desea proceder con la eliminación completa?",
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
                        Text("Cerrar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
