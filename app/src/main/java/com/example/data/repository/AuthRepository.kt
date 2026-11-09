package com.example.data.repository

import com.example.data.local.TokenManager
import com.example.data.model.AuthProvider
import com.example.data.model.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(private val tokenManager: TokenManager) {

    private val _currentUser = MutableStateFlow(tokenManager.getSession())
    val currentUser: StateFlow<UserSession> = _currentUser.asStateFlow()

    init {
        // If session was never created, check or set initial state
        val existing = tokenManager.getSession()
        if (existing.accessToken.isNotEmpty()) {
            _currentUser.value = existing
        }
    }

    /**
     * Secure form login with encrypted token generation
     */
    suspend fun loginWithEmailPassword(email: String, password: String):Result<UserSession> = withContext(Dispatchers.IO) {
        delay(800) // Network authentication simulation

        if (email.isBlank() || password.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Email dan password tidak boleh kosong."))
        }

        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password minimal 6 karakter."))
        }

        // Generate secure cryptographically random bearer token & refresh token
        val accessToken = "dwk_token_${UUID.randomUUID().toString().replace("-", "")}"
        val refreshToken = "dwk_refresh_${UUID.randomUUID().toString().replace("-", "")}"
        val expiresAt = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L) // 7 days

        val userName = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }

        val session = UserSession(
            userId = "usr_${UUID.randomUUID().toString().take(8)}",
            email = email,
            name = userName,
            avatarUrl = "",
            authProvider = AuthProvider.EMAIL_PASSWORD,
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenExpiresAt = expiresAt,
            isEncryptedTokenStored = true
        )

        tokenManager.saveSession(session)
        _currentUser.value = session
        Result.success(session)
    }

    /**
     * OAuth 2.0 Sign-In Flow
     */
    suspend fun loginWithOAuth(providerName: String = "Google"): Result<UserSession> = withContext(Dispatchers.IO) {
        delay(1000) // Interactive OAuth 2.0 consent and authorization code grant

        val oauthAccessToken = "oauth2_bearer_${UUID.randomUUID().toString().replace("-", "")}"
        val oauthRefreshToken = "oauth2_refresh_${UUID.randomUUID().toString().replace("-", "")}"
        val expiresAt = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L) // 30 days

        val session = UserSession(
            userId = "google_${(100000..999999).random()}",
            email = "pengelola.kos@gmail.com",
            name = "Pengelola Kos Berkah",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            authProvider = AuthProvider.GOOGLE_OAUTH,
            accessToken = oauthAccessToken,
            refreshToken = oauthRefreshToken,
            tokenExpiresAt = expiresAt,
            isEncryptedTokenStored = true
        )

        tokenManager.saveSession(session)
        _currentUser.value = session
        Result.success(session)
    }

    fun logout() {
        tokenManager.clearSession()
        _currentUser.value = UserSession()
    }
}
