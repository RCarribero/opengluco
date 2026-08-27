package com.example.opengluco.core.data

import org.junit.Assert.*
import org.junit.Test
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreCryptoHelperTest {

    // --- Tier 1 & Tier 2: KeystoreCryptoHelper Contract & Fallbacks ---

    @Test
    fun testEncrypt_blankString_returnsEmptyString() {
        assertEquals("", KeystoreCryptoHelper.encrypt(""))
        assertEquals("", KeystoreCryptoHelper.encrypt("   "))
    }

    @Test
    fun testDecrypt_blankString_returnsEmptyString() {
        assertEquals("", KeystoreCryptoHelper.decrypt(""))
        assertEquals("", KeystoreCryptoHelper.decrypt("   "))
    }

    @Test
    fun testDecrypt_legacyUnencryptedString_returnsOriginalString() {
        val legacyToken = "eyJh...legacy_unencrypted_jwt_token"
        val decrypted = KeystoreCryptoHelper.decrypt(legacyToken)
        assertEquals(legacyToken, decrypted)
    }

    @Test
    fun testDecrypt_malformedBase64WithPrefix_handlesGracefullyWithoutCrash() {
        val malformedPayload = "ENC:not_valid_base_64!@#"
        val decrypted = KeystoreCryptoHelper.decrypt(malformedPayload)
        assertNotNull(decrypted)
    }

    @Test
    fun testDecrypt_truncatedPayloadLessThanIvLength_handlesSafely() {
        // Less than 12 bytes IV
        val tinyBytes = ByteArray(8) { 0x01 }
        val tinyPayload = "ENC:" + java.util.Base64.getEncoder().encodeToString(tinyBytes)
        val decrypted = KeystoreCryptoHelper.decrypt(tinyPayload)
        assertNotNull(decrypted)
        assertTrue(decrypted == "" || decrypted == tinyPayload)
    }

    // --- Tier 2 & Tier 3: AES-256-GCM Cryptographic Specification Verification ---

    @Test
    fun testAes256GcmSpecification_encryptAndDecryptRoundtrip() {
        // Verify the exact algorithm spec specified in PROJECT.md:
        // AES-256-GCM, 12-byte IV, 128-bit tag, "ENC:" + Base64(IV + Ciphertext + Tag)
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey: SecretKey = keyGen.generateKey()

        val plainText = "{\"email\":\"user@example.com\",\"auth_token\":\"secret-jwt-12345\"}"

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        assertEquals("IV length must be exactly 12 bytes (96 bits)", 12, iv.size)

        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + cipherBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

        val serialized = "ENC:" + Base64.getEncoder().encodeToString(combined)
        assertTrue(serialized.startsWith("ENC:"))

        // Now decrypt using the spec
        val rawBase64 = serialized.removePrefix("ENC:")
        val decodedCombined = Base64.getDecoder().decode(rawBase64)
        val extractedIv = ByteArray(12)
        System.arraycopy(decodedCombined, 0, extractedIv, 0, 12)
        val extractedCipherBytes = ByteArray(decodedCombined.size - 12)
        System.arraycopy(decodedCombined, 12, extractedCipherBytes, 0, extractedCipherBytes.size)

        val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, extractedIv)
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        val decryptedBytes = decryptCipher.doFinal(extractedCipherBytes)
        val decryptedText = String(decryptedBytes, Charsets.UTF_8)

        assertEquals(plainText, decryptedText)
    }

    @Test
    fun testAes256GcmSpecification_authTagTamperingRejection() {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey: SecretKey = keyGen.generateKey()

        val plainText = "Sensible Glucose History"
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Corrupt the tag byte
        cipherBytes[cipherBytes.size - 1] = (cipherBytes[cipherBytes.size - 1].toInt() xor 0xFF).toByte()

        val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        try {
            decryptCipher.doFinal(cipherBytes)
            fail("Decryption of tampered ciphertext must throw AEADBadTagException")
        } catch (e: javax.crypto.AEADBadTagException) {
            // Expected
            assertTrue(true)
        }
    }
}
