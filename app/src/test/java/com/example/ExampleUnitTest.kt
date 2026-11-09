package com.example

import com.example.util.EncryptionHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testTokenEncryptionDecryption() {
        val originalToken = "oauth2_bearer_test_token_secret_12345"
        val encrypted = EncryptionHelper.encryptString(originalToken)
        assertNotEquals(originalToken, encrypted)
        assertTrue(encrypted.isNotEmpty())

        val decrypted = EncryptionHelper.decryptString(encrypted)
        assertEquals(originalToken, decrypted)
    }
}
