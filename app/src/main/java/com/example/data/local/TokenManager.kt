package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AuthProvider
import com.example.data.model.UserSession
import com.example.util.EncryptionHelper

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "secure_auth_prefs"
        private const val KEY_ENCRYPTED_ACCESS_TOKEN = "enc_access_token"
        private const val KEY_ENCRYPTED_REFRESH_TOKEN = "enc_refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_NAME = "user_name"
        private const val KEY_AVATAR = "user_avatar"
        private const val KEY_PROVIDER = "auth_provider"
        private const val KEY_EXPIRES_AT = "token_expires_at"
    }

    fun saveSession(session: UserSession) {
        val encryptedAccess = EncryptionHelper.encryptString(session.accessToken)
        val encryptedRefresh = EncryptionHelper.encryptString(session.refreshToken)

        prefs.edit()
            .putString(KEY_ENCRYPTED_ACCESS_TOKEN, encryptedAccess)
            .putString(KEY_ENCRYPTED_REFRESH_TOKEN, encryptedRefresh)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_NAME, session.name)
            .putString(KEY_AVATAR, session.avatarUrl)
            .putString(KEY_PROVIDER, session.authProvider.name)
            .putLong(KEY_EXPIRES_AT, session.tokenExpiresAt)
            .apply()
    }

    fun getSession(): UserSession {
        val encAccess = prefs.getString(KEY_ENCRYPTED_ACCESS_TOKEN, "") ?: ""
        val encRefresh = prefs.getString(KEY_ENCRYPTED_REFRESH_TOKEN, "") ?: ""
        val userId = prefs.getString(KEY_USER_ID, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val name = prefs.getString(KEY_NAME, "") ?: ""
        val avatar = prefs.getString(KEY_AVATAR, "") ?: ""
        val providerStr = prefs.getString(KEY_PROVIDER, AuthProvider.NONE.name) ?: AuthProvider.NONE.name
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)

        val accessToken = EncryptionHelper.decryptString(encAccess)
        val refreshToken = EncryptionHelper.decryptString(encRefresh)
        val provider = try {
            AuthProvider.valueOf(providerStr)
        } catch (_: Exception) {
            AuthProvider.NONE
        }

        return UserSession(
            userId = userId,
            email = email,
            name = name,
            avatarUrl = avatar,
            authProvider = provider,
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenExpiresAt = expiresAt,
            isEncryptedTokenStored = encAccess.isNotEmpty()
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
