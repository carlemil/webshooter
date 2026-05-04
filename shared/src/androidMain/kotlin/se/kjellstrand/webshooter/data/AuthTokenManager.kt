package se.kjellstrand.webshooter.data

import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

class AuthTokenManager(context: Context) {
    private val masterKeyAlias: MasterKey
    private val settings: Settings

    init {
        // MasterKey and EncryptedSharedPreferences perform unavoidable
        // keystore/disk I/O during initialization.
        val oldPolicy = StrictMode.allowThreadDiskWrites()
        try {
            masterKeyAlias = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences: SharedPreferences = try {
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE,
                    masterKeyAlias,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                Log.w(TAG, "EncryptedSharedPreferences corrupted, clearing and recreating", e)
                context.deleteSharedPreferences(PREFS_FILE)
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE,
                    masterKeyAlias,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }
            settings = SharedPreferencesSettings(sharedPreferences)
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }

    fun storeToken(newToken: String) {
        settings.putString(AUTH_TOKEN_KEY, newToken)
        token = newToken
    }

    fun storeTokens(accessToken: String, newRefreshToken: String, expiresInSeconds: Long) {
        val expiresAt = System.currentTimeMillis() + expiresInSeconds * 1000
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
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt
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
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val TOKEN_EXPIRES_AT_KEY = "token_expires_at"
        private const val PREFS_FILE = "auth_prefs"
        private const val TAG = "AuthTokenManager"
        @Volatile
        var token: String? = null
        @Volatile
        var refreshToken: String? = null
        @Volatile
        var tokenExpiresAtMillis: Long? = null
    }
}
