package se.kjellstrand.webshooter.di

import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.SessionManager
import se.kjellstrand.webshooter.data.createAuthTokenManager
import se.kjellstrand.webshooter.data.createWebshooterHttpClient
import se.kjellstrand.webshooter.data.db.AppDatabase
import se.kjellstrand.webshooter.data.db.createAppDatabase
import se.kjellstrand.webshooter.data.secure.SecurePrefs
import se.kjellstrand.webshooter.data.secure.createSecurePrefs
import se.kjellstrand.webshooter.data.telemetry.CrashReporter
import se.kjellstrand.webshooter.data.telemetry.CrashReporterBridge
import se.kjellstrand.webshooter.data.telemetry.IosCrashReporter
import se.kjellstrand.webshooter.data.telemetry.NoOpCrashReporter
import se.kjellstrand.webshooter.ui.platform.CalendarOpener
import se.kjellstrand.webshooter.ui.platform.IosCalendarOpener
import se.kjellstrand.webshooter.ui.platform.IosUrlLauncher
import se.kjellstrand.webshooter.ui.platform.UrlLauncher

/**
 * iOS Koin module — supplies the Darwin-engine HttpClient, the iOS Room
 * database (Documents-directory-backed), and Keychain-backed
 * [AuthTokenManager]/[SecurePrefs]. Pair with [sharedModule] via
 * [initKoin] from the SwiftUI app entry point.
 */
fun iosPlatformModule(
    config: WebshooterConfig,
    crashReporterBridge: CrashReporterBridge? = null,
): Module = module {
    single { config }
    single { createAppDatabase() }
    single<AuthTokenManager> { createAuthTokenManager() }
    single<SecurePrefs> { createSecurePrefs() }
    single<UrlLauncher> { IosUrlLauncher() }
    single<CalendarOpener> { IosCalendarOpener() }
    single<CrashReporter> {
        if (config.crashReportingEnabled && crashReporterBridge != null) {
            IosCrashReporter(crashReporterBridge)
        } else {
            NoOpCrashReporter
        }
    }
    single<HttpClient> {
        createWebshooterHttpClient(
            json = get(),
            authTokenManager = get(),
            sessionManager = get<SessionManager>(),
            crashReporter = get(),
            isDebug = config.isDebug,
            baseUrl = config.baseUrl,
            versionName = config.versionName,
            clientSecret = config.clientSecret,
        )
    }
}
