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

    @Test
    fun testAllowedGitHubUrls() {
        assertTrue(repository.isAllowedGitHubUrl("https://github.com/RCarribero/opengluco/releases/download/v1.3.0/OpenGluco-Mobile-v1.3.0.apk"))
        assertTrue(repository.isAllowedGitHubUrl("https://objects.githubusercontent.com/github-production-release-asset/12345"))
        assertFalse(repository.isAllowedGitHubUrl("http://github.com/RCarribero/opengluco/releases/download/v1.3.0/app.apk"))
        assertFalse(repository.isAllowedGitHubUrl("https://evil.example.com/OpenGluco-Mobile-v1.3.0.apk"))
        assertFalse(repository.isAllowedGitHubUrl(""))
    }
}
