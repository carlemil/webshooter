package se.kjellstrand.webshooter.data.secure

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings

/**
 * iOS factory: builds a [SecurePrefs] backed by the device Keychain.
 */
@OptIn(ExperimentalSettingsImplementation::class)
fun createSecurePrefs(): SecurePrefs =
    SecurePrefs(KeychainSettings(service = SecurePrefs.FILE_NAME))
