package se.kjellstrand.webshooter.data

import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Android factory: builds an [AuthTokenManager] backed by an encrypted
 * SharedPreferences instance protected by an AndroidX Security MasterKey.
 *
 * If a previously stored set of preferences is corrupted (master key rotated
 * by the OS, etc.) the file is deleted and recreated rather than letting the
 * exception propagate — the app would be unable to start otherwise.
 */
fun createAuthTokenManager(context: Context): AuthTokenManager {
    // MasterKey and EncryptedSharedPreferences perform unavoidable
    // keystore/disk I/O during initialization.
    val oldPolicy = StrictMode.allowThreadDiskWrites()
    try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences: SharedPreferences = try {
            buildEncryptedPrefs(context, masterKey, AuthTokenManager.PREFS_FILE)
        } catch (e: Exception) {
            Log.w(TAG, "EncryptedSharedPreferences corrupted, clearing and recreating", e)
            context.deleteSharedPreferences(AuthTokenManager.PREFS_FILE)
            buildEncryptedPrefs(context, masterKey, AuthTokenManager.PREFS_FILE)
        }
        return AuthTokenManager(SharedPreferencesSettings(sharedPreferences))
    } finally {
        StrictMode.setThreadPolicy(oldPolicy)
    }
}

private fun buildEncryptedPrefs(
    context: Context,
    masterKey: MasterKey,
    fileName: String
): SharedPreferences = EncryptedSharedPreferences.create(
    context,
    fileName,
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

private const val TAG = "AuthTokenManager"
