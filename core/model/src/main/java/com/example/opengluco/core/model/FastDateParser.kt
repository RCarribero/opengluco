package com.example.opengluco.core.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Parser de marcas de tiempo de alto rendimiento optimizado para el ecosistema OpenGluco.
 * Utiliza formateadores inmutables de Java 8+ (JSR-310) reutilizables, thread-safe y sin alocaciones repetidas.
 */
object FastDateParser {

    private val SYSTEM_ZONE: ZoneId = ZoneId.systemDefault()

    // Formateadores inmutables para fechas con guion
    private val DASH_FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
    )

    // Formateadores inmutables para fechas con barra y formato AM/PM
    private val SLASH_AMPM_FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("M/d/yyyy h:mm a", Locale.US),
        DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy h:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy h:mm a", Locale.US),
        DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a", Locale.US)
    )

    // Formateadores inmutables para fechas con barra y formato 24 horas
    private val SLASH_24H_FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("M/d/yyyy HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.US)
    )

    // Formateador 24h para interfaz clinica
    private val DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    fun parseEpoch(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return 0L

        // 1. Marca de tiempo epoch numerica
        val num = trimmed.toLongOrNull()
        if (num != null) {
            return if (num < 10_000_000_000L) num * 1000L else num
        }

        val hasT = trimmed.contains('T')
        val hasZ = trimmed.endsWith('Z') || trimmed.endsWith('z')
        val hasSlash = trimmed.contains('/')
        val hasDash = trimmed.contains('-')
        val hasAmPm = trimmed.endsWith("AM", ignoreCase = true) || trimmed.endsWith("PM", ignoreCase = true)

        try {
            // Caso 1: ISO UTC Instant (ej. 2026-10-05T18:31:31.000Z o 2026-10-05T18:31:31Z)
            if (hasT && hasZ) {
                return Instant.parse(trimmed).toEpochMilli()
            }

            // Caso 2: Fechas con barra '/'
            if (hasSlash) {
                val formatters = if (hasAmPm) SLASH_AMPM_FORMATTERS else SLASH_24H_FORMATTERS
                for (fmt in formatters) {
                    try {
                        return LocalDateTime.parse(trimmed, fmt).atZone(SYSTEM_ZONE).toInstant().toEpochMilli()
                    } catch (_: Exception) {}
                }
            }

            // Caso 3: Fechas con guion '-'
            if (hasDash) {
                for (fmt in DASH_FORMATTERS) {
                    try {
                        return LocalDateTime.parse(trimmed, fmt).atZone(SYSTEM_ZONE).toInstant().toEpochMilli()
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}

        return 0L
    }

    fun formatDisplayTime(epochMs: Long): String {
        if (epochMs <= 0L) return "Ahora"
        return try {
            Instant.ofEpochMilli(epochMs).atZone(SYSTEM_ZONE).format(DISPLAY_TIME_FORMATTER)
        } catch (_: Exception) {
            "Ahora"
        }
    }
}
