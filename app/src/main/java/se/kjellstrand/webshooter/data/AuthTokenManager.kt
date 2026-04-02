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

    fun readToken(): String? {
        token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)

        return token
    }

    fun clearToken() {
        with(sharedPreferences.edit()) {
            remove(AUTH_TOKEN_KEY)
            apply()
        }
        token = null
    }

    companion object {
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val PREFS_FILE = "auth_prefs"
        private const val TAG = "AuthTokenManager"
        var token: String? = null
    }
}