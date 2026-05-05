package se.kjellstrand.webshooter.data

import com.russhwolf.settings.Settings
import kotlinx.datetime.Clock
import kotlin.concurrent.Volatile

/**
 * Holds the OAuth access / refresh tokens persistently and exposes them to the
 * Ktor Auth plugin. The class itself is pure Kotlin — backend choice of the
 * underlying [Settings] (EncryptedSharedPreferences on Android, Keychain on
 * iOS) is left to the platform-specific factory.
 */
class AuthTokenManager(private val settings: Settings) {

    fun storeToken(newToken: String) {
        settings.putString(AUTH_TOKEN_KEY, newToken)
        token = newToken
    }

    fun storeTokens(accessToken: String, newRefreshToken: String, expiresInSeconds: Long) {
        val expiresAt = Clock.System.now().toEpochMilliseconds() + expiresInSeconds * 1000
        settings.putString(AUTH_TOKEN_KEY, accessToken)
        settings.putString(REFRESH_TOKEN_KEY, newRefreshToken)
        settings.putLong(TOKEN_EXPIRES_AT_KEY, expiresAt)
        token = accessToken
        refreshToken = newRefreshToken
        tokenExpiresAtMillis = expiresAt
    }

    fun readToken(): String? {
        token = settings.getStringOrNull(AUTH_TOKEN_KEY)
        return token
    }

    fun readRefreshToken(): String? {
        refreshToken = settings.getStringOrNull(REFRESH_TOKEN_KEY)
        return refreshToken
    }

    fun isTokenExpired(): Boolean {
        val expiresAt = tokenExpiresAtMillis
            ?: settings.getLong(TOKEN_EXPIRES_AT_KEY, 0L)
        return expiresAt > 0 && Clock.System.now().toEpochMilliseconds() >= expiresAt
    }

    fun clearToken() {
        settings.remove(AUTH_TOKEN_KEY)
        settings.remove(REFRESH_TOKEN_KEY)
        settings.remove(TOKEN_EXPIRES_AT_KEY)
        token = null
        refreshToken = null
        tokenExpiresAtMillis = null
    }

    companion object {
        const val PREFS_FILE = "auth_prefs"
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val TOKEN_EXPIRES_AT_KEY = "token_expires_at"

        @Volatile
        var token: String? = null

        @Volatile
        var refreshToken: String? = null

        @Volatile
        var tokenExpiresAtMillis: Long? = null
    }
}
