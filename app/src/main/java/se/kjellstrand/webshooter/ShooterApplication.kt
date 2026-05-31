package se.kjellstrand.webshooter

import android.app.Application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.MockInterceptor
import se.kjellstrand.webshooter.data.MockModeManager
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.data.secure.SecurePrefs
import se.kjellstrand.webshooter.di.ApplicationCoroutineScopeQualifier
import se.kjellstrand.webshooter.di.WebshooterConfig
import se.kjellstrand.webshooter.di.androidPlatformModule
import se.kjellstrand.webshooter.di.initKoin

class ShooterApplication : Application() {

    override fun onCreate() {
        // Boot Koin first so any Compose screen constructed afterwards
        // can resolve its ViewModel via koinViewModel.
        initKoin(
            androidPlatformModule(
                context = applicationContext,
                config = WebshooterConfig(
                    isDebug = BuildConfig.DEBUG,
                    baseUrl = BuildConfig.BASE_URL,
                    versionName = BuildConfig.VERSION_NAME,
                    clientSecret = BuildConfig.CLIENT_SECRET,
                ),
                extraOkHttpInterceptors = listOf(MockInterceptor(applicationContext)),
            )
        )

        super.onCreate()

        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }

        val koin = KoinPlatform.getKoin()
        MockModeManager.isMockMode = koin.get<SecurePrefs>().isMockMode()
        val authTokenManager = koin.get<AuthTokenManager>()
        if (authTokenManager.readToken() == null) return
        val applicationScope = koin.get<CoroutineScope>(ApplicationCoroutineScopeQualifier)
        val competitionsRepository = koin.get<CompetitionsRepository>()
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
