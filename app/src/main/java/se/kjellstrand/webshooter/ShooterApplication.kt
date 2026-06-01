package se.kjellstrand.webshooter

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
import se.kjellstrand.webshooter.data.telemetry.CrashReporter
import se.kjellstrand.webshooter.data.telemetry.CrashlyticsAntilog
import se.kjellstrand.webshooter.di.ApplicationCoroutineScopeQualifier
import se.kjellstrand.webshooter.di.WebshooterConfig
import se.kjellstrand.webshooter.di.androidPlatformModule
import se.kjellstrand.webshooter.di.initKoin

class ShooterApplication : Application() {

    override fun onCreate() {
        // Initialise Firebase before Koin so AndroidCrashReporter (resolved
        // lazily from the Koin graph) has a live FirebaseCrashlytics
        // singleton when it's first touched. The Crashlytics gradle plugin
        // applied at the :app level already enables the dSYM/mapping
        // upload phase; this call only sets the runtime collection flag.
        if (BuildConfig.CRASH_REPORTING_ENABLED) {
            FirebaseApp.initializeApp(this)
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
        }

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
                    crashReportingEnabled = BuildConfig.CRASH_REPORTING_ENABLED,
                ),
                extraOkHttpInterceptors = listOf(MockInterceptor(applicationContext)),
            )
        )

        super.onCreate()

        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }

        val koin = KoinPlatform.getKoin()
        val crashReporter = koin.get<CrashReporter>()
        if (BuildConfig.CRASH_REPORTING_ENABLED) {
            Napier.base(CrashlyticsAntilog(crashReporter))
            // Boot keys — visible in every Crashlytics issue. Identifies
            // which build the crash came from.
            crashReporter.setCustomKey("platform", "android")
            crashReporter.setCustomKey("flavor", BuildConfig.FLAVOR)
            crashReporter.setCustomKey("baseUrl", BuildConfig.BASE_URL)
            crashReporter.setCustomKey("versionName", BuildConfig.VERSION_NAME)
        }
        MockModeManager.isMockMode = koin.get<SecurePrefs>().isMockMode()
        crashReporter.setCustomKey("mockMode", MockModeManager.isMockMode)
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
