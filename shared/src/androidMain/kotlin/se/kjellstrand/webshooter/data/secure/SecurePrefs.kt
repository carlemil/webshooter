package se.kjellstrand.webshooter.data.secure

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

class SecurePrefs(context: Context) {

    companion object {
        private const val FILE_NAME = "secure_prefs"
        private const val KEY_USERNAME = "username"
        private const val TAG = "SecurePrefs"
    }

    private val settings: Settings

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPrefs: SharedPreferences = try {
            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.w(TAG, "EncryptedSharedPreferences corrupted, clearing and recreating", e)
            context.deleteSharedPreferences(FILE_NAME)
            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        settings = SharedPreferencesSettings(sharedPrefs)
    }

    fun saveUsername(username: String) {
        settings.putString(KEY_USERNAME, username)
    }

    fun getUsername(): String = settings.getString(KEY_USERNAME, "")

    fun clearUsername() {
        settings.remove(KEY_USERNAME)
    }
}
