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
import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.core.model.QrDeviceType

class QrLoginCarScreen(carContext: CarContext) : Screen(carContext) {

    private var qrBitmap: Bitmap? = null
    private var pairingPayloadJson: String = ""

    init {
        val payload = QrAuthHelper.createPairingPayload(
            deviceType = QrDeviceType.ANDROID_AUTO,
            deviceName = "Android Auto Car Screen"
        )
        pairingPayloadJson = QrAuthHelper.serializePairingPayload(payload)
        qrBitmap = QrAuthHelper.generateQrBitmap(pairingPayloadJson, sizePx = 400)
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
                    screenManager.push(GlucoseDashboardCarScreen(carContext))
                }
                .build()
        )

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Vincular Android Auto")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}
