package com.example.opengluco.mobile.ui.qr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.core.model.QrDeviceType
import com.example.opengluco.core.model.QrPairingPayload
import com.example.opengluco.mobile.ui.theme.ClinicalTheme
import com.google.android.gms.wearable.Wearable
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    userEmail: String,
    userToken: String,
    userId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val colors = ClinicalTheme.colors

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var scannedPayload by remember { mutableStateOf<QrPairingPayload?>(null) }
    var syncSuccess by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Vincular Dispositivo (QR)", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = colors.mint)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary,
                    navigationIconContentColor = colors.mint
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.background)
        ) {
            if (hasCameraPermission) {
                if (scannedPayload == null) {
                    CameraPreview(
                        onQrDetected = { rawQr ->
                            Log.d("QrScanner", "QR detectado en cámara: $rawQr")
                            val payload = QrAuthHelper.parsePairingPayload(rawQr)
                            if (payload != null && scannedPayload == null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scannedPayload = payload
                            }
                        }
                    )

                    // Marco de escaneo visual clínico
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .align(Alignment.Center)
                            .border(BorderStroke(3.dp, colors.mint), RoundedCornerShape(20.dp))
                    )

                    Text(
                        text = "Apunta al código QR en la pantalla de tu Reloj Samsung o Coche",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp, start = 32.dp, end = 32.dp)
                    )
                } else if (!syncSuccess) {
                    // Modal de Verificación Numérica (SAS / Numeric Comparison)
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                        border = BorderStroke(1.dp, colors.surfaceBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = if (scannedPayload?.deviceType == QrDeviceType.WEAR_OS) Icons.Default.Watch else Icons.Default.DirectionsCar,
                                contentDescription = "Dispositivo",
                                tint = colors.mint,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Código de Verificación",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = scannedPayload?.verificationCode?.takeIf { it.isNotBlank() } ?: "--- ---",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.mint,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Comprueba que este código coincide con el mostrado en tu dispositivo. Confirma la vinculación para iniciar la transferencia cifrada.",
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { scannedPayload = null },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.surfaceBorder,
                                        contentColor = colors.textPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Rechazar", fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        val payload = scannedPayload
                                        if (payload != null) {
                                            transferSessionToDevice(context, payload, userEmail, userToken, userId) {
                                                syncSuccess = true
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.mint,
                                        contentColor = if (colors.isDark) Color.Black else Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Autorizar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Modal de Éxito al vincular
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                        border = BorderStroke(1.dp, colors.surfaceBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = if (scannedPayload?.deviceType == QrDeviceType.WEAR_OS) Icons.Default.Watch else Icons.Default.DirectionsCar,
                                contentDescription = "Dispositivo",
                                tint = colors.mint,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Dispositivo Vinculado",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Canal seguro establecido con ${scannedPayload?.deviceName}. Las lecturas se sincronizarán de forma continua.",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onNavigateBack,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.mint,
                                    contentColor = if (colors.isDark) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Volver al Monitor", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Se requiere permiso de cámara para escanear el código QR de vinculación.",
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.mint,
                            contentColor = if (colors.isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Conceder Permiso", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun transferSessionToDevice(
    context: android.content.Context,
    payload: QrPairingPayload,
    email: String,
    token: String,
    userId: String,
    onSuccess: () -> Unit
) {
    MobilePairingHelper.transferSessionToDevice(context, payload, email, token, userId, onSuccess)
}

@Composable
private fun CameraPreview(
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val hints = mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                    DecodeHintType.TRY_HARDER to true,
                    DecodeHintType.CHARACTER_SET to "UTF-8"
                )
                val reader = MultiFormatReader().apply { setHints(hints) }

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    val qrCode = decodeQrFromImage(imageProxy, reader)
                    if (qrCode != null) {
                        previewView.post { onQrDetected(qrCode) }
                    }
                    imageProxy.close()
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (_: Exception) {}
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier.fillMaxSize()
    )
}

private fun decodeQrFromImage(imageProxy: ImageProxy, reader: MultiFormatReader): String? {
    if (imageProxy.format !in listOf(ImageFormat.YUV_420_888, ImageFormat.YUV_422_888, ImageFormat.YUV_444_888)) {
        return null
    }

    val rotation = imageProxy.imageInfo.rotationDegrees
    val buffer = imageProxy.planes[0].buffer
    val data = ByteArray(buffer.remaining())
    buffer.get(data)

    val width = imageProxy.width
    val height = imageProxy.height

    // Rotar datos YUV según la orientación del sensor para ZXing
    val rotatedData = when (rotation) {
        90 -> rotateYUV420Degree90(data, width, height)
        180 -> rotateYUV420Degree180(data, width, height)
        270 -> rotateYUV420Degree270(data, width, height)
        else -> data
    }
    val rotatedWidth = if (rotation == 90 || rotation == 270) height else width
    val rotatedHeight = if (rotation == 90 || rotation == 270) width else height

    val source = PlanarYUVLuminanceSource(rotatedData, rotatedWidth, rotatedHeight, 0, 0, rotatedWidth, rotatedHeight, false)
    val bitmap = BinaryBitmap(HybridBinarizer(source))

    return try {
        val result = reader.decodeWithState(bitmap)
        result.text
    } catch (_: Exception) {
        null
    } finally {
        reader.reset()
    }
}

private fun rotateYUV420Degree90(data: ByteArray, imageWidth: Int, imageHeight: Int): ByteArray {
    val yuv = ByteArray(imageWidth * imageHeight)
    var i = 0
    for (x in 0 until imageWidth) {
        for (y in imageHeight - 1 downTo 0) {
            yuv[i++] = data[y * imageWidth + x]
        }
    }
    return yuv
}

private fun rotateYUV420Degree180(data: ByteArray, imageWidth: Int, imageHeight: Int): ByteArray {
    val yuv = ByteArray(imageWidth * imageHeight)
    var i = 0
    for (y in imageHeight - 1 downTo 0) {
        for (x in imageWidth - 1 downTo 0) {
            yuv[i++] = data[y * imageWidth + x]
        }
    }
    return yuv
}

private fun rotateYUV420Degree270(data: ByteArray, imageWidth: Int, imageHeight: Int): ByteArray {
    val yuv = ByteArray(imageWidth * imageHeight)
    var i = 0
    for (x in imageWidth - 1 downTo 0) {
        for (y in 0 until imageHeight) {
            yuv[i++] = data[y * imageWidth + x]
        }
    }
    return yuv
}
