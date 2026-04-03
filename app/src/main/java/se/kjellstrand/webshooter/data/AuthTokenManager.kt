package se.kjellstrand.webshooter.data

import android.content.Context
import android.os.StrictMode
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AuthTokenManager(context: Context) {
    private val masterKeyAlias: MasterKey
    private val sharedPreferences: android.content.SharedPreferences

    init {
        // MasterKey and EncryptedSharedPreferences perform unavoidable
        // keystore/disk I/O during initialization.
        val oldPolicy = StrictMode.allowThreadDiskWrites()
        try {
            masterKeyAlias = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            sharedPreferences = try {
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
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }

    fun storeToken(newToken: String) {
        with(sharedPreferences.edit()) {
            putString(AUTH_TOKEN_KEY, newToken)
            apply()
        }
        token = newToken
    }

    fun storeTokens(accessToken: String, newRefreshToken: String, expiresInSeconds: Long) {
        val expiresAt = System.currentTimeMillis() + expiresInSeconds * 1000
        with(sharedPreferences.edit()) {
            putString(AUTH_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, newRefreshToken)
            putLong(TOKEN_EXPIRES_AT_KEY, expiresAt)
            apply()
        }
        token = accessToken
        refreshToken = newRefreshToken
        tokenExpiresAtMillis = expiresAt
    }

    fun readToken(): String? {
        token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
        return token
    }

    fun readRefreshToken(): String? {
        refreshToken = sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
        return refreshToken
    }

    fun isTokenExpired(): Boolean {
        val expiresAt = tokenExpiresAtMillis
            ?: sharedPreferences.getLong(TOKEN_EXPIRES_AT_KEY, 0L)
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt
    }

    fun clearToken() {
        with(sharedPreferences.edit()) {
            remove(AUTH_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            remove(TOKEN_EXPIRES_AT_KEY)
            apply()
        }
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
        @Volatile var token: String? = null
        @Volatile var refreshToken: String? = null
        @Volatile var tokenExpiresAtMillis: Long? = null
    }
}