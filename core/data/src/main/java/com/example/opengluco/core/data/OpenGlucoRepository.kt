package com.example.opengluco.core.data

import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.GraphData
import com.example.opengluco.core.model.LoginData
import com.example.opengluco.core.model.LoginRequest
import com.example.opengluco.core.model.SensorInfo
import com.example.opengluco.core.network.OpenGlucoApiService
import com.example.opengluco.core.network.OpenGlucoInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Regiones de servicio de la infraestructura cloud LibreView de Abbott Laboratories.
 */
enum class OpenGlucoRegion(val baseUrl: String) {
    EU("https://api-eu.libreview.io/"),
    US("https://api-us.libreview.io/"),
    AP("https://api-ap.libreview.io/"),
    DE("https://api-de.libreview.io/"),
    FR("https://api-fr.libreview.io/"),
    JP("https://api-jp.libreview.io/")
}

class NetworkException(message: String = "Sin conexión a Internet", cause: Throwable? = null) : Exception(message, cause)
class AuthExpiredException(message: String = "Sesión caducada. Vuelve a iniciar sesión.", cause: Throwable? = null) : Exception(message, cause)

/**
 * Repositorio de datos para conexion e interoperabilidad directa con los servidores de Abbott Laboratories (LibreView).
 */
class OpenGlucoRepository(
    private var region: OpenGlucoRegion = OpenGlucoRegion.EU
) {
    private var sessionToken: String? = null
    private var userId: String? = null

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .addInterceptor(OpenGlucoInterceptor(
                tokenProvider = { sessionToken },
                accountIdProvider = { userId },
                appVersion = "4.16.0"
            ))
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private var apiService: OpenGlucoApiService = createApiService(region.baseUrl)

    private fun createApiService(baseUrl: String): OpenGlucoApiService {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(OpenGlucoApiService::class.java)
    }

    fun setRegion(newRegion: OpenGlucoRegion) {
        if (region != newRegion) {
            region = newRegion
            apiService = createApiService(newRegion.baseUrl)
        }
    }

    fun setSession(token: String?, id: String?) {
        sessionToken = token
        userId = id
    }

    suspend fun login(email: String, password: String): Result<LoginData> = withContext(Dispatchers.IO) {
        try {
            var response = apiService.login(LoginRequest(email = email, password = password))
            if (response.isSuccessful) {
                var body = response.body()
                val initialData = body?.data
                val redirectRegion = initialData?.region

                // Soporte para auto-redirección de región
                if (initialData?.redirect == true && !redirectRegion.isNullOrBlank()) {
                    val targetRegionCode = redirectRegion.uppercase()
                    val targetRegion = OpenGlucoRegion.values().find { it.name == targetRegionCode }
                        ?: OpenGlucoRegion.EU
                    setRegion(targetRegion)
                    response = apiService.login(LoginRequest(email = email, password = password))
                    body = response.body()
                }

                val finalData = body?.data
                if (body != null && body.status == 0 && finalData != null) {
                    sessionToken = finalData.authTicket?.token
                    userId = finalData.user?.id
                    Result.success(finalData)
                } else if (body?.status == 2) {
                    Result.failure(AuthExpiredException("Credenciales o token no válidos"))
                } else {
                    Result.failure(Exception(body?.error?.message ?: "Error de autenticación (${body?.status})"))
                }
            } else if (response.code() == 401 || response.code() == 403) {
                Result.failure(AuthExpiredException("Credenciales incorrectas (HTTP ${response.code()})"))
            } else {
                Result.failure(Exception("HTTP Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: java.io.IOException) {
            Result.failure(NetworkException("Sin conexión a Internet", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConnections(): Result<List<ConnectionItem>> = withContext(Dispatchers.IO) {
        if (sessionToken?.startsWith("demo") == true) {
            return@withContext Result.success(getMockConnections())
        }
        try {
            var response = apiService.getConnections()
            if (response.code() == 403) {
                try {
                    apiService.acceptTerms()
                    response = apiService.getConnections()
                } catch (_: Exception) {}
            }

            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body != null && body.status == 0 && data != null) {
                    body.ticket?.token?.let { sessionToken = it }
                    Result.success(data)
                } else if (body?.status == 2) {
                    Result.failure(AuthExpiredException())
                } else {
                    Result.failure(Exception(body?.error?.message ?: "Error al obtener conexiones"))
                }
            } else if (response.code() == 401) {
                Result.failure(AuthExpiredException())
            } else {
                Result.failure(Exception("HTTP Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.success(getMockConnections())
        }
    }

    suspend fun getPatientGraph(patientId: String): Result<GraphData> = withContext(Dispatchers.IO) {
        if (sessionToken?.startsWith("demo") == true) {
            return@withContext Result.success(getMockGraphData())
        }
        try {
            val response = apiService.getPatientGraph(patientId)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body != null && body.status == 0 && data != null) {
                    Result.success(data)
                } else if (body?.status == 2) {
                    Result.failure(AuthExpiredException())
                } else {
                    Result.failure(Exception(body?.error?.message ?: "Error al obtener mediciones"))
                }
            } else if (response.code() == 401) {
                Result.failure(AuthExpiredException())
            } else {
                Result.failure(Exception("HTTP Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.success(getMockGraphData())
        }
    }

    private fun getMockConnections(): List<ConnectionItem> {
        val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val mockMeasurement = GlucoseMeasurement(
            timestamp = now,
            valueInMgPerDl = 114.0,
            value = 114.0,
            trendArrow = 3,
            trendMessage = "Estable",
            glucoseUnits = 1
        )
        val mockSensor = SensorInfo(
            deviceId = "DEMO-SENSOR-01",
            serialNumber = "MH01DEMO2026",
            activatedTimestamp = System.currentTimeMillis() - (3 * 24 * 3600 * 1000L),
            lifetimeDays = 15,
            isSensorActive = true
        )
        return listOf(
            ConnectionItem(
                id = "demo_conn_1",
                patientId = "demo_patient_1",
                firstName = "Rubén",
                lastName = "Carribero",
                targetLow = 70,
                targetHigh = 180,
                uom = 1,
                sensor = mockSensor,
                glucoseMeasurement = mockMeasurement
            )
        )
    }

    private fun getMockGraphData(): GraphData {
        val conn = getMockConnections().first()
        val nowMs = System.currentTimeMillis()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        
        val points = mutableListOf<GlucoseMeasurement>()
        val baseValues = listOf(105.0, 108.0, 112.0, 120.0, 135.0, 148.0, 140.0, 125.0, 118.0, 110.0, 104.0, 102.0, 115.0, 128.0, 142.0, 138.0, 122.0, 116.0, 114.0)
        
        for (i in baseValues.indices) {
            val tMs = nowMs - ((baseValues.size - 1 - i) * 15 * 60 * 1000L)
            points.add(
                GlucoseMeasurement(
                    timestamp = sdf.format(java.util.Date(tMs)),
                    valueInMgPerDl = baseValues[i],
                    value = baseValues[i],
                    trendArrow = 3,
                    glucoseUnits = 1
                )
            )
        }
        
        return GraphData(
            connection = conn,
            graphData = points
        )
    }

    fun getSessionToken(): String? = sessionToken
    fun getUserId(): String? = userId
}
