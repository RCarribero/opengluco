package com.example.opengluco.core.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateRepositoryTest {

    private val repository = AppUpdateRepository()

    @Test
    fun testSemanticVersionComparison() {
        // Version remota mas reciente que la actual
        assertTrue(repository.isVersionNewer("1.1.0", "1.0.0"))
        assertTrue(repository.isVersionNewer("v1.1.0", "v1.0.0"))
        assertTrue(repository.isVersionNewer("2.0.0", "1.9.9"))
        assertTrue(repository.isVersionNewer("1.0.1", "1.0.0"))
        assertTrue(repository.isVersionNewer("v2.1.3-beta", "v2.1.2"))

        // Version remota igual o inferior
        assertFalse(repository.isVersionNewer("1.0.0", "1.0.0"))
        assertFalse(repository.isVersionNewer("v1.0.0", "1.0.0"))
        assertFalse(repository.isVersionNewer("1.0.0", "1.1.0"))
        assertFalse(repository.isVersionNewer("0.9.9", "1.0.0"))
        assertFalse(repository.isVersionNewer("", "1.0.0"))
        assertFalse(repository.isVersionNewer("1.0.0", ""))
    }
}
