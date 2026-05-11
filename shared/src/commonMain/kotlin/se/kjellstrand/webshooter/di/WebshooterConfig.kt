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
)
