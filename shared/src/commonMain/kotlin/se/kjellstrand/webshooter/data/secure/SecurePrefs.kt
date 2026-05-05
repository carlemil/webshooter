package se.kjellstrand.webshooter.data.secure

import com.russhwolf.settings.Settings

/**
 * Holds non-token user-scoped credentials (currently just the saved username
 * for the login screen "remember me" flow). The class itself is pure Kotlin —
 * platform-specific factories supply the underlying [Settings] backend.
 */
class SecurePrefs(private val settings: Settings) {

    fun saveUsername(username: String) {
        settings.putString(KEY_USERNAME, username)
    }

    fun getUsername(): String = settings.getString(KEY_USERNAME, "")

    fun clearUsername() {
        settings.remove(KEY_USERNAME)
    }

    companion object {
        const val FILE_NAME = "secure_prefs"
        private const val KEY_USERNAME = "username"
    }
}
