package com.example.opengluco.core.data

import android.graphics.Bitmap
import android.graphics.Color
import com.example.opengluco.core.model.QrDeviceType
import com.example.opengluco.core.model.QrPairingPayload
import com.example.opengluco.core.model.QrSessionExchange
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.util.UUID
import java.util.concurrent.TimeUnit

object QrAuthHelper {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private fun generateRandomHex(byteCount: Int): String {
        val random = java.security.SecureRandom()
        val bytes = ByteArray(byteCount)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun encryptAesGcm(plainText: String, keyHex: String, ivHex: String): String {
        val keyBytes = hexToBytes(keyHex)
        val ivBytes = hexToBytes(ivHex)
        val secretKey = javax.crypto.spec.SecretKeySpec(keyBytes, "AES")
        val gcmSpec = javax.crypto.spec.GCMParameterSpec(128, ivBytes)

        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return cipherBytes.joinToString("") { "%02x".format(it) }
    }

    fun decryptAesGcm(cipherHex: String, keyHex: String, ivHex: String): String {
        val keyBytes = hexToBytes(keyHex)
        val ivBytes = hexToBytes(ivHex)
        val cipherBytes = hexToBytes(cipherHex)
        val secretKey = javax.crypto.spec.SecretKeySpec(keyBytes, "AES")
        val gcmSpec = javax.crypto.spec.GCMParameterSpec(128, ivBytes)

        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, Charsets.UTF_8)
    }

    fun generateVerificationCode(seed: String? = null): String {
        val number = if (!seed.isNullOrBlank()) {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val hash = md.digest(seed.toByteArray(Charsets.UTF_8))
            val value = ((hash[0].toInt() and 0xFF) shl 16) or
                    ((hash[1].toInt() and 0xFF) shl 8) or
                    (hash[2].toInt() and 0xFF)
            (value % 900000) + 100000
        } else {
            java.security.SecureRandom().nextInt(900000) + 100000
        }
        return String.format(java.util.Locale.US, "%03d %03d", number / 1000, number % 1000)
    }

    fun createPairingPayload(deviceType: QrDeviceType, deviceName: String, port: Int = 8888): QrPairingPayload {
        val sId = UUID.randomUUID().toString()
        val keyHex = generateRandomHex(32)
        val nonce = generateRandomHex(12)
        val code = generateVerificationCode(sId + keyHex)

        return QrPairingPayload(
            sessionId = sId,
            deviceType = deviceType,
            deviceName = deviceName,
            ip = getLocalIpAddress(),
            port = port,
            secretKeyHex = keyHex, // 256 bits AES key
            nonceHex = nonce,      // 96 bits GCM IV
            verificationCode = code
        )
    }

    fun serializePairingPayload(payload: QrPairingPayload): String {
        return json.encodeToString(payload)
    }

    fun parsePairingPayload(rawQrContent: String): QrPairingPayload? {
        return try {
            json.decodeFromString<QrPairingPayload>(rawQrContent)
        } catch (e: Exception) {
            null
        }
    }

    fun createSessionExchange(
        sessionId: String,
        email: String,
        token: String,
        userId: String,
        region: String = "eu",
        phoneBluetoothMac: String? = null
    ): String {
        val exchange = QrSessionExchange(
            sessionId = sessionId,
            email = email,
            token = token,
            userId = userId,
            region = region,
            phoneBluetoothMac = phoneBluetoothMac
        )
        return json.encodeToString(exchange)
    }

    fun parseSessionExchange(rawContent: String): QrSessionExchange? {
        return try {
            json.decodeFromString<QrSessionExchange>(rawContent)
        } catch (e: Exception) {
            null
        }
    }

    private const val MAX_PAIRING_BODY_BYTES = 32 * 1024

    fun isPrivateOrLocalAddress(host: String): Boolean {
        if (host.isBlank()) return false
        return try {
            val addr = java.net.InetAddress.getByName(host.trim())
            addr.isLoopbackAddress || addr.isSiteLocalAddress || addr.isLinkLocalAddress
        } catch (_: Exception) {
            false
        }
    }

