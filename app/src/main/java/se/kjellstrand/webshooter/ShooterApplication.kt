package se.kjellstrand.webshooter

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.data.secure.SecurePrefs
import se.kjellstrand.webshooter.di.ApplicationScope
import javax.inject.Inject

@HiltAndroidApp
class ShooterApplication : Application() {

    @Inject lateinit var competitionsRepository: CompetitionsRepository
    @Inject lateinit var authTokenManager: AuthTokenManager
    @Inject lateinit var securePrefs: SecurePrefs
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        if (authTokenManager.readToken() != null) {
            applicationScope.launch {
                val age = System.currentTimeMillis() - securePrefs.getCompetitionsLastSync()
                if (age < COMPETITIONS_CACHE_TTL_MS) {
                    Log.i(TAG, "Skipping competitions sync, last synced ${age}ms ago")
                    return@launch
                }
                try {
                    competitionsRepository.syncAll()
                    securePrefs.setCompetitionsLastSync(System.currentTimeMillis())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync competitions on startup", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "ShooterApplication"
        private const val COMPETITIONS_CACHE_TTL_MS = 24L * 60 * 60 * 1000
    }
}
