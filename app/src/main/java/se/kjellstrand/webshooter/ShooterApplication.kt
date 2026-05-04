package se.kjellstrand.webshooter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
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

        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }

        if (authTokenManager.readToken() == null) return
        applicationScope.launch {
            try {
                competitionsRepository.syncAll()
            } catch (e: Exception) {
                Napier.w("Failed to sync competitions on startup", e, TAG)
            }
        }
    }

    companion object {
        private const val TAG = "ShooterApplication"
    }
}
