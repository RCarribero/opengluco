package com.example.opengluco.core.network

import com.example.opengluco.core.model.LoginRequest
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class OpenGlucoApiServiceContractTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: OpenGlucoApiService

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val contentType = "application/json".toMediaType()
        apiService = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(OpenGlucoApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testLoginContract_success() = runBlocking {
        val jsonResponse = """
            {
                "status": 0,
                "data": {
                    "user": {
                        "id": "usr-12345",
                        "firstName": "John",
                        "lastName": "Doe",
                        "email": "john@example.com",
                        "country": "ES",
                        "uom": 1
                    },
                    "authTicket": {
                        "token": "jwt-test-auth-token",
                        "expires": 1750000000,
                        "duration": 3600000
                    },
                    "redirect": false,
                    "region": "eu"
                }
            }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val response = apiService.login(LoginRequest("john@example.com", "secretPassword"))
        assertTrue(response.isSuccessful)

        val body = response.body()
        assertNotNull(body)
        assertEquals(0, body!!.status)
        assertEquals("usr-12345", body.data?.user?.id)
        assertEquals("jwt-test-auth-token", body.data?.authTicket?.token)

        val recorded = server.takeRequest()
        assertEquals("/llu/auth/login", recorded.path)
        assertEquals("POST", recorded.method)
    }

    @Test
    fun testGetConnectionsContract_success() = runBlocking {
        val jsonResponse = """
            {
                "status": 0,
                "data": [
                    {
                        "id": "conn-1",
                        "patientId": "patient-abc",
                        "firstName": "Alice",
                        "lastName": "Smith",
                        "targetLow": 70,
                        "targetHigh": 180,
                        "sensor": {
                            "deviceId": "dev-1",
                            "sn": "SN123",
                            "a": 1720000000,
                            "w": 60,
                            "pt": 3
                        },
                        "glucoseMeasurement": {
                            "ValueInMgPerDl": 125.0,
                            "TrendArrow": 3,
                            "TrendMessage": "Estable",
                            "Timestamp": "8/27/2026 10:00:00 AM"
                        }
                    }
                ]
            }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val response = apiService.getConnections()
        assertTrue(response.isSuccessful)

        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.data?.size)
        val patient = body.data!![0]
        assertEquals("Alice Smith", patient.fullName)
        assertEquals(125.0, patient.effectiveMeasurement?.numericValue ?: 0.0, 0.001)

        val recorded = server.takeRequest()
        assertEquals("/llu/connections", recorded.path)
        assertEquals("GET", recorded.method)
    }

    @Test
    fun testGetPatientGraphContract_success() = runBlocking {
        val jsonResponse = """
            {
                "status": 0,
                "data": {
                    "graphData": [
                        {
                            "ValueInMgPerDl": 110.0,
                            "TrendArrow": 3,
                            "Timestamp": "8/27/2026 9:00:00 AM"
                        },
                        {
                            "ValueInMgPerDl": 130.0,
                            "TrendArrow": 4,
                            "Timestamp": "8/27/2026 9:15:00 AM"
                        }
                    ]
                }
            }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val response = apiService.getPatientGraph("patient-abc")
        assertTrue(response.isSuccessful)

        val body = response.body()
        assertNotNull(body)
        assertEquals(2, body!!.data?.graphData?.size)

        val recorded = server.takeRequest()
        assertEquals("/llu/connections/patient-abc/graph", recorded.path)
        assertEquals("GET", recorded.method)
    }

    @Test
    fun testTermsAcceptContract() = runBlocking {
        val jsonResponse = """
            {
                "status": 0,
                "data": {
                    "accepted": "true"
                }
            }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val response = apiService.acceptTerms()
        assertTrue(response.isSuccessful)

        val recorded = server.takeRequest()
        assertEquals("/llu/auth/terms/accept", recorded.path)
        assertEquals("POST", recorded.method)
    }
}
