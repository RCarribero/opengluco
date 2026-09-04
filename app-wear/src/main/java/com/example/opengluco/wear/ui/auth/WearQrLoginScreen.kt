package com.example.opengluco.wear.ui.auth

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.Text
import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.model.QrDeviceType
import com.example.opengluco.core.model.QrSessionExchange
import com.example.opengluco.wear.service.WearBluetoothRfcommService
import com.example.opengluco.wear.ui.theme.ClinicalBackground
import com.example.opengluco.wear.ui.theme.ClinicalMint
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceBorder
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceOrb
import com.example.opengluco.wear.ui.theme.ClinicalTextSecondary
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.ServerSocket

/**
 * Pantalla de inicio de sesión y emparejamiento con verificación numérica SAS (6 dígitos)
 * y confirmación interactiva con apagado total de sockets para ahorro de batería.
 */
@Composable
fun WearQrLoginScreen(
    preferencesRepository: UserPreferencesRepository,
    onLoginSuccess: () -> Unit,
    onManualLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var serverSocket by remember { mutableStateOf<ServerSocket?>(null) }
    var verificationCode by remember { mutableStateOf("") }
    var pendingSession by remember { mutableStateOf<QrSessionExchange?>(null) }
    var isSyncing by remember { mutableStateOf(false) }
    var showQrView by remember { mutableStateOf(false) }

    fun confirmAndPersistSession(session: QrSessionExchange) {
        scope.launch {
            isSyncing = true
            preferencesRepository.saveAuthSession(
                email = session.email,
                token = session.token,
                userId = session.userId,
                phoneMac = session.phoneBluetoothMac
            )
            // Iniciar servicio de stream continuo tras guardar sesión
            WearBluetoothRfcommService.start(context)
            delay(400)
            onLoginSuccess()
        }
    }

    // Generar código numérico SAS e iniciar servidor de escucha local temporal
    LaunchedEffect(Unit) {
        val payload = QrAuthHelper.createPairingPayload(
            deviceType = QrDeviceType.WEAR_OS,
            deviceName = "Galaxy Watch",
            port = 8888
        )
        verificationCode = payload.verificationCode
        val jsonPayload = QrAuthHelper.serializePairingPayload(payload)
        qrBitmap = QrAuthHelper.generateQrBitmap(jsonPayload, sizePx = 512)

        // 1. Canal Directo por Socket Local Cifrado con AES-256-GCM
        serverSocket = QrAuthHelper.startPairingServer(
            port = 8888,
            secretKeyHex = payload.secretKeyHex,
            nonceHex = payload.nonceHex
        ) { session ->
            pendingSession = session
        }

        // 2. Anuncio automático a todos los teléfonos conectados por Google Play Services Wearable
        try {
            val nodeClient = Wearable.getNodeClient(context)
            val messageClient = Wearable.getMessageClient(context)
            nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                val bytes = jsonPayload.toByteArray(Charsets.UTF_8)
                for (node in nodes) {
                    messageClient.sendMessage(node.id, "/opengluco_pairing_request", bytes)
                }
            }
        } catch (_: Exception) {}
    }

    // 2. Canal Wearable MessageClient (DataLayer)
    DisposableEffect(Unit) {
        val messageClient = Wearable.getMessageClient(context)
        val listener = MessageClient.OnMessageReceivedListener { messageEvent ->
            if (messageEvent.path == "/opengluco_auth_sync") {
                val rawJson = String(messageEvent.data, Charsets.UTF_8)
                val session = QrAuthHelper.parseSessionExchange(rawJson)
                if (session != null) {
                    pendingSession = session
                }
            }
        }

        messageClient.addListener(listener)
        onDispose {
            // Destrucción total de sockets y listeners para cero consumo de batería
            messageClient.removeListener(listener)
            try { serverSocket?.close() } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicalBackground),
        contentAlignment = Alignment.Center
    ) {
        when {
            isSyncing -> {
                // Estado de Sincronización Exitosa
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    ClinicalProgressSpinner(
                        color = ClinicalMint,
                        strokeWidth = 8f,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡Vinculado!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClinicalMint,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cargando glucosa...",
                        fontSize = 11.sp,
                        color = ClinicalTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            pendingSession != null -> {
                // Modal de Confirmación de Emparejamiento por Tick
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = "Código de Enlace",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ClinicalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = verificationCode,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClinicalMint,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¿Coincide con tu móvil?",
                        fontSize = 10.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Rechazar (Cruz)
                        IconButton(
                            onClick = { pendingSession = null },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color(0xFF2D3748),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Rechazar",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Botón Aceptar (Tick verde)
                        IconButton(
                            onClick = { pendingSession?.let { confirmAndPersistSession(it) } },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = ClinicalMint,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Aceptar",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            showQrView && qrBitmap != null -> {
                // Vista QR Alternativa
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .size(135.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Código QR",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toca para ver código",
                        fontSize = 10.sp,
                        color = ClinicalTextSecondary,
                        modifier = Modifier
                            .clickable { showQrView = false }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            else -> {
                // Vista Principal: Código Numérico de 6 Dígitos
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = "Vincular Reloj",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClinicalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (verificationCode.isNotBlank()) verificationCode else "--- ---",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClinicalMint,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Confirma cuando coincida",
                        fontSize = 9.sp,
                        color = ClinicalTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Ver QR
                        IconButton(
                            onClick = { showQrView = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = ClinicalSurfaceOrb,
                                contentColor = ClinicalMint
                            ),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Ver QR",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Botón Login Manual
                        Text(
                            text = "Login",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = ClinicalTextSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ClinicalSurfaceOrb)
                                .clickable { onManualLogin() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClinicalProgressSpinner(
    modifier: Modifier = Modifier,
    color: Color = ClinicalMint,
    strokeWidth: Float = 8f
) {
    val transition = rememberInfiniteTransition(label = "spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Canvas(modifier = modifier) {
        drawArc(
            color = Color(0xFF1E232D),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
