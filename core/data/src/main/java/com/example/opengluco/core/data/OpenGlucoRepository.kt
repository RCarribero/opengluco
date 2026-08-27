package com.example.opengluco.core.data

import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.core.model.GraphData
import com.example.opengluco.core.model.LoginData
import com.example.opengluco.core.model.LoginRequest
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
            level = HttpLoggingInterceptor.Level.BODY
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
                } else {
                    Result.failure(Exception(body?.error?.message ?: "Error de autenticación (${body?.status})"))
                }
            } else {
                Result.failure(Exception("HTTP Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConnections(): Result<List<ConnectionItem>> = withContext(Dispatchers.IO) {
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
                } else {
                    Result.failure(Exception(body?.error?.message ?: "Error al obtener conexiones"))
                }
            } else {
                Result.failure(Exception("HTTP Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPatientGraph(patientId: String): Result<GraphData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPatientGraph(patientId)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body != null && body.status == 0 && data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception(body?.error?.message ?: "Error al obtener mediciones"))
                }
            } else {
                Result.failure(Exception("HTTP Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSessionToken(): String? = sessionToken
    fun getUserId(): String? = userId
}
