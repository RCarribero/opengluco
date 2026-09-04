package com.example.opengluco.core.model

import kotlin.math.abs

/**
 * Filtro clínico de procesamiento de señales para curvas de glucosa continua (CGM).
 * Elimina el ruido de micro-muestreo ("dientes de sierra") y suaviza la curva
 * preservando fielmente los picos reales, hipoglucemias y la dirección de tendencia.
 */
object CgmCurveSmoother {

    /**
     * Consolida y deduplica lecturas temporales para evitar cúmulos de mediciones
     * en intervalos menores a [minBucketMs] (por defecto 3 minutos).
     */
    fun consolidateTemporalBuckets(
        readings: List<GlucoseMeasurement>,
        minBucketMs: Long = 3 * 60 * 1000L
    ): List<GlucoseMeasurement> {
        val valid = readings.filter { it.numericValue > 0.0 }.sortedBy { it.getEpochMillis() }
        if (valid.size <= 2) return valid

        val consolidated = mutableListOf<GlucoseMeasurement>()
        var currentBucket = mutableListOf<GlucoseMeasurement>()
        var bucketStartEpoch = valid.first().getEpochMillis()

        for (m in valid) {
            val epoch = m.getEpochMillis()
            if (epoch - bucketStartEpoch < minBucketMs) {
                currentBucket.add(m)
            } else {
                if (currentBucket.isNotEmpty()) {
                    consolidated.add(consolidateBucket(currentBucket))
                }
                currentBucket = mutableListOf(m)
                bucketStartEpoch = epoch
            }
        }

        if (currentBucket.isNotEmpty()) {
            consolidated.add(consolidateBucket(currentBucket))
        }

        return consolidated
    }

    private fun consolidateBucket(bucket: List<GlucoseMeasurement>): GlucoseMeasurement {
        if (bucket.size == 1) return bucket.first()
        val latest = bucket.last()
        val avgValue = bucket.map { it.numericValue }.average()
        return latest.copy(
            valueInMgPerDl = avgValue,
            value = avgValue
        )
    }

    /**
     * Muestrea las lecturas reduciendo a 1 punto por cada 3 lecturas recibidas.
     * Elimina el micro-ruido de muestreo y evita curvas con dientes de sierra ("muchos picos").
     * Si [useWeightedAverage] es true, aplica ponderación fisiológica (0.25, 0.50, 0.25)
     * a cada terna de 3 lecturas consecutivas para mayor estabilidad clínica.
     * Preserva siempre intacta la última lectura en vivo para coincidir exactamente
     * con la medición instantánea del sensor.
     */
    fun subsampleOneOfThree(
        readings: List<GlucoseMeasurement>,
        useWeightedAverage: Boolean = true
    ): List<GlucoseMeasurement> {
        val valid = readings.filter { it.numericValue > 0.0 }.sortedBy { it.getEpochMillis() }
        if (valid.size <= 3) return valid

        val result = mutableListOf<GlucoseMeasurement>()
        val chunks = valid.chunked(3)

        for (i in chunks.indices) {
            val chunk = chunks[i]
            val isLastChunk = (i == chunks.lastIndex)

            if (chunk.size == 3) {
                if (useWeightedAverage && !isLastChunk) {
                    val wAvg = (0.25 * chunk[0].numericValue) + (0.50 * chunk[1].numericValue) + (0.25 * chunk[2].numericValue)
                    val representative = chunk[2]
                    result.add(
                        representative.copy(
                            valueInMgPerDl = wAvg,
                            value = wAvg
                        )
                    )
                } else {
                    result.add(chunk.last())
                }
            } else {
                result.add(chunk.last())
            }
        }

        // Preservar exactamente la última medición real en vivo
        val lastReal = valid.last()
        if (result.isEmpty() || result.last().getEpochMillis() != lastReal.getEpochMillis()) {
            result.add(lastReal)
        } else {
            result[result.lastIndex] = lastReal
        }

        return result
    }

    /**
     * Aplica un suavizado Gaussiano fisiológico de 3 puntos:
     * y_i = 0.20 * y_{i-1} + 0.60 * y_i + 0.20 * y_{i+1}
     * Preserva intacta la última medición en vivo para coincidir exactamente con el valor instantáneo.
     */
    fun smoothMeasurements(readings: List<GlucoseMeasurement>): List<GlucoseMeasurement> {
        if (readings.size < 3) return readings

        val smoothed = ArrayList<GlucoseMeasurement>(readings.size)
        smoothed.add(readings.first())

        for (i in 1 until readings.size - 1) {
            val prev = readings[i - 1].numericValue
            val curr = readings[i].numericValue
            val next = readings[i + 1].numericValue

            // Filtro de media ponderada Gaussiana fisiológica
            val filteredVal = (0.20 * prev) + (0.60 * curr) + (0.20 * next)

            smoothed.add(
                readings[i].copy(
                    valueInMgPerDl = filteredVal,
                    value = filteredVal
                )
            )
        }

        // La última medición se preserva intacta para coincidir con la lectura instantánea
        smoothed.add(readings.last())
        return smoothed
    }

    /**
     * Estructura de puntos de control Bézier cúbicos para trazar curvas suaves Catmull-Rom.
     */
    data class CubicBezierSegment(
        val startX: Float,
        val startY: Float,
        val cp1X: Float,
        val cp1Y: Float,
        val cp2X: Float,
        val cp2Y: Float,
        val endX: Float,
        val endY: Float
    )

    /**
     * Calcula los segmentos de curva cúbica Catmull-Rom con tensión controlada.
     * Elimina el aspecto escalonado o de dientes de sierra de los puntos directos.
     */
    fun computeCatmullRomSpline(
        points: List<Pair<Float, Float>>,
        tension: Float = 0.35f
    ): List<CubicBezierSegment> {
        if (points.size < 2) return emptyList()
        val segments = mutableListOf<CubicBezierSegment>()

        val n = points.size
        for (i in 0 until n - 1) {
            val p0 = if (i > 0) points[i - 1] else points[i]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = if (i + 2 < n) points[i + 2] else points[i + 1]

            val cp1X = p1.first + (p2.first - p0.first) * tension
            val cp1Y = p1.second + (p2.second - p0.second) * tension

            val cp2X = p2.first - (p3.first - p1.first) * tension
            val cp2Y = p2.second - (p3.second - p1.second) * tension

            segments.add(
                CubicBezierSegment(
                    startX = p1.first,
                    startY = p1.second,
                    cp1X = cp1X,
                    cp1Y = cp1Y,
                    cp2X = cp2X,
                    cp2Y = cp2Y,
                    endX = p2.first,
                    endY = p2.second
                )
            )
        }

        return segments
    }
}
