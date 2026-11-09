package com.example.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionHelper {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val INTERNAL_APP_KEY = "DataWargaKos2026SecretInternalSaltKeySecure"

    /**
     * Derive a 256-bit AES key from a passphrase or default internal key
     */
    private fun deriveKey(passphrase: String): SecretKeySpec {
        val sha256 = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha256.digest(passphrase.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypt a string with AES-GCM
     */
    fun encryptString(plainText: String, secretKey: String = INTERNAL_APP_KEY): String {
        if (plainText.isEmpty()) return ""
        val key = deriveKey(secretKey)
        val iv = ByteArray(IV_LENGTH_BYTE)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Prepend IV to ciphertext
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

        return Base64.getEncoder().encodeToString(combined)
    }

    /**
     * Decrypt a string with AES-GCM
     */
    fun decryptString(encryptedBase64: String, secretKey: String = INTERNAL_APP_KEY): String {
        if (encryptedBase64.isEmpty()) return ""
        try {
            val combined = Base64.getDecoder().decode(encryptedBase64)
            if (combined.size < IV_LENGTH_BYTE) return ""

            val iv = ByteArray(IV_LENGTH_BYTE)
            System.arraycopy(combined, 0, iv, 0, iv.size)

            val cipherText = ByteArray(combined.size - IV_LENGTH_BYTE)
            System.arraycopy(combined, IV_LENGTH_BYTE, cipherText, 0, cipherText.size)

            val key = deriveKey(secretKey)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))

            val decrypted = cipher.doFinal(cipherText)
            return String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    /**
     * Encrypt a file (PDF or Excel) using AES-GCM with a user-supplied password
     */
    fun encryptFile(sourceFile: File, destinationFile: File, password: String): Boolean {
        return try {
            val key = deriveKey(password)
            val iv = ByteArray(IV_LENGTH_BYTE)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))

            FileOutputStream(destinationFile).use { fos ->
                // Write magic header: 'ENC' + IV
                fos.write("ENCDWK".toByteArray(Charsets.UTF_8))
                fos.write(iv)
                CipherOutputStream(fos, cipher).use { cos ->
                    FileInputStream(sourceFile).use { fis ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (fis.read(buffer).also { bytesRead = it } != -1) {
                            cos.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Decrypt an encrypted file to a temporary file for preview or verification
     */
    fun decryptFile(sourceFile: File, destinationFile: File, password: String): Boolean {
        return try {
            FileInputStream(sourceFile).use { fis ->
                val header = ByteArray(6)
                fis.read(header)
                if (String(header, Charsets.UTF_8) != "ENCDWK") {
                    return false
                }

                val iv = ByteArray(IV_LENGTH_BYTE)
                fis.read(iv)

                val key = deriveKey(password)
                val cipher = Cipher.getInstance(ALGORITHM)
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))

                CipherInputStream(fis, cipher).use { cis ->
                    FileOutputStream(destinationFile).use { fos ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (cis.read(buffer).also { bytesRead = it } != -1) {
                            fos.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
