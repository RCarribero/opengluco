package com.example.opengluco.core.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeystoreCryptoHelper {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "opengluco_master_keystore_key_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    @Volatile
    private var jvmFallbackKey: SecretKey? = null

    private fun getOrCreateSecretKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(KEY_ALIAS)) {
                val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                if (entry != null) {
                    return entry.secretKey
                }
            }

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        } catch (_: Throwable) {
            // Entorno JVM / Unit Tests sin AndroidKeyStore: clave en memoria AES-256
            val existing = jvmFallbackKey
            if (existing != null) {
                existing
            } else {
                val keyGen = KeyGenerator.getInstance("AES")
                keyGen.init(256)
                val newKey = keyGen.generateKey()
                jvmFallbackKey = newKey
                newKey
            }
        }
    }

    fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // Concatenate IV + CipherBytes
            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

            val base64Str = try {
                Base64.encodeToString(combined, Base64.NO_WRAP)
            } catch (_: Throwable) {
                java.util.Base64.getEncoder().encodeToString(combined)
            }
            "ENC:$base64Str"
        } catch (_: Exception) {
            // Fail-closed: nunca devolver datos sensibles en texto claro
            ""
        }
    }

    fun decrypt(encryptedPayload: String): String {
        if (encryptedPayload.isBlank()) return ""
        if (!encryptedPayload.startsWith("ENC:")) {
            // Soporte de migración heredada para tokens no cifrados
            return encryptedPayload
        }

        return try {
            val rawBase64 = encryptedPayload.removePrefix("ENC:")
            val combined = try {
                Base64.decode(rawBase64, Base64.NO_WRAP)
            } catch (_: Throwable) {
                java.util.Base64.getDecoder().decode(rawBase64)
            }
            if (combined.size < GCM_IV_LENGTH) return ""

            val iv = ByteArray(GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)

            val cipherBytes = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.size)

            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            val plainBytes = cipher.doFinal(cipherBytes)
            String(plainBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            // En caso de payload corrupto o clave incorrecta, devolver vacío
            ""
        }
    }
}
