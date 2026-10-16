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

    // Formateadores inmutables para fechas con guion
    private val DASH_FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US),
        DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US)
    )

    // Formateadores inmutables para fechas con barra y formato AM/PM
    private val SLASH_AMPM_FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("M/d/yyyy hh:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("M/d/yyyy h:mm a", Locale.US),
        DateTimeFormatter.ofPattern("M/d/yyyy hh:mm a", Locale.US),
        DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a", Locale.US),
        DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy h:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy hh:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy h:mm a", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy hh:mm a", Locale.US),
        DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm:ss a", Locale.US),
        DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.US),
        DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a", Locale.US)
    )

    // Formateadores inmutables para fechas con barra y formato 24 horas
    private val SLASH_24H_FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("M/d/yyyy HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy H:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("d/M/yyyy H:mm", Locale.US),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.US)
    )

    // Formateador 24h para interfaz clinica
    private val DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    // Cache LRU acotada para timestamps analizados (evita re-parsing en lecturas recurrentes y purga los mas antiguos)
    private const val MAX_PARSE_CACHE_SIZE = 512
    private val parseCache: MutableMap<String, Long> = java.util.Collections.synchronizedMap(
        object : java.util.LinkedHashMap<String, Long>(128, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?): Boolean = size > MAX_PARSE_CACHE_SIZE
        }
    )

    // Cache LRU acotada para formateo HH:mm por minuto (evita alocaciones JSR-310 en rendering continuo)
    private const val MAX_DISPLAY_CACHE_SIZE = 512
    private val displayTimeCache: MutableMap<Long, String> = java.util.Collections.synchronizedMap(
        object : java.util.LinkedHashMap<Long, String>(128, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, String>?): Boolean = size > MAX_DISPLAY_CACHE_SIZE
        }
    )

    fun parseEpoch(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return 0L

        // Consulta rapida en cache
        parseCache[trimmed]?.let { return it }

        // 1. Marca de tiempo epoch numerica
        val num = trimmed.toLongOrNull()
        if (num != null) {
            val epoch = if (num < 10_000_000_000L) num * 1000L else num
            parseCache[trimmed] = epoch
            return epoch
        }

        val hasT = trimmed.contains('T')
        val hasZ = trimmed.endsWith('Z') || trimmed.endsWith('z')
        val hasSlash = trimmed.contains('/')
        val hasDash = trimmed.contains('-')
        val hasAmPm = trimmed.endsWith("AM", ignoreCase = true) || trimmed.endsWith("PM", ignoreCase = true)

        try {
            // Caso 1: ISO UTC Instant o con offset (ej. 2026-10-05T18:31:31.000Z o +02:00)
            if (hasT) {
                if (hasZ) {
                    val epoch = Instant.parse(trimmed).toEpochMilli()
                    parseCache[trimmed] = epoch
                    return epoch
                }
                if (trimmed.contains('+') || (trimmed.lastIndexOf('-') > trimmed.indexOf('T'))) {
                    try {
                        val epoch = java.time.OffsetDateTime.parse(trimmed).toInstant().toEpochMilli()
                        parseCache[trimmed] = epoch
                        return epoch
                    } catch (_: Exception) {}
                }
            }

            // Caso 2: Fechas con barra '/'
            if (hasSlash) {
                val formatters = if (hasAmPm) SLASH_AMPM_FORMATTERS else SLASH_24H_FORMATTERS
                for (fmt in formatters) {
                    try {
                        val epoch = LocalDateTime.parse(trimmed, fmt).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        parseCache[trimmed] = epoch
                        return epoch
                    } catch (_: Exception) {}
                }
            }

            // Caso 3: Fechas con guion '-'
            if (hasDash) {
                for (fmt in DASH_FORMATTERS) {
                    try {
                        val epoch = try {
                            LocalDateTime.parse(trimmed, fmt).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        } catch (_: Exception) {
                            java.time.LocalDate.parse(trimmed, fmt).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        }
                        parseCache[trimmed] = epoch
                        return epoch
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}

        // Cachear resultado negativo para evitar loops de excepciones en fallos repetidos
        parseCache[trimmed] = 0L
        return 0L
    }

    fun formatDisplayTime(epochMs: Long): String {
        if (epochMs <= 0L) return "Ahora"
        val minuteBucket = epochMs / 60_000L
        displayTimeCache[minuteBucket]?.let { return it }

        val formatted = try {
            Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).format(DISPLAY_TIME_FORMATTER)
        } catch (_: Exception) {
            "Ahora"
        }
        displayTimeCache[minuteBucket] = formatted
        return formatted
    }
}
