package com.example.opengluco.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FastDateParserTest {

    @Test
    fun testParseEpoch_emptyOrBlank_returnsZero() {
        assertEquals(0L, FastDateParser.parseEpoch(null))
        assertEquals(0L, FastDateParser.parseEpoch(""))
        assertEquals(0L, FastDateParser.parseEpoch("   "))
    }

    @Test
    fun testParseEpoch_numericTimestamp() {
        val millis = 1770000000000L
        assertEquals(millis, FastDateParser.parseEpoch("1770000000000"))

        val seconds = 1770000000L
        assertEquals(seconds * 1000L, FastDateParser.parseEpoch("1770000000"))
    }

    @Test
    fun testParseEpoch_isoUtc() {
        val iso = "2026-10-05T18:31:31Z"
        val epoch = FastDateParser.parseEpoch(iso)
        assertTrue("Epoch must be positive", epoch > 0L)

        // Verificacion de hit en cache
        val cachedEpoch = FastDateParser.parseEpoch(iso)
        assertEquals(epoch, cachedEpoch)
    }

    @Test
    fun testParseEpoch_slashAmPm() {
        val str = "9/3/2026 10:00:00 AM"
        val epoch = FastDateParser.parseEpoch(str)
        assertTrue("Epoch for 9/3/2026 10:00:00 AM must be positive", epoch > 0L)
    }

    @Test
    fun testParseEpoch_dashStandard() {
        val str = "2026-10-16 14:30:00"
        val epoch = FastDateParser.parseEpoch(str)
        assertTrue("Epoch for dash standard format must be positive", epoch > 0L)
    }

    @Test
    fun testFormatDisplayTime() {
        val epoch = 1770000000000L
        val formatted = FastDateParser.formatDisplayTime(epoch)
        assertTrue("Formatted time must be non-empty", formatted.isNotBlank())
        assertTrue("Formatted time must match HH:mm", formatted.matches(Regex("\\d{2}:\\d{2}")))

        // Fallback para epoch invalido
        assertEquals("Ahora", FastDateParser.formatDisplayTime(0L))
        assertEquals("Ahora", FastDateParser.formatDisplayTime(-1L))
    }

    @Test
    fun testLruCache_handlesMoreThan512EntriesWithoutFailure() {
        // Generar 600 timestamps unicos para superar la capacidad del cache de 512
        for (i in 1..600) {
            val ts = "2026-01-01T00:00:00.${String.format(java.util.Locale.US, "%03d", i % 1000)}Z"
            val parsed = FastDateParser.parseEpoch(ts)
            assertTrue("Timestamp $i must parse successfully", parsed > 0L)
        }

        // El parser debe seguir funcionando perfectamente y respondiendo en cache para lecturas recientes
        val recentTs = "2026-01-01T00:00:00.600Z"
        val parsedRecent = FastDateParser.parseEpoch(recentTs)
        assertTrue("Recent timestamp must parse", parsedRecent > 0L)
    }

    @Test
    fun testParseEpoch_isoWithTimezoneOffset() {
        val isoOffset = "2026-10-16T15:30:00+02:00"
        val epoch = FastDateParser.parseEpoch(isoOffset)
        assertTrue("Epoch for ISO with offset must be positive", epoch > 0L)

        val cachedEpoch = FastDateParser.parseEpoch(isoOffset)
        assertEquals(epoch, cachedEpoch)
    }

    @Test
    fun testParseEpoch_invalidStringReturnsZeroAndCaches() {
        val invalid = "malformed-timestamp-string-xyz"
        val first = FastDateParser.parseEpoch(invalid)
        assertEquals(0L, first)

        val second = FastDateParser.parseEpoch(invalid)
        assertEquals(0L, second)
    }
}
