package com.example.opengluco.core.data

import android.content.Context
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.opengluco.core.model.BlockMetric
import com.example.opengluco.core.model.DailyPatternsReport
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.GlucoseEventMarker
import com.example.opengluco.core.model.GlucoseEventType
import com.example.opengluco.core.model.PeriodSummary
import com.example.opengluco.core.model.ReportTimeBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.math.roundToInt

/**
 * Base de datos SQLite embebida de alta velocidad (< 5 ms) en modo WAL para el ecosistema OpenGluco.
 * Persiste lecturas de glucosa acumulativas hasta 90 dias indexadas por (patient_id, epoch_ms).
 *
 * Dispone de almacenamiento transaccional indexado y fallback seguro en memoria para entornos de prueba.
 */
class LocalGlucoseDatabase(
    context: Context? = null,
    dbName: String = DATABASE_NAME
) : SQLiteOpenHelper(context, dbName, null, DATABASE_VERSION) {


    companion object {
        const val DATABASE_NAME = "opengluco_glucose.db"
        const val DATABASE_VERSION = 3

        const val TABLE_NAME = "glucose_measurements"
        const val COL_PATIENT_ID = "patient_id"
        const val COL_EPOCH_MS = "epoch_ms"
        const val COL_VALUE = "value"
        const val COL_TREND_ARROW = "trend_arrow"
        const val COL_TREND_MESSAGE = "trend_message"
        const val COL_COLOR = "measurement_color"
        const val COL_UNITS = "glucose_units"
        const val COL_TIMESTAMP = "raw_timestamp"
        const val COL_FACTORY_TIMESTAMP = "factory_timestamp"
        const val COL_IS_HIGH = "is_high"
        const val COL_IS_LOW = "is_low"
        private const val EVENT_TABLE_NAME = "glucose_event_markers"
        private const val EVENT_COL_ID = "event_id"
        private const val EVENT_COL_PATIENT_ID = "patient_id"
        private const val EVENT_COL_TIMESTAMP_MS = "timestamp_ms"
        private const val EVENT_COL_TYPE = "event_type"
        private const val EVENT_COL_ENCRYPTED_NOTE = "encrypted_note"

        private const val CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COL_PATIENT_ID TEXT NOT NULL,
                $COL_EPOCH_MS INTEGER NOT NULL,
                $COL_VALUE REAL NOT NULL,
                $COL_TREND_ARROW INTEGER NOT NULL DEFAULT 3,
                $COL_TREND_MESSAGE TEXT,
                $COL_COLOR INTEGER DEFAULT 1,
                $COL_UNITS INTEGER DEFAULT 1,
                $COL_TIMESTAMP TEXT,
                $COL_FACTORY_TIMESTAMP TEXT,
                $COL_IS_HIGH INTEGER DEFAULT 0,
                $COL_IS_LOW INTEGER DEFAULT 0,
                PRIMARY KEY ($COL_PATIENT_ID, $COL_EPOCH_MS)
            )
        """

        private const val CREATE_INDEX_SQL = """
            CREATE INDEX IF NOT EXISTS idx_patient_epoch ON $TABLE_NAME ($COL_PATIENT_ID, $COL_EPOCH_MS DESC)
        """

        private const val CREATE_EPOCH_INDEX_SQL = """
            CREATE INDEX IF NOT EXISTS idx_epoch ON $TABLE_NAME ($COL_EPOCH_MS DESC)
        """

        private const val CREATE_EVENT_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $EVENT_TABLE_NAME (
                $EVENT_COL_ID TEXT PRIMARY KEY NOT NULL,
                $EVENT_COL_PATIENT_ID TEXT NOT NULL,
                $EVENT_COL_TIMESTAMP_MS INTEGER NOT NULL,
                $EVENT_COL_TYPE TEXT NOT NULL,
                $EVENT_COL_ENCRYPTED_NOTE TEXT
            )
        """

        private const val CREATE_EVENT_INDEX_SQL = """
            CREATE INDEX IF NOT EXISTS idx_event_patient_time ON $EVENT_TABLE_NAME ($EVENT_COL_PATIENT_ID, $EVENT_COL_TIMESTAMP_MS DESC)
        """
    }

    // In-memory fallback for JVM testing where SQLiteOpenHelper stubs return null
    private val inMemoryStorage = ConcurrentHashMap<String, ConcurrentSkipListMap<Long, GlucoseMeasurement>>()
    private val inMemoryEventMarkers = ConcurrentHashMap<String, ConcurrentHashMap<String, GlucoseEventMarker>>()

    // Flow notifier for reactive updates
    private val _dbUpdateEvents = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val dbUpdateEvents = _dbUpdateEvents.asSharedFlow()

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        try {
            db.enableWriteAheadLogging()
        } catch (_: Exception) {}
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SQL)
        db.execSQL(CREATE_INDEX_SQL)
        db.execSQL(CREATE_EPOCH_INDEX_SQL)
        db.execSQL(CREATE_EVENT_TABLE_SQL)
        db.execSQL(CREATE_EVENT_INDEX_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_EVENT_TABLE_SQL)
            db.execSQL(CREATE_EVENT_INDEX_SQL)
        }
        if (oldVersion < 3) {
            db.execSQL(CREATE_EPOCH_INDEX_SQL)
        }
    }

    private fun getSafeWritableDatabase(): SQLiteDatabase? {
        return try {
            writableDatabase
        } catch (_: Exception) {
            null
        }
    }

    private fun getSafeReadableDatabase(): SQLiteDatabase? {
        return try {
            readableDatabase
        } catch (_: Exception) {
            null
        }
    }

    private fun parseTimestamp(ts: String?): Long? {
        if (ts.isNullOrBlank()) return null
        ts.toLongOrNull()?.let { return if (it < 10_000_000_000L) it * 1000L else it }

        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm",
            "M/d/yyyy h:mm:ss a",
            "M/d/yyyy hh:mm:ss a",
            "MM/dd/yyyy hh:mm:ss a",
            "M/d/yyyy H:mm:ss",
            "M/d/yyyy HH:mm:ss",
            "M/d/yyyy h:mm a",
            "d/M/yyyy h:mm:ss a",
            "d/M/yyyy HH:mm:ss",
            "d/M/yyyy H:mm:ss",
            "d/M/yyyy h:mm a"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                if (pattern.endsWith("'Z'")) {
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = sdf.parse(ts)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return null
    }

    private fun getMeasurementEpoch(m: GlucoseMeasurement): Long? {
        val ep = m.getEpochMillis()
        if (ep > 0L) return ep
        return parseTimestamp(m.timestamp) ?: parseTimestamp(m.factoryTimestamp)
    }

    /**
     * Insercion masiva en bloque con transaccion protegida y clausula INSERT OR IGNORE.
     * Garantiza deduplicacion inmutable sobre (patient_id, epoch_ms).
     */
    fun insertReadings(readings: List<GlucoseMeasurement>, patientId: String = ""): Int {
        if (readings.isEmpty()) return 0
        val targetPatientId = patientId.trim()
        var insertedCount = 0

        val db = getSafeWritableDatabase()
        if (db != null) {
            try {
                db.beginTransaction()
                val sql = """
                    INSERT OR IGNORE INTO $TABLE_NAME (
                        $COL_PATIENT_ID, $COL_EPOCH_MS, $COL_VALUE, $COL_TREND_ARROW,
                        $COL_TREND_MESSAGE, $COL_COLOR, $COL_UNITS, $COL_TIMESTAMP,
                        $COL_FACTORY_TIMESTAMP, $COL_IS_HIGH, $COL_IS_LOW
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
                val stmt = db.compileStatement(sql)
                for (m in readings) {
                    val epoch = getMeasurementEpoch(m) ?: continue
                    val value = m.numericValue
                    if (value <= 0.0) continue

                    stmt.clearBindings()
                    stmt.bindString(1, targetPatientId)
                    stmt.bindLong(2, epoch)
                    stmt.bindDouble(3, value)
                    stmt.bindLong(4, (m.trendArrow ?: 3).toLong())
                    if (m.trendMessage != null) stmt.bindString(5, m.trendMessage) else stmt.bindNull(5)
                    stmt.bindLong(6, (m.measurementColor ?: 1).toLong())
                    stmt.bindLong(7, (m.glucoseUnits ?: 1).toLong())
                    if (m.timestamp != null) stmt.bindString(8, m.timestamp) else stmt.bindNull(8)
                    if (m.factoryTimestamp != null) stmt.bindString(9, m.factoryTimestamp) else stmt.bindNull(9)
                    stmt.bindLong(10, if (m.isHigh == true || value > 180.0) 1L else 0L)
                    stmt.bindLong(11, if (m.isLow == true || value < 70.0) 1L else 0L)

                    val rowId = stmt.executeInsert()
                    if (rowId != -1L) {
                        insertedCount++
                    }
                }
                db.setTransactionSuccessful()
            } catch (_: Exception) {
            } finally {
                try { db.endTransaction() } catch (_: Exception) {}
            }
        }

        // Sincronizar almacenamiento en memoria solo como fallback de testing sin SQLite
        if (db == null) {
            val patientMap = inMemoryStorage.getOrPut(targetPatientId) { ConcurrentSkipListMap() }
            for (m in readings) {
                val epoch = getMeasurementEpoch(m) ?: continue
                val value = m.numericValue
                if (value <= 0.0) continue
                if (!patientMap.containsKey(epoch)) {
                    patientMap[epoch] = m
                    insertedCount++
                }
            }
        }

        _dbUpdateEvents.tryEmit(targetPatientId)
        return insertedCount
    }

    /**
     * Purga lecturas que superan la ventana movil de retencion (por defecto 90 dias).
     */
    fun purgeOldReadings(patientId: String? = null, retentionDays: Int = 90): Int {
        val cutoff = System.currentTimeMillis() - (retentionDays.toLong() * 24L * 3600L * 1000L)
        var deletedCount = 0

        val db = getSafeWritableDatabase()
        if (db != null) {
            try {
                deletedCount = if (!patientId.isNullOrBlank()) {
                    db.delete(TABLE_NAME, "$COL_PATIENT_ID = ? AND $COL_EPOCH_MS < ?", arrayOf(patientId.trim(), cutoff.toString()))
                } else {
                    db.delete(TABLE_NAME, "$COL_EPOCH_MS < ?", arrayOf(cutoff.toString()))
                }
                if (!patientId.isNullOrBlank()) {
                    db.delete(EVENT_TABLE_NAME, "$EVENT_COL_PATIENT_ID = ? AND $EVENT_COL_TIMESTAMP_MS < ?", arrayOf(patientId.trim(), cutoff.toString()))
                } else {
                    db.delete(EVENT_TABLE_NAME, "$EVENT_COL_TIMESTAMP_MS < ?", arrayOf(cutoff.toString()))
                }

            } catch (_: Exception) {}
        }

        if (!patientId.isNullOrBlank()) {
            val target = patientId.trim()
            val map = inMemoryStorage[target]
            if (map != null) {
                val toRemove = map.headMap(cutoff).keys.toList()
                toRemove.forEach { map.remove(it) }
                if (db == null) deletedCount += toRemove.size
            }
            inMemoryEventMarkers[target]?.entries?.removeIf { it.value.timestampMs < cutoff }
        } else {
            inMemoryStorage.forEach { (_, map) ->
                val toRemove = map.headMap(cutoff).keys.toList()
                toRemove.forEach { map.remove(it) }
                if (db == null) deletedCount += toRemove.size
            }
            inMemoryEventMarkers.values.forEach { markers ->
                markers.entries.removeIf { it.value.timestampMs < cutoff }
            }
        }

        _dbUpdateEvents.tryEmit(patientId.orEmpty())
        return deletedCount
    }

    /**
     * Consulta cerrada por intervalo de marcas de tiempo [startEpoch, endEpoch].
     */
    fun getReadingsBetween(patientId: String?, startEpoch: Long, endEpoch: Long): List<GlucoseMeasurement> {
        val targetPatientId = patientId?.trim().orEmpty()
        val db = getSafeReadableDatabase()

        if (db != null) {
            val list = mutableListOf<GlucoseMeasurement>()
            val query = if (targetPatientId.isNotBlank()) {
                """
                    SELECT $COL_PATIENT_ID, $COL_EPOCH_MS, $COL_VALUE, $COL_TREND_ARROW,
                           $COL_TREND_MESSAGE, $COL_COLOR, $COL_UNITS, $COL_TIMESTAMP,
                           $COL_FACTORY_TIMESTAMP, $COL_IS_HIGH, $COL_IS_LOW
                    FROM $TABLE_NAME
                    WHERE $COL_PATIENT_ID = ? AND $COL_EPOCH_MS >= ? AND $COL_EPOCH_MS <= ?
                    ORDER BY $COL_EPOCH_MS ASC
                """.trimIndent()
            } else {
                """
                    SELECT $COL_PATIENT_ID, $COL_EPOCH_MS, $COL_VALUE, $COL_TREND_ARROW,
                           $COL_TREND_MESSAGE, $COL_COLOR, $COL_UNITS, $COL_TIMESTAMP,
                           $COL_FACTORY_TIMESTAMP, $COL_IS_HIGH, $COL_IS_LOW
                    FROM $TABLE_NAME
                    WHERE $COL_EPOCH_MS >= ? AND $COL_EPOCH_MS <= ?
                    ORDER BY $COL_EPOCH_MS ASC
                """.trimIndent()
            }
            val args = if (targetPatientId.isNotBlank()) {
                arrayOf(targetPatientId, startEpoch.toString(), endEpoch.toString())
            } else {
                arrayOf(startEpoch.toString(), endEpoch.toString())
            }

            var cursor: Cursor? = null
            try {
                cursor = db.rawQuery(query, args)
                if (cursor.moveToFirst()) {
                    val idxVal = cursor.getColumnIndexOrThrow(COL_VALUE)
                    val idxArrow = cursor.getColumnIndexOrThrow(COL_TREND_ARROW)
                    val idxMsg = cursor.getColumnIndexOrThrow(COL_TREND_MESSAGE)
                    val idxColor = cursor.getColumnIndexOrThrow(COL_COLOR)
                    val idxUnits = cursor.getColumnIndexOrThrow(COL_UNITS)
                    val idxTs = cursor.getColumnIndexOrThrow(COL_TIMESTAMP)
                    val idxFts = cursor.getColumnIndexOrThrow(COL_FACTORY_TIMESTAMP)
                    val idxHigh = cursor.getColumnIndexOrThrow(COL_IS_HIGH)
                    val idxLow = cursor.getColumnIndexOrThrow(COL_IS_LOW)

                    do {
                        list.add(
                            GlucoseMeasurement(
                                factoryTimestamp = cursor.getString(idxFts),
                                timestamp = cursor.getString(idxTs),
                                valueInMgPerDl = cursor.getDouble(idxVal),
                                value = cursor.getDouble(idxVal),
                                trendArrow = cursor.getInt(idxArrow),
                                trendMessage = cursor.getString(idxMsg),
                                measurementColor = cursor.getInt(idxColor),
                                glucoseUnits = cursor.getInt(idxUnits),
                                isHigh = cursor.getInt(idxHigh) == 1,
                                isLow = cursor.getInt(idxLow) == 1
                            )
                        )
                    } while (cursor.moveToNext())
                }
            } catch (_: Exception) {
            } finally {
                cursor?.close()
            }
            return list
        }

        // Fallback a almacenamiento en memoria cuando SQLite no esta disponible (JVM unit tests)
        if (targetPatientId.isBlank()) {
            if (inMemoryStorage.isEmpty()) return emptyList()
            val allReadings = inMemoryStorage.values.flatMap { map ->
                map.subMap(startEpoch, true, endEpoch, true).values
            }.sortedBy { it.getEpochMillis() }
            return allReadings
        }
        val targetMap = inMemoryStorage[targetPatientId]
        if (targetMap != null) {
            return targetMap.subMap(startEpoch, true, endEpoch, true).values.toList()
        }
        return emptyList()
    }

    /**
     * Resumen clinico de periodo (1d, 7d, 14d, 30d, 90d) mediante agregacion SQL directa de alta velocidad (< 5 ms).
     * Incorpora el calculo unificado de GRI (Klonoff et al. 2022) sin redondeos intermedios.
     */
    fun getPeriodSummary(
        patientId: String?,
        periodDays: Int,
        anchorTimeMs: Long = System.currentTimeMillis()
    ): PeriodSummary {
        val targetPatientId = patientId?.trim().orEmpty()
        val windowMs = periodDays.toLong() * 24L * 3600L * 1000L

        // Determinar anclaje temporal real (reloj del sistema o fallback a maximo si datos historicos viejos)
        val maxAvailableEpoch = getMaxEpoch(targetPatientId)
        val effectiveAnchor = if (maxAvailableEpoch > 0L && maxAvailableEpoch < anchorTimeMs - windowMs) {
            maxAvailableEpoch
        } else {
            anchorTimeMs
        }
        val cutoff = effectiveAnchor - windowMs

        val db = getSafeReadableDatabase()
        if (db != null) {
            val query = if (targetPatientId.isNotBlank()) {
                """
                    SELECT 
                        COUNT(*) AS total_count,
                        AVG($COL_VALUE) AS avg_value,
                        MIN($COL_VALUE) AS min_value,
                        MAX($COL_VALUE) AS max_value,
                        SUM(CASE WHEN $COL_VALUE < 54.0 THEN 1 ELSE 0 END) AS vlow_count,
                        SUM(CASE WHEN $COL_VALUE >= 54.0 AND $COL_VALUE < 70.0 THEN 1 ELSE 0 END) AS low_count,
                        SUM(CASE WHEN $COL_VALUE >= 70.0 AND $COL_VALUE <= 180.0 THEN 1 ELSE 0 END) AS in_range_count,
                        SUM(CASE WHEN $COL_VALUE > 180.0 AND $COL_VALUE <= 250.0 THEN 1 ELSE 0 END) AS high_count,
                        SUM(CASE WHEN $COL_VALUE > 250.0 THEN 1 ELSE 0 END) AS vhigh_count,
                        SUM(CASE WHEN $COL_VALUE >= 70.0 AND $COL_VALUE <= 140.0 THEN 1 ELSE 0 END) AS tight_count
                    FROM $TABLE_NAME
                    WHERE $COL_PATIENT_ID = ? AND $COL_EPOCH_MS >= ? AND $COL_EPOCH_MS <= ?
                """.trimIndent()
            } else {
                """
                    SELECT 
                        COUNT(*) AS total_count,
                        AVG($COL_VALUE) AS avg_value,
                        MIN($COL_VALUE) AS min_value,
                        MAX($COL_VALUE) AS max_value,
                        SUM(CASE WHEN $COL_VALUE < 54.0 THEN 1 ELSE 0 END) AS vlow_count,
                        SUM(CASE WHEN $COL_VALUE >= 54.0 AND $COL_VALUE < 70.0 THEN 1 ELSE 0 END) AS low_count,
                        SUM(CASE WHEN $COL_VALUE >= 70.0 AND $COL_VALUE <= 180.0 THEN 1 ELSE 0 END) AS in_range_count,
                        SUM(CASE WHEN $COL_VALUE > 180.0 AND $COL_VALUE <= 250.0 THEN 1 ELSE 0 END) AS high_count,
                        SUM(CASE WHEN $COL_VALUE > 250.0 THEN 1 ELSE 0 END) AS vhigh_count,
                        SUM(CASE WHEN $COL_VALUE >= 70.0 AND $COL_VALUE <= 140.0 THEN 1 ELSE 0 END) AS tight_count
                    FROM $TABLE_NAME
                    WHERE $COL_EPOCH_MS >= ? AND $COL_EPOCH_MS <= ?
                """.trimIndent()
            }
            val args = if (targetPatientId.isNotBlank()) {
                arrayOf(targetPatientId, cutoff.toString(), (effectiveAnchor + 300_000L).toString())
            } else {
                arrayOf(cutoff.toString(), (effectiveAnchor + 300_000L).toString())
            }

            var cursor: Cursor? = null
            try {
                cursor = db.rawQuery(query, args)
                if (cursor.moveToFirst()) {
                    val count = cursor.getInt(0)
                    if (count > 0) {
                        val avg = cursor.getDouble(1)
                        val min = cursor.getDouble(2)
                        val max = cursor.getDouble(3)
                        val vLow = cursor.getInt(4)
                        val low = cursor.getInt(5)
                        val inRange = cursor.getInt(6)
                        val high = cursor.getInt(7)
                        val vHigh = cursor.getInt(8)
                        val tight = cursor.getInt(9)

                        return buildPeriodSummary(
                            periodDays = periodDays,
                            count = count,
                            mean = avg,
                            min = min,
                            max = max,
                            vLow = vLow,
                            low = low,
                            inRange = inRange,
                            high = high,
                            vHigh = vHigh,
                            tight = tight
                        )
                    }
                }
            } catch (_: Exception) {
            } finally {
                cursor?.close()
            }
        }

        // Calculo sobre lecturas en memoria
        val readings = getReadingsBetween(targetPatientId, cutoff, effectiveAnchor + 300_000L)
        val count = readings.size
        if (count == 0) {
            return PeriodSummary(periodDays = periodDays)
        }

        val values = readings.map { it.numericValue }
        val avg = values.average()
        val min = values.minOrNull() ?: 0.0
        val max = values.maxOrNull() ?: 0.0
        val vLow = values.count { it < 54.0 }
        val low = values.count { it >= 54.0 && it < 70.0 }
        val inRange = values.count { it in 70.0..180.0 }
        val high = values.count { it > 180.0 && it <= 250.0 }
        val vHigh = values.count { it > 250.0 }
        val tight = values.count { it in 70.0..140.0 }

        return buildPeriodSummary(
            periodDays = periodDays,
            count = count,
            mean = avg,
            min = min,
            max = max,
            vLow = vLow,
            low = low,
            inRange = inRange,
            high = high,
            vHigh = vHigh,
            tight = tight
        )
    }

    private fun buildPeriodSummary(
        periodDays: Int,
        count: Int,
        mean: Double,
        min: Double,
        max: Double,
        vLow: Int,
        low: Int,
        inRange: Int,
        high: Int,
        vHigh: Int,
        tight: Int
    ): PeriodSummary {
        val total = count.toDouble()
        val vLowPctRaw = (vLow / total) * 100.0
        val lowPctRaw = (low / total) * 100.0
        val inRangePctRaw = (inRange / total) * 100.0
        val highPctRaw = (high / total) * 100.0
        val vHighPctRaw = (vHigh / total) * 100.0
        val tightPctRaw = (tight / total) * 100.0

        // GRI (Klonoff et al. 2022) sin redondeos intermedios
        val rawGri = (3.0 * vLowPctRaw) + (2.4 * lowPctRaw) + (1.6 * vHighPctRaw) + (0.8 * highPctRaw)
        val gri = roundDec(rawGri.coerceIn(0.0, 100.0), 1)

        val griCategory = when {
            gri <= 20.0 -> "Zona A (Muy Bajo Riesgo)"
            gri <= 40.0 -> "Zona B (Bajo Riesgo)"
            gri <= 60.0 -> "Zona C (Riesgo Moderado)"
            gri <= 80.0 -> "Zona D (Riesgo Alto)"
            else -> "Zona E (Riesgo Muy Alto)"
        }

        return PeriodSummary(
            periodDays = periodDays,
            totalCount = count,
            mean = roundDec(mean, 1),
            min = roundDec(min, 1),
            max = roundDec(max, 1),
            veryLowCount = vLow,
            lowCount = low,
            inRangeCount = inRange,
            highCount = high,
            veryHighCount = vHigh,
            tightRangeCount = tight,
            inRangePercent = roundDec(inRangePctRaw, 1),
            belowRangePercent = roundDec(vLowPctRaw + lowPctRaw, 1),
            aboveRangePercent = roundDec(highPctRaw + vHighPctRaw, 1),
            tightRangePercent = roundDec(tightPctRaw, 1),
            veryLowPercent = roundDec(vLowPctRaw, 1),
            lowPercent = roundDec(lowPctRaw, 1),
            highPercent = roundDec(highPctRaw, 1),
            veryHighPercent = roundDec(vHighPctRaw, 1),
            gri = gri,
            griCategory = griCategory
        )
    }

    /**
     * Desglose por los 4 bloques horarios clinicos de ReportTimeBlock:
     * NIGHT (00-06h), MORNING (06-12h), AFTERNOON (12-18h), EVENING (18-24h).
     */
    fun getTimeBlockSummary(
        patientId: String?,
        periodDays: Int,
        anchorTimeMs: Long = System.currentTimeMillis()
    ): List<BlockMetric> {
        val windowMs = periodDays.toLong() * 24L * 3600L * 1000L
        val maxAvailable = getMaxEpoch(patientId.orEmpty())
        val effectiveAnchor = if (maxAvailable > 0L && maxAvailable < anchorTimeMs - windowMs) maxAvailable else anchorTimeMs
        val cutoff = effectiveAnchor - windowMs

        val readings = getReadingsBetween(patientId, cutoff, effectiveAnchor + 300_000L)
        val avgReport = ClinicalReportsCalculator.calculateAverageGlucose(readings, periodDays)
        val lowReport = ClinicalReportsCalculator.calculateLowGlucoseEvents(readings, periodDays)

        return ReportTimeBlock.values().map { block ->
            BlockMetric(
                block = block,
                averageGlucose = avgReport.averageByBlock[block] ?: 0.0,
                lowEventsCount = lowReport.eventsByBlock[block] ?: 0
            )
        }
    }

    /**
     * Desglose horario (0..23h) para AGP modal (percentiles p10, p25, p50, p75, p90, CV %, SD, MAGE).
     */
    fun getDailyPatterns(
        patientId: String?,
        periodDays: Int,
        targetLow: Double = 70.0,
        targetHigh: Double = 180.0,
        anchorTimeMs: Long = System.currentTimeMillis()
    ): DailyPatternsReport {
        val windowMs = periodDays.toLong() * 24L * 3600L * 1000L
        val maxAvailable = getMaxEpoch(patientId.orEmpty())
        val effectiveAnchor = if (maxAvailable > 0L && maxAvailable < anchorTimeMs - windowMs) maxAvailable else anchorTimeMs
        val cutoff = effectiveAnchor - windowMs

        val readings = getReadingsBetween(patientId, cutoff, effectiveAnchor + 300_000L)
        return ClinicalReportsCalculator.calculateDailyPatterns(readings, periodDays, targetLow, targetHigh)
    }

    /**
     * Importador de CSV de LibreView y OpenGluco para precarga masiva por lotes de 90 dias.
     */
    fun importLibreViewCsv(patientId: String, csvContent: String): Int {
        val cleanContent = csvContent.removePrefix("\uFEFF")
        if (cleanContent.isBlank()) return 0
        val lines = cleanContent.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return 0

        // Deteccion automatica de la fila de cabecera
        var headerIndex = -1
        for (i in lines.indices) {
            val lower = lines[i].lowercase(Locale.US)
            if (lower.contains("timestamp") || lower.contains("hora") || lower.contains("fecha") ||
                lower.contains("tiempo") || lower.contains("time") ||
                lower.contains("glucosa") || lower.contains("glucose")) {
                headerIndex = i
                break
            }
        }
        if (headerIndex == -1) return 0

        val delimiter = if (lines[headerIndex].contains(";")) ';' else ','
        val headerCols = parseCsvLine(lines[headerIndex], delimiter).map { it.lowercase(Locale.US).trim() }
        var tsCol = -1
        var histValCol = -1
        var scanValCol = -1
        var genericValCol = -1

        for (c in headerCols.indices) {
            val name = headerCols[c]
            val normalized = name.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
            when {
                normalized.contains("timestamp") || normalized.contains("hora") ||
                normalized.contains("fecha") || normalized.contains("date") ||
                normalized.contains("tiempo") || normalized.contains("time") -> {
                    if (tsCol == -1) tsCol = c
                }
                normalized.contains("histori") -> {
                    histValCol = c
                }
                normalized.contains("escanead") || normalized.contains("scan") -> {
                    scanValCol = c
                }
                normalized.contains("glucosa") || normalized.contains("glucose") ||
                normalized.contains("value") || normalized.contains("mg/dl") || normalized.contains("mmol") -> {
                    if (genericValCol == -1) genericValCol = c
                }
            }
        }

        if (tsCol == -1) return 0

        val parsedList = mutableListOf<GlucoseMeasurement>()
        for (i in (headerIndex + 1) until lines.size) {
            val row = parseCsvLine(lines[i], delimiter)
            if (row.size <= tsCol) continue

            val rawTs = row[tsCol].trim().removeSurrounding("\"")
            val epoch = parseTimestamp(rawTs) ?: continue

            // Extraer valor de glucosa
            val rawValStr = when {
                histValCol >= 0 && histValCol < row.size && row[histValCol].isNotBlank() -> row[histValCol]
                scanValCol >= 0 && scanValCol < row.size && row[scanValCol].isNotBlank() -> row[scanValCol]
                genericValCol >= 0 && genericValCol < row.size && row[genericValCol].isNotBlank() -> row[genericValCol]
                else -> ""
            }.trim().removeSurrounding("\"").replace(",", ".")

            var numVal = rawValStr.toDoubleOrNull() ?: continue
            // Si la unidad es mmol/L (< 35.0), convertir a mg/dL
            if (numVal in 1.0..34.99) {
                numVal *= 18.0182
            }
            if (numVal < 20.0 || numVal > 600.0) continue

            parsedList.add(
                GlucoseMeasurement(
                    timestamp = rawTs,
                    factoryTimestamp = rawTs,
                    valueInMgPerDl = roundDec(numVal, 1),
                    value = roundDec(numVal, 1),
                    trendArrow = 3,
                    trendMessage = "Estable",
                    measurementColor = 1,
                    glucoseUnits = 1,
                    isHigh = numVal > 180.0,
                    isLow = numVal < 70.0
                )
            )
        }

        return insertReadings(parsedList, patientId)
    }

    private fun parseCsvLine(line: String, delimiter: Char = ','): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            if (ch == '\"') {
                inQuotes = !inQuotes
            } else if (ch == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    /**
     * Flujo reactivo de lecturas persistidas para los ultimos `days` dias.
     */
    fun getReadingsFlow(patientId: String?, days: Int = 90): Flow<List<GlucoseMeasurement>> = flow {
        val targetPatientId = patientId?.trim().orEmpty()
        emit(getHistoricalReadingsList(days, targetPatientId))

        _dbUpdateEvents.collect { updatedId ->
            if (targetPatientId.isBlank() || updatedId.isBlank() || updatedId == targetPatientId) {
                emit(getHistoricalReadingsList(days, targetPatientId))
            }
        }
    }

    /**
     * Obtiene el listado completo de lecturas para los ultimos `days` dias de forma sincrona.
     */
    fun getHistoricalReadingsList(days: Int, patientId: String? = null): List<GlucoseMeasurement> {
        val targetPatientId = patientId?.trim().orEmpty()
        val windowMs = days.toLong() * 24L * 3600L * 1000L
        val maxAvailable = getMaxEpoch(targetPatientId)
        val now = System.currentTimeMillis()
        val effectiveAnchor = if (maxAvailable > 0L && maxAvailable < now - windowMs) maxAvailable else now
        val cutoff = effectiveAnchor - windowMs
        return getReadingsBetween(targetPatientId, cutoff, effectiveAnchor + 300_000L)
    }

    /**
     * Devuelve la cantidad de dias distintos disponibles en el historial local para el paciente.
     */
    fun getAvailableDays(patientId: String?): Int {
        val targetPatientId = patientId?.trim().orEmpty()
        val db = getSafeReadableDatabase()
        if (db != null) {
            val sql = if (targetPatientId.isNotBlank()) {
                "SELECT MIN($COL_EPOCH_MS), MAX($COL_EPOCH_MS) FROM $TABLE_NAME WHERE $COL_PATIENT_ID = ? AND $COL_VALUE > 0"
            } else {
                "SELECT MIN($COL_EPOCH_MS), MAX($COL_EPOCH_MS) FROM $TABLE_NAME WHERE $COL_VALUE > 0"
            }
            val args = if (targetPatientId.isNotBlank()) arrayOf(targetPatientId) else null
            var cursor: android.database.Cursor? = null
            try {
                cursor = db.rawQuery(sql, args)
                if (cursor.moveToFirst() && !cursor.isNull(0) && !cursor.isNull(1)) {
                    val minEpoch = cursor.getLong(0)
                    val maxEpoch = cursor.getLong(1)
                    if (maxEpoch > minEpoch) {
                        val diffMs = maxEpoch - minEpoch
                        return maxOf(1, kotlin.math.ceil(diffMs.toDouble() / (24L * 3600L * 1000L)).toInt())
                    } else if (minEpoch > 0L) {
                        return 1
                    }
                }
            } catch (_: Exception) {
            } finally {
                cursor?.close()
            }
        }
        val all = getReadingsBetween(patientId, 0L, Long.MAX_VALUE)
        return ClinicalReportsCalculator.calculateAvailableDays(all)
    }

    fun insertEventMarker(
        patientId: String,
        timestampMs: Long,
        type: GlucoseEventType,
        note: String? = null
    ): GlucoseEventMarker? {
        val targetPatientId = patientId.trim()
        if (targetPatientId.isBlank() || timestampMs <= 0L) return null

        val cleanNote = note?.trim()?.takeIf { it.isNotBlank() }
        val encryptedNote = cleanNote?.let { KeystoreCryptoHelper.encrypt(it) }
        if (cleanNote != null && encryptedNote.isNullOrBlank()) return null

        val marker = GlucoseEventMarker(
            id = UUID.randomUUID().toString(),
            patientId = targetPatientId,
            timestampMs = timestampMs,
            type = type,
            note = cleanNote
        )
        val db = getSafeWritableDatabase()
        if (db != null) {
            val values = ContentValues().apply {
                put(EVENT_COL_ID, marker.id)
                put(EVENT_COL_PATIENT_ID, targetPatientId)
                put(EVENT_COL_TIMESTAMP_MS, timestampMs)
                put(EVENT_COL_TYPE, type.name)
                if (encryptedNote != null) put(EVENT_COL_ENCRYPTED_NOTE, encryptedNote)
            }
            try {
                if (db.insertOrThrow(EVENT_TABLE_NAME, null, values) < 0L) return null
            } catch (_: Exception) {
                return null
            }
        } else {
            inMemoryEventMarkers.getOrPut(targetPatientId) { ConcurrentHashMap() }[marker.id] = marker
        }

        _dbUpdateEvents.tryEmit(targetPatientId)
        return marker
    }

    fun getEventMarkers(patientId: String): List<GlucoseEventMarker> {
        val targetPatientId = patientId.trim()
        if (targetPatientId.isBlank()) return emptyList()

        val db = getSafeReadableDatabase()
        if (db != null) {
            var cursor: Cursor? = null
            try {
                cursor = db.query(
                    EVENT_TABLE_NAME,
                    arrayOf(EVENT_COL_ID, EVENT_COL_PATIENT_ID, EVENT_COL_TIMESTAMP_MS, EVENT_COL_TYPE, EVENT_COL_ENCRYPTED_NOTE),
                    "$EVENT_COL_PATIENT_ID = ?",
                    arrayOf(targetPatientId),
                    null,
                    null,
                    "$EVENT_COL_TIMESTAMP_MS DESC"
                )
                val markers = mutableListOf<GlucoseEventMarker>()
                while (cursor.moveToNext()) {
                    val type = runCatching { GlucoseEventType.valueOf(cursor.getString(3)) }.getOrNull() ?: continue
                    val encryptedNote = if (cursor.isNull(4)) null else cursor.getString(4)
                    val decryptedNote = encryptedNote?.let(KeystoreCryptoHelper::decrypt)?.takeIf { it.isNotBlank() }
                    markers += GlucoseEventMarker(
                        id = cursor.getString(0),
                        patientId = cursor.getString(1),
                        timestampMs = cursor.getLong(2),
                        type = type,
                        note = decryptedNote
                    )
                }
                return markers
            } catch (_: Exception) {
            } finally {
                cursor?.close()
            }
        }

        return inMemoryEventMarkers[targetPatientId]?.values
            ?.sortedByDescending { it.timestampMs }
            .orEmpty()
    }

    fun getEventMarkersFlow(patientId: String?): Flow<List<GlucoseEventMarker>> = flow {
        val targetPatientId = patientId?.trim().orEmpty()
        emit(getEventMarkers(targetPatientId))
        _dbUpdateEvents.collect { updatedId ->
            if (updatedId == targetPatientId) emit(getEventMarkers(targetPatientId))
        }
    }

    fun deleteEventMarker(patientId: String, markerId: String): Boolean {
        val targetPatientId = patientId.trim()
        if (targetPatientId.isBlank() || markerId.isBlank()) return false
        val db = getSafeWritableDatabase()
        val deleted = if (db != null) {
            try {
                db.delete(
                    EVENT_TABLE_NAME,
                    "$EVENT_COL_PATIENT_ID = ? AND $EVENT_COL_ID = ?",
                    arrayOf(targetPatientId, markerId)
                ) > 0
            } catch (_: Exception) {
                false
            }
        } else {
            inMemoryEventMarkers[targetPatientId]?.remove(markerId) != null
        }
        if (deleted) _dbUpdateEvents.tryEmit(targetPatientId)
        return deleted
    }

    /**
     * Limpia datos almacenados para un paciente o todos.
     */
    fun clearData(patientId: String? = null): Int {
        val targetPatientId = patientId?.trim().orEmpty()
        var count = 0
        val db = getSafeWritableDatabase()
        if (db != null) {
            try {
                count = if (targetPatientId.isNotBlank()) {
                    db.delete(TABLE_NAME, "$COL_PATIENT_ID = ?", arrayOf(targetPatientId))
                } else {
                    db.delete(TABLE_NAME, null, null)
                }
                if (targetPatientId.isNotBlank()) {
                    db.delete(EVENT_TABLE_NAME, "$EVENT_COL_PATIENT_ID = ?", arrayOf(targetPatientId))
                } else {
                    db.delete(EVENT_TABLE_NAME, null, null)
                }

            } catch (_: Exception) {}
        }

        if (targetPatientId.isNotBlank()) {
            inMemoryStorage.remove(targetPatientId)
            inMemoryEventMarkers.remove(targetPatientId)
        } else {
            inMemoryStorage.clear()
            inMemoryEventMarkers.clear()
        }

        _dbUpdateEvents.tryEmit(targetPatientId)
        return count
    }

    private fun getMaxEpoch(patientId: String): Long {
        val db = getSafeReadableDatabase()
        if (db != null) {
            val query = if (patientId.isNotBlank()) {
                "SELECT MAX($COL_EPOCH_MS) FROM $TABLE_NAME WHERE $COL_PATIENT_ID = ?"
            } else {
                "SELECT MAX($COL_EPOCH_MS) FROM $TABLE_NAME"
            }
            val args = if (patientId.isNotBlank()) arrayOf(patientId) else null
            var cursor: Cursor? = null
            try {
                cursor = db.rawQuery(query, args)
                if (cursor.moveToFirst()) {
                    val maxVal = cursor.getLong(0)
                    if (maxVal > 0L) return maxVal
                }
            } catch (_: Exception) {
            } finally {
                cursor?.close()
            }
        }

        val map = inMemoryStorage[patientId]
            ?: if (patientId.isBlank() && inMemoryStorage.isNotEmpty()) {
                val maxKey = inMemoryStorage.values.mapNotNull { it.keys.lastOrNull() }.maxOrNull() ?: 0L
                return maxKey
            } else null
        return map?.lastKey() ?: 0L
    }

    private fun roundDec(value: Double, decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10.0 }
        return (value * multiplier).roundToInt() / multiplier
    }
}
