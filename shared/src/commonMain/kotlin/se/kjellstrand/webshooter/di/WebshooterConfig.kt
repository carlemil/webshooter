package se.kjellstrand.webshooter.di

/**
 * Deployment-time constants supplied by each platform host (Android
 * BuildConfig, iOS Info.plist, etc.). Provided by the platform Koin module
 * so commonMain code (HttpClient factory, LoginRepository) can read them
 * via [org.koin.core.component.get].
 */
data class WebshooterConfig(
    val isDebug: Boolean,
    val baseUrl: String,
    val versionName: String,
    val clientSecret: String,
    /**
     * Gate for the Crashlytics platform binding. True only on the prod
     * release flavor on Android (set via `BuildConfig.CRASH_REPORTING_ENABLED`)
     * and on the Release-Prod scheme on iOS (set via Info.plist
     * `WebshooterCrashReportingEnabled`). Staging, debug, and mock-mode
     * runs bind [se.kjellstrand.webshooter.data.telemetry.NoOpCrashReporter]
     * so they never reach the dashboard.
     */
    val crashReportingEnabled: Boolean = false,
)
