package com.example.opengluco.auto

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Gestor de alertas por sintesis de voz (TTS) para Android Auto.
 * Proporciona avisos auditivos sobrios y breves para minimizar la distraccion visual al volante.
 */
class AutoTtsAlertManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var lastSpokenTimestamp = 0L
    private var lastSpokenValue = 0.0

    companion object {
        private const val MIN_SPOKEN_COOLDOWN_MS = 3 * 60 * 1000L // 3 minutos de cooldown entre avisos de voz
        private const val UTTERANCE_ID_ALARM = "opengluco_car_alarm_utterance"
    }

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts?.setAudioAttributes(audioAttributes)
            isInitialized = true
        }
    }

    /**
     * Evalua si debe vocalizar un aviso de glucosa critico para el conductor.
     */
    fun speakGlucoseAlertIfNeeded(
        glucoseMgDl: Double,
        trendText: String,
        lowThreshold: Double = 70.0,
        highThreshold: Double = 180.0
    ) {
        if (!isInitialized || glucoseMgDl <= 0.0) return

        val now = System.currentTimeMillis()
        val timeSinceLast = now - lastSpokenTimestamp
        val isCriticalLow = glucoseMgDl <= 55.0
        val isLow = glucoseMgDl < lowThreshold
        val isUrgentHigh = glucoseMgDl >= 250.0
        val isHigh = glucoseMgDl > highThreshold

        // Solo vocalizar si es un estado fuera de rango
        if (!isCriticalLow && !isLow && !isUrgentHigh && !isHigh) return

        // Cooldown: si el valor no ha cambiado significativamente y estamos dentro del cooldown, omitir
        val valueDiff = kotlin.math.abs(glucoseMgDl - lastSpokenValue)
        if (timeSinceLast < MIN_SPOKEN_COOLDOWN_MS && valueDiff < 10.0 && !isCriticalLow) {
            return
        }

        val speechText = when {
            isCriticalLow -> "Atencion. Nivel urgente muy bajo de glucosa: ${glucoseMgDl.toInt()} miligramos por decilitro. Tendencia $trendText."
            isLow -> "Aviso: Glucosa baja, ${glucoseMgDl.toInt()} miligramos por decilitro. Tendencia $trendText."
            isUrgentHigh -> "Aviso: Glucosa muy alta, ${glucoseMgDl.toInt()} miligramos por decilitro."
            else -> "Aviso: Glucosa alta, ${glucoseMgDl.toInt()} miligramos por decilitro."
        }

        lastSpokenTimestamp = now
        lastSpokenValue = glucoseMgDl

        val params = Bundle()
        tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_ID_ALARM)
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (_: Exception) {}
    }
}
