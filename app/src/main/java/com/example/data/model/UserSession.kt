package com.example.data.model

data class UserSession(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val authProvider: AuthProvider = AuthProvider.NONE,
    val accessToken: String = "",
    val refreshToken: String = "",
    val tokenExpiresAt: Long = 0,
    val isEncryptedTokenStored: Boolean = false
) {
    val isLoggedIn: Boolean
        get() = accessToken.isNotEmpty() && System.currentTimeMillis() < tokenExpiresAt
}

enum class AuthProvider {
    NONE,
    EMAIL_PASSWORD,
    GOOGLE_OAUTH
}
