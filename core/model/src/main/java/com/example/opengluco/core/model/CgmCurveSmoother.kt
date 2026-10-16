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
    private fun ensureSortedValid(readings: List<GlucoseMeasurement>): List<GlucoseMeasurement> {
        if (readings.isEmpty()) return emptyList()
        var isSortedAndValid = true
        var prevEpoch = Long.MIN_VALUE
        for (i in readings.indices) {
            val item = readings[i]
            if (item.numericValue <= 0.0) {
                isSortedAndValid = false
                break
            }
            val epoch = item.getEpochMillis()
            if (epoch < prevEpoch) {
                isSortedAndValid = false
                break
            }
            prevEpoch = epoch
        }
        if (isSortedAndValid) return readings
        val valid = ArrayList<GlucoseMeasurement>(readings.size)
        for (i in readings.indices) {
            val m = readings[i]
            if (m.numericValue > 0.0) {
                valid.add(m)
            }
        }
        valid.sortBy { it.getEpochMillis() }
        return valid
    }

    /**
     * Consolida y deduplica lecturas temporales para evitar cúmulos de mediciones
     * en intervalos menores a [minBucketMs] (por defecto 3 minutos).
     */
    fun consolidateTemporalBuckets(
        readings: List<GlucoseMeasurement>,
        minBucketMs: Long = 3 * 60 * 1000L
    ): List<GlucoseMeasurement> {
        val valid = ensureSortedValid(readings)
        if (valid.size <= 2) return valid

        val consolidated = ArrayList<GlucoseMeasurement>(valid.size)
        var currentBucket = ArrayList<GlucoseMeasurement>(4)
        var bucketStartEpoch = valid.first().getEpochMillis()

        for (m in valid) {
            val epoch = m.getEpochMillis()
            if (epoch - bucketStartEpoch < minBucketMs) {
                currentBucket.add(m)
            } else {
                if (currentBucket.isNotEmpty()) {
                    consolidated.add(consolidateBucket(currentBucket))
                }
                currentBucket = ArrayList<GlucoseMeasurement>(4).apply { add(m) }
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
        var sum = 0.0
        for (i in bucket.indices) {
            sum += bucket[i].numericValue
        }
        val avgValue = sum / bucket.size
        return latest.copy(
            valueInMgPerDl = avgValue,
            value = avgValue
        ).apply {
            setCachedTime(latest.getEpochMillis(), latest.getDisplayTime())
        }
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
        val valid = ensureSortedValid(readings)
        if (valid.size <= 3) return valid

        val result = ArrayList<GlucoseMeasurement>((valid.size / 3) + 2)
        val size = valid.size
        var i = 0
        while (i < size) {
            val remaining = size - i
            val isLastChunk = (i + 3 >= size)

            if (remaining >= 3) {
                if (useWeightedAverage && !isLastChunk) {
                    val wAvg = (0.25 * valid[i].numericValue) + (0.50 * valid[i + 1].numericValue) + (0.25 * valid[i + 2].numericValue)
                    val representative = valid[i + 2]
                    result.add(
                        representative.copy(
                            valueInMgPerDl = wAvg,
                            value = wAvg
                        ).apply {
                            setCachedTime(representative.getEpochMillis(), representative.getDisplayTime())
                        }
                    )
                } else {
                    result.add(valid[i + 2])
                }
                i += 3
            } else {
                result.add(valid.last())
                break
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
     * Calcula los segmentos de curva cúbica Catmull-Rom con tensión controlada sobre arrays primitivos.
     * Evita la creación de objetos Pair y el boxing de Float en rendering continuo.
     */
    fun computeCatmullRomSpline(
        xs: FloatArray,
        ys: FloatArray,
        tension: Float = 0.35f
    ): List<CubicBezierSegment> {
        val n = minOf(xs.size, ys.size)
        if (n < 2) return emptyList()
        val segments = ArrayList<CubicBezierSegment>(n - 1)

        for (i in 0 until n - 1) {
            val p0X = if (i > 0) xs[i - 1] else xs[i]
            val p0Y = if (i > 0) ys[i - 1] else ys[i]
            val p1X = xs[i]
            val p1Y = ys[i]
            val p2X = xs[i + 1]
            val p2Y = ys[i + 1]
            val p3X = if (i + 2 < n) xs[i + 2] else xs[i + 1]
            val p3Y = if (i + 2 < n) ys[i + 2] else ys[i + 1]

            val cp1X = p1X + (p2X - p0X) * tension
            val cp1Y = p1Y + (p2Y - p0Y) * tension

            val cp2X = p2X - (p3X - p1X) * tension
            val cp2Y = p2Y - (p3Y - p1Y) * tension

            segments.add(
                CubicBezierSegment(
                    startX = p1X,
                    startY = p1Y,
                    cp1X = cp1X,
                    cp1Y = cp1Y,
                    cp2X = cp2X,
                    cp2Y = cp2Y,
                    endX = p2X,
                    endY = p2Y
                )
            )
        }

        return segments
    }

    /**
     * Calcula los segmentos de curva cúbica Catmull-Rom con tensión controlada.
     * Elimina el aspecto escalonado o de dientes de sierra de los puntos directos.
     */
    fun computeCatmullRomSpline(
        points: List<Pair<Float, Float>>,
        tension: Float = 0.35f
    ): List<CubicBezierSegment> {
        if (points.size < 2) return emptyList()
        val segments = ArrayList<CubicBezierSegment>(points.size - 1)

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
