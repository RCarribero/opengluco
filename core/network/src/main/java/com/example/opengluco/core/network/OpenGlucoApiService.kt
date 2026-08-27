package com.example.opengluco.core.network

import com.example.opengluco.core.model.BaseResponse
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.core.model.GraphData
import com.example.opengluco.core.model.LoginData
import com.example.opengluco.core.model.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OpenGlucoApiService {

    @POST("llu/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<BaseResponse<LoginData>>

    @GET("llu/connections")
    suspend fun getConnections(): Response<BaseResponse<List<ConnectionItem>>>

    @GET("llu/connections/{patientId}/graph")
    suspend fun getPatientGraph(
        @Path("patientId") patientId: String
    ): Response<BaseResponse<GraphData>>

    @POST("llu/auth/terms/accept")
    suspend fun acceptTerms(): Response<BaseResponse<Map<String, String>>>
}
