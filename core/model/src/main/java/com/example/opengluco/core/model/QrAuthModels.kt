package com.example.opengluco.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class QrDeviceType {
    @SerialName("wear_os")
    WEAR_OS,

    @SerialName("android_auto")
    ANDROID_AUTO
}

@Serializable
data class QrPairingPayload(
    @SerialName("protocol") val protocol: String = "OPENGLUCO_QR_AUTH_V2_ENCRYPTED",
    @SerialName("sessionId") val sessionId: String,
    @SerialName("deviceType") val deviceType: QrDeviceType,
    @SerialName("deviceName") val deviceName: String,
    @SerialName("ip") val ip: String? = null,
    @SerialName("port") val port: Int? = 8888,
    @SerialName("secretKeyHex") val secretKeyHex: String? = null,
    @SerialName("nonceHex") val nonceHex: String? = null,
    @SerialName("phoneBluetoothMac") val phoneBluetoothMac: String? = null,
    @SerialName("verificationCode") val verificationCode: String = "",
    @SerialName("timestamp") val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class QrEncryptedPayload(
    @SerialName("sessionId") val sessionId: String,
    @SerialName("encryptedDataHex") val encryptedDataHex: String,
    @SerialName("ivHex") val ivHex: String,
    @SerialName("timestamp") val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class QrSessionExchange(
    @SerialName("sessionId") val sessionId: String,
    @SerialName("email") val email: String,
    @SerialName("token") val token: String,
    @SerialName("userId") val userId: String,
    @SerialName("region") val region: String = "eu",
    @SerialName("phoneBluetoothMac") val phoneBluetoothMac: String? = null,
    @SerialName("timestamp") val timestamp: Long = System.currentTimeMillis()
)

