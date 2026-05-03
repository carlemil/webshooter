package se.kjellstrand.webshooter

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.di.ApplicationScope
import javax.inject.Inject

@HiltAndroidApp
class ShooterApplication : Application() {

    @Inject lateinit var competitionsRepository: CompetitionsRepository
    @Inject lateinit var authTokenManager: AuthTokenManager
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        if (authTokenManager.readToken() == null) return
        applicationScope.launch {
            try {
                competitionsRepository.syncAll()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync competitions on startup", e)
            }
        }
    }

    companion object {
        private const val TAG = "ShooterApplication"
    }
}
