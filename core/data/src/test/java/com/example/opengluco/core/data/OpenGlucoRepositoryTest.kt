package com.example.opengluco.core.data

import org.junit.Assert.*
import org.junit.Test

class OpenGlucoRepositoryTest {

    @Test
    fun testRegionsBaseUrls() {
        assertEquals("https://api-eu.libreview.io/", OpenGlucoRegion.EU.baseUrl)
        assertEquals("https://api-us.libreview.io/", OpenGlucoRegion.US.baseUrl)
        assertEquals("https://api-ap.libreview.io/", OpenGlucoRegion.AP.baseUrl)
        assertEquals("https://api-de.libreview.io/", OpenGlucoRegion.DE.baseUrl)
        assertEquals("https://api-fr.libreview.io/", OpenGlucoRegion.FR.baseUrl)
        assertEquals("https://api-jp.libreview.io/", OpenGlucoRegion.JP.baseUrl)
    }

    @Test
    fun testRepositorySessionState() {
        val repo = OpenGlucoRepository(OpenGlucoRegion.EU)
        assertNull(repo.getSessionToken())
        assertNull(repo.getUserId())

        repo.setSession("token-abc", "user-123")
        assertEquals("token-abc", repo.getSessionToken())
        assertEquals("user-123", repo.getUserId())

        repo.setSession(null, null)
        assertNull(repo.getSessionToken())
        assertNull(repo.getUserId())
    }

    @Test
    fun testRepositoryRegionSwitching() {
        val repo = OpenGlucoRepository(OpenGlucoRegion.EU)
        repo.setRegion(OpenGlucoRegion.US)
        // Verify repository initializes without throwing
        assertNotNull(repo)
    }
}
