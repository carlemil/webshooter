package se.kjellstrand.webshooter.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AuthTokenManager(context: Context) {
    private val masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun storeToken(newToken: String) {
        with(sharedPreferences.edit()) {
            putString(AUTH_TOKEN_KEY, newToken)
            apply()
        }
        println("$AUTH_TOKEN_KEY stored: $newToken")
        token = newToken
    }

    fun readToken(): String? {
        token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
        println("$AUTH_TOKEN_KEY read: $token")

        return token
    }

    companion object {
        private const val AUTH_TOKEN_KEY = "auth_token"
        var token: String? = null
    }
}