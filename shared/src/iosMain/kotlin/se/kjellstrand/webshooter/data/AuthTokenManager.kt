package se.kjellstrand.webshooter.data

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import io.github.aakira.napier.Napier

/**
 * iOS factory: builds an [AuthTokenManager] backed by the device Keychain.
 * The service name is the same as the Android prefs file name so the two
 * platforms stay symmetric.
 *
 * If the Keychain is in a state that makes reads throw (e.g. items written
 * under a previous code-signing identity and now inaccessible after a
 * provisioning change), wipe the entries for this service and rebuild —
 * mirrors the Android `EncryptedSharedPreferences` corruption-recovery path.
 */
@OptIn(ExperimentalSettingsImplementation::class)
fun createAuthTokenManager(): AuthTokenManager {
    val settings = try {
        val candidate = KeychainSettings(service = AuthTokenManager.PREFS_FILE)
        // Probe — surfaces Keychain errors here rather than later from a
        // ViewModel coroutine where they'd kill the app.
        candidate.getStringOrNull(PROBE_KEY)
        candidate
    } catch (t: Throwable) {
        Napier.w(
            "Keychain unusable for ${AuthTokenManager.PREFS_FILE}; clearing and recreating",
            t,
            tag = TAG,
        )
        runCatching { KeychainSettings(service = AuthTokenManager.PREFS_FILE).clear() }
        KeychainSettings(service = AuthTokenManager.PREFS_FILE)
    }
    return AuthTokenManager(settings)
}

private const val TAG = "AuthTokenManager"
private const val PROBE_KEY = "__probe__"
