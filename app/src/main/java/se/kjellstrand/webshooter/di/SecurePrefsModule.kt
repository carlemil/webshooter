package se.kjellstrand.webshooter.data.secure

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePrefs @Inject constructor(@ApplicationContext context: Context) {

    companion object {
        private const val FILE_NAME = "secure_prefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val TAG = "SecurePrefs"
    }

    private val sharedPrefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPrefs = try {
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
    }

    fun saveCredentials(username: String, password: String) {
        sharedPrefs.edit {
            putString(KEY_USERNAME, username)
            putString(KEY_PASSWORD, password)
        }
    }

    fun getUsername(): String = sharedPrefs.getString(KEY_USERNAME, "") ?: ""
    fun getPassword(): String = sharedPrefs.getString(KEY_PASSWORD, "") ?: ""

    fun saveRememberMe(value: Boolean) {
        sharedPrefs.edit { putBoolean(KEY_REMEMBER_ME, value) }
    }

    fun getRememberMe(): Boolean = sharedPrefs.getBoolean(KEY_REMEMBER_ME, false)

    fun clearCredentials() {
        sharedPrefs.edit {
            remove(KEY_USERNAME)
            remove(KEY_PASSWORD)
        }
    }
}
