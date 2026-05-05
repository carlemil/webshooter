package se.kjellstrand.webshooter.data

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings

/**
 * iOS factory: builds an [AuthTokenManager] backed by the device Keychain.
 * The service name is the same as the Android prefs file name so the two
 * platforms stay symmetric.
 */
@OptIn(ExperimentalSettingsImplementation::class)
fun createAuthTokenManager(): AuthTokenManager =
    AuthTokenManager(KeychainSettings(service = AuthTokenManager.PREFS_FILE))