    suspend fun sendSessionOverNetwork(
        targetIp: String,
        targetPort: Int,
        sessionJson: String,
        secretKeyHex: String? = null,
        nonceHex: String? = null,
        sessionId: String = UUID.randomUUID().toString()
    ): Boolean = withContext(Dispatchers.IO) {
        if (secretKeyHex.isNullOrBlank() || nonceHex.isNullOrBlank()) {
            // Rechazo estricto: prohibido transmitir credenciales en texto plano por la red local
            return@withContext false
        }
        if (targetPort !in 1024..65535 || !isPrivateOrLocalAddress(targetIp)) {
            return@withContext false
        }
        try {
            val encryptedDataHex = encryptAesGcm(sessionJson, secretKeyHex, nonceHex)
            val encryptedModel = com.example.opengluco.core.model.QrEncryptedPayload(
                sessionId = sessionId,
                encryptedDataHex = encryptedDataHex,
                ivHex = nonceHex
            )
            val bodyPayload = json.encodeToString(encryptedModel)
            val bodyBytes = bodyPayload.toByteArray(Charsets.UTF_8)

            // Usar socket TCP directo para IPs privadas LAN (el payload ya va cifrado con AES-256-GCM)
            java.net.Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress(targetIp.trim(), targetPort), 5000)
                socket.soTimeout = 5000
                val out = socket.getOutputStream()
                val headers = "POST /auth HTTP/1.1\r\n" +
                    "Host: $targetIp:$targetPort\r\n" +
                    "Content-Type: application/json; charset=utf-8\r\n" +
                    "Content-Length: ${bodyBytes.size}\r\n" +
                    "Connection: close\r\n\r\n"
                out.write(headers.toByteArray(Charsets.US_ASCII))
                out.write(bodyBytes)
                out.flush()

                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.US_ASCII))
                val statusLine = reader.readLine() ?: return@withContext false
                statusLine.contains("200")
            }
        } catch (_: Exception) {
            false
        }
    }

    fun startPairingServer(
        port: Int = 8888,
        secretKeyHex: String? = null,
        nonceHex: String? = null,
        expectedSessionId: String? = null,
        onSessionReceived: (QrSessionExchange) -> Unit
    ): ServerSocket? {
        return try {
            val serverSocket = ServerSocket(port)
            Thread {
                try {
                    while (!serverSocket.isClosed) {
                        val client = serverSocket.accept()
                        Thread {
                            try {
                                client.soTimeout = 5000
                                val input = client.getInputStream()
                                val headerBytes = java.io.ByteArrayOutputStream()
                                var prev = -1
                                var crlfCount = 0
                                while (headerBytes.size() < 4096) {
                                    val b = input.read()
                                    if (b == -1) break
                                    headerBytes.write(b)
                                    if (prev == '\r'.code && b == '\n'.code) {
                                        crlfCount++
                                        if (crlfCount == 2) break
                                    } else if (b != '\r'.code) {
                                        crlfCount = 0
                                    }
                                    prev = b
                                }

                                val headerText = headerBytes.toString(Charsets.US_ASCII.name())
                                val contentLength = headerText.lines()
                                    .firstOrNull { it.lowercase().startsWith("content-length:") }
                                    ?.substringAfter(":")
                                    ?.trim()
                                    ?.toIntOrNull() ?: 0

                                if (contentLength <= 0 || contentLength > MAX_PAIRING_BODY_BYTES) {
                                    val out = client.getOutputStream()
                                    val resp = if (contentLength > MAX_PAIRING_BODY_BYTES) {
                                        "HTTP/1.1 413 Payload Too Large\r\nContent-Length: 0\r\n\r\n"
                                    } else {
                                        "HTTP/1.1 400 Bad Request\r\nContent-Length: 0\r\n\r\n"
                                    }
                                    out.write(resp.toByteArray(Charsets.US_ASCII))
                                    out.flush()
                                    client.close()
                                    return@Thread
                                }

                                val bodyBuffer = ByteArray(contentLength)
                                var totalRead = 0
                                while (totalRead < contentLength) {
                                    val read = input.read(bodyBuffer, totalRead, contentLength - totalRead)
                                    if (read == -1) break
                                    totalRead += read
                                }
                                if (totalRead != contentLength) {
                                    client.close()
                                    return@Thread
                                }
                                val rawBody = String(bodyBuffer, Charsets.UTF_8)

                                var session: QrSessionExchange? = null
                                if (!secretKeyHex.isNullOrBlank() && !nonceHex.isNullOrBlank()) {
                                    try {
                                        val encPayload = json.decodeFromString<com.example.opengluco.core.model.QrEncryptedPayload>(rawBody)
                                        val ivMatches = encPayload.ivHex.equals(nonceHex, ignoreCase = true)
                                        val outerSessionMatches = expectedSessionId.isNullOrBlank() || encPayload.sessionId == expectedSessionId
                                        if (ivMatches && outerSessionMatches) {
                                            val decryptedJson = decryptAesGcm(encPayload.encryptedDataHex, secretKeyHex, nonceHex)
                                            val parsed = parseSessionExchange(decryptedJson)
                                            if (parsed != null && (expectedSessionId.isNullOrBlank() || parsed.sessionId == expectedSessionId)) {
                                                session = parsed
                                            }
                                        }
                                    } catch (_: Exception) {
                                        session = null
                                    }
                                }

                                if (session != null) {
                                    val out = client.getOutputStream()
                                    val httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: 15\r\n\r\n{\"status\":\"ok\"}"
                                    out.write(httpResponse.toByteArray(Charsets.UTF_8))
                                    out.flush()
                                    client.close()
                                    try { serverSocket.close() } catch (_: Exception) {}
                                    onSessionReceived(session)
                                    return@Thread
                                } else {
                                    val out = client.getOutputStream()
                                    val errorResponse = "HTTP/1.1 401 Unauthorized\r\nContent-Type: application/json\r\nContent-Length: 26\r\n\r\n{\"error\":\"unauthorized\"}"
                                    out.write(errorResponse.toByteArray(Charsets.UTF_8))
                                    out.flush()
                                }
                                client.close()
                            } catch (_: Exception) {
                                try { client.close() } catch (_: Exception) {}
                            }
                        }.start()
                    }
                } catch (_: Exception) {}
            }.start()
            serverSocket
        } catch (e: Exception) {
            null
        }
    }

    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
