package com.example.opengluco.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

class OpenGlucoInterceptor(
    private val tokenProvider: () -> String?,
    private val accountIdProvider: () -> String?,
    private val appVersion: String = "4.16.0"
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("product", "llu.android")
            .header("version", appVersion)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")

        val token = tokenProvider()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val accountId = accountIdProvider()
        if (!accountId.isNullOrBlank()) {
            val accountIdHash = sha256(accountId)
            requestBuilder.header("account-id", accountIdHash)
        }

        return chain.proceed(requestBuilder.build())
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
