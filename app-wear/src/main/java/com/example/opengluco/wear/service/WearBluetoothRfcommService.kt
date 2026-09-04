package com.example.opengluco.wear.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.opengluco.core.data.AlarmEvaluator
import com.example.opengluco.core.data.AlarmRepository
import com.example.opengluco.core.data.KeystoreCryptoHelper
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.wear.notification.WearAlarmNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

/**
 * Servicio en segundo plano para recepción de stream continuo de telemetría de glucosa
 * mediante socket Bluetooth RFCOMM (SPP) con verificación estricta de la dirección MAC
 * del teléfono emisor antes de procesar cualquier dato.
 */
class WearBluetoothRfcommService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var serverSocket: BluetoothServerSocket? = null
    private var isRunning = false

    private lateinit var userPrefs: UserPreferencesRepository

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun onCreate() {
        super.onCreate()
        userPrefs = UserPreferencesRepository(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        if (!isRunning) {
            isRunning = true
            startRfcommListener()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sincronización Bluetooth RFCOMM",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Recepción de stream de telemetría de glucosa continua"
                enableVibration(false)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("OpenGluco Stream")
            .setContentText("Escuchando telemetría por Bluetooth...")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startRfcommListener() {
        serviceScope.launch {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter == null || !adapter.isEnabled) {
                Log.w(TAG, "Bluetooth no disponible o desactivado")
                return@launch
            }

            while (isActive && isRunning) {
                var currentSocket: BluetoothSocket? = null
                try {
                    serverSocket = adapter.listenUsingRfcommWithServiceRecord(
                        SERVICE_NAME,
                        OPENGLUCO_RFCOMM_UUID
                    )
                    Log.i(TAG, "Servidor RFCOMM iniciado en UUID: $OPENGLUCO_RFCOMM_UUID")

                    currentSocket = serverSocket?.accept()
                    serverSocket?.close() // Cierra el server socket tras aceptar el cliente activo

                    if (currentSocket != null) {
                        handleIncomingConnection(currentSocket)
                    }
                } catch (e: Exception) {
                    if (isActive && isRunning) {
                        Log.w(TAG, "Error en ciclo de servidor RFCOMM: ${e.message}")
                        kotlinx.coroutines.delay(RECONNECT_DELAY_MS)
                    }
                } finally {
                    try { currentSocket?.close() } catch (_: Exception) {}
                }
            }
        }
    }

    private suspend fun handleIncomingConnection(socket: BluetoothSocket) {
        val remoteDevice = socket.remoteDevice
        val remoteMac = remoteDevice?.address.orEmpty()

        Log.i(TAG, "Conexión RFCOMM entrante desde dispositivo: $remoteMac")

        // 1. Verificación estricta de la dirección MAC del teléfono
        val isTrusted = userPrefs.isTrustedPhoneMac(remoteMac)
        if (!isTrusted) {
            Log.w(TAG, "ACCESO RECHAZADO: Dirección MAC [$remoteMac] no coincide con el teléfono de confianza.")
            try {
                socket.close()
            } catch (_: Exception) {}
            return
        }

        Log.i(TAG, "Identidad verificada exitosamente para MAC: $remoteMac. Procesando stream...")

        // 2. Procesamiento de stream continuo de datos
        try {
            val reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))
            while (serviceScope.isActive && socket.isConnected) {
                val line = reader.readLine() ?: break
                if (line.isNotBlank()) {
                    processStreamPayload(line.trim())
                }
            }
        } catch (e: Exception) {
            Log.i(TAG, "Finalizado stream RFCOMM: ${e.message}")
        }
    }

    private suspend fun processStreamPayload(rawPayload: String) {
        try {
            val decryptedPayload = if (rawPayload.startsWith("ENC:")) {
                KeystoreCryptoHelper.decrypt(rawPayload)
            } else {
                rawPayload
            }

            if (decryptedPayload.isBlank()) return

            // Parsear como medición individual
            val measurement = json.decodeFromString<GlucoseMeasurement>(decryptedPayload)
            userPrefs.saveHistoricalReadings(listOf(measurement))

            val numVal = measurement.numericValue
            val trend = measurement.trendArrow ?: 3
            val ts = measurement.timestamp ?: measurement.factoryTimestamp ?: ""

            if (numVal > 0) {
                userPrefs.saveLastMeasurement(numVal, trend, ts)

                val settings = userPrefs.userSettingsFlow.first()
                if (settings.hapticAlertsEnabled) {
                    val alarmRepo = AlarmRepository(applicationContext)
                    val alarms = alarmRepo.getAllAlarms()
                    val timestamps = alarmRepo.getLastFiredTimestamps()

                    val result = AlarmEvaluator.evaluate(
                        currentValueMgDl = numVal.toDouble(),
                        alarms = alarms,
                        lastFiredTimestamps = timestamps
                    )

                    val triggered = result.triggeredAlarm
                    if (triggered != null) {
                        WearAlarmNotificationHelper.triggerAlarmBySeverity(
                            context = applicationContext,
                            alarm = triggered,
                            glucoseValueMgDl = numVal.toDouble()
                        )
                        alarmRepo.recordAlarmFired(triggered.id)
                    }
                }
            }
            Log.d(TAG, "Medición de stream procesada y deduplicada: $numVal mg/dL")
        } catch (e: Exception) {
            Log.w(TAG, "Error procesando trama de telemetría: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serviceJob.cancel()
        Log.i(TAG, "Servicio Bluetooth RFCOMM detenido")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "WearBluetoothRfcomm"
        private const val SERVICE_NAME = "OpenGlucoStream"
        private const val CHANNEL_ID = "cgm_rfcomm_channel"
        private const val NOTIFICATION_ID = 4040
        private const val RECONNECT_DELAY_MS = 5000L

        const val ACTION_STOP = "com.example.opengluco.wear.STOP_RFCOMM_STREAM"

        // UUID estándar SPP para enlace serie Bluetooth RFCOMM
        val OPENGLUCO_RFCOMM_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        fun start(context: Context) {
            val intent = Intent(context, WearBluetoothRfcommService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, WearBluetoothRfcommService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
