package se.kjellstrand.webshooter.data.secure

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import se.kjellstrand.webshooter.data.common.CompetitionStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePrefs @Inject constructor(@ApplicationContext context: Context) {

    companion object {
        private const val FILE_NAME = "secure_prefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_COMPETITION_STATUS = "competition_status"
    }

    private val sharedPrefs: SharedPreferences

    init {
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
            putString(KEY_PASSWORD, password)
        }
    }

    fun getUsername(): String = sharedPrefs.getString(KEY_USERNAME, "") ?: ""
    fun getPassword(): String = sharedPrefs.getString(KEY_PASSWORD, "") ?: ""

    fun saveCompetitionStatus(status: CompetitionStatus) {
        sharedPrefs.edit {
            putString(KEY_COMPETITION_STATUS, status.name)
        }
    }

    fun getCompetitionStatus(): CompetitionStatus {
        val statusName = sharedPrefs.getString(KEY_COMPETITION_STATUS, CompetitionStatus.COMPLETED.name)
        return CompetitionStatus.valueOf(statusName ?: CompetitionStatus.COMPLETED.name)
    }
}
