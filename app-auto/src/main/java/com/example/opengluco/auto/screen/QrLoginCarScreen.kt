package com.example.opengluco.auto.screen

import android.graphics.Bitmap
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.opengluco.auto.AutoMobileSyncHelper
import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.model.QrDeviceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ServerSocket

class QrLoginCarScreen(carContext: CarContext) : Screen(carContext) {

    private var qrBitmap: Bitmap? = null
    private var pairingPayloadJson: String = ""
    private var serverSocket: ServerSocket? = null
    private val prefsRepo = UserPreferencesRepository(carContext)
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        val payload = QrAuthHelper.createPairingPayload(
            deviceType = QrDeviceType.ANDROID_AUTO,
            deviceName = "Android Auto Car Screen"
        )
        pairingPayloadJson = QrAuthHelper.serializePairingPayload(payload)
        qrBitmap = QrAuthHelper.generateQrBitmap(pairingPayloadJson, sizePx = 400)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                if (serverSocket == null || serverSocket?.isClosed == true) {
                    serverSocket = QrAuthHelper.startPairingServer(
                        port = payload.port ?: 8888,
                        secretKeyHex = payload.secretKeyHex,
                        nonceHex = payload.nonceHex,
                        expectedSessionId = payload.sessionId
                    ) { session ->
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                prefsRepo.saveAuthSession(
                                    email = session.email,
                                    token = session.token,
                                    userId = session.userId,
                                    phoneMac = session.phoneBluetoothMac
                                )
                            }
                            screenManager.push(GlucoseDashboardCarScreen(carContext))
                        }
                    }
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                try { serverSocket?.close() } catch (_: Exception) {}
                serverSocket = null
            }

            override fun onDestroy(owner: LifecycleOwner) {
                try { serverSocket?.close() } catch (_: Exception) {}
                serverSocket = null
            }
        })
    }

    override fun onGetTemplate(): Template {
        val paneBuilder = Pane.Builder()

        val qrIcon = if (qrBitmap != null) {
            CarIcon.Builder(IconCompat.createWithBitmap(qrBitmap!!)).build()
        } else null

        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Iniciar sesión en tu coche")
                .addText("1. Abre OpenGluco en tu teléfono móvil.")
                .addText("2. Pulsa en 'Vincular Reloj / Coche'.")
                .addText("3. Escanea el código que ves en este salpicadero.")
                .apply {
                    if (qrIcon != null) {
                        setImage(qrIcon)
                    }
                }
                .build()
        )

        paneBuilder.addAction(
            Action.Builder()
                .setTitle("Comprobar conexión")
                .setOnClickListener {
                    scope.launch {
                        AutoMobileSyncHelper.syncSessionFromMobile(carContext, prefsRepo)
                        screenManager.push(GlucoseDashboardCarScreen(carContext))
                    }
                }
                .build()
        )

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Vincular Android Auto")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}
