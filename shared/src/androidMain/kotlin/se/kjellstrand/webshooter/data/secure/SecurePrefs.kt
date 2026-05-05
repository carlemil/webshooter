package se.kjellstrand.webshooter.data.secure

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Android factory: builds a [SecurePrefs] backed by encrypted SharedPreferences.
 * If the existing prefs file is corrupted (master key rotated, etc.) it is
 * deleted and recreated rather than letting the exception propagate.
 */
fun createSecurePrefs(context: Context): SecurePrefs {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPrefs: SharedPreferences = try {
        buildEncryptedPrefs(context, masterKey, SecurePrefs.FILE_NAME)
    } catch (e: Exception) {
        Log.w(TAG, "EncryptedSharedPreferences corrupted, clearing and recreating", e)
        context.deleteSharedPreferences(SecurePrefs.FILE_NAME)
        buildEncryptedPrefs(context, masterKey, SecurePrefs.FILE_NAME)
    }
    return SecurePrefs(SharedPreferencesSettings(sharedPrefs))
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

private const val TAG = "SecurePrefs"
