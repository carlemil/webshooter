package se.kjellstrand.webshooter.ui.login

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePrefs {
    private const val FILE_NAME = "secure_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"

    private lateinit var sharedPrefs: SharedPreferences

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPrefs = EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveCredentials(username: String, password: String) {
        sharedPrefs.edit {
            putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
        }
    }

    fun getUsername(): String = sharedPrefs.getString(KEY_USERNAME, "") ?: ""
    fun getPassword(): String = sharedPrefs.getString(KEY_PASSWORD, "") ?: ""
}
