package com.example.opengluco.core.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest

class OpenGlucoInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun expectedSha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun testHeaders_standardHeadersAlwaysPresent() {
        var token: String? = null
        var accountId: String? = null

        val client = OkHttpClient.Builder()
            .addInterceptor(OpenGlucoInterceptor(
                tokenProvider = { token },
                accountIdProvider = { accountId },
                appVersion = "4.16.0"
            ))
            .build()

        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder()
            .url(server.url("/test"))
            .build()

        val response = client.newCall(request).execute()
        assertTrue(response.isSuccessful)

        val recorded = server.takeRequest()
        assertEquals("application/json", recorded.getHeader("Content-Type"))
        assertEquals("application/json", recorded.getHeader("Accept"))
        assertEquals("llu.android", recorded.getHeader("product"))
        assertEquals("4.16.0", recorded.getHeader("version"))
        assertNotNull(recorded.getHeader("User-Agent"))
        assertNull("Authorization should be absent when token is null", recorded.getHeader("Authorization"))
        assertNull("account-id should be absent when accountId is null", recorded.getHeader("account-id"))
    }

    @Test
    fun testHeaders_bearerTokenInjectedWhenPresent() {
        val client = OkHttpClient.Builder()
            .addInterceptor(OpenGlucoInterceptor(
                tokenProvider = { "my-secret-jwt-token" },
                accountIdProvider = { null }
            ))
            .build()

        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        client.newCall(Request.Builder().url(server.url("/test")).build()).execute()

        val recorded = server.takeRequest()
        assertEquals("Bearer my-secret-jwt-token", recorded.getHeader("Authorization"))
    }

    @Test
    fun testHeaders_accountIdHashedWithSha256() {
        val userId = "user-uuid-12345"
        val expectedHash = expectedSha256(userId)

        val client = OkHttpClient.Builder()
            .addInterceptor(OpenGlucoInterceptor(
                tokenProvider = { null },
                accountIdProvider = { userId }
            ))
            .build()

        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        client.newCall(Request.Builder().url(server.url("/test")).build()).execute()

        val recorded = server.takeRequest()
        assertEquals(expectedHash, recorded.getHeader("account-id"))
    }

    @Test
    fun testHeaders_emptyOrBlankValuesIgnored() {
        val client = OkHttpClient.Builder()
            .addInterceptor(OpenGlucoInterceptor(
                tokenProvider = { "   " },
                accountIdProvider = { "" }
            ))
            .build()

        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        client.newCall(Request.Builder().url(server.url("/test")).build()).execute()

        val recorded = server.takeRequest()
        assertNull(recorded.getHeader("Authorization"))
        assertNull(recorded.getHeader("account-id"))
    }

    @Test
    fun testSha256Determinism() {
        val hash1 = expectedSha256("test-account-id")
        val hash2 = expectedSha256("test-account-id")
        assertEquals(hash1, hash2)
        assertEquals(64, hash1.length) // 256 bits = 64 hex characters
    }
}
