package se.kjellstrand.webshooter.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.telemetry.CrashReporter

/**
 * iOS factory: builds the Webshooter [HttpClient] over the Darwin engine and
 * applies the cross-platform [configureWebshooterHttpClient] block. The iOS
 * app passes its own equivalents of the Android BuildConfig values.
 */
fun createWebshooterHttpClient(
    json: Json,
    authTokenManager: AuthTokenManager,
    sessionManager: SessionManager,
    crashReporter: CrashReporter,
    isDebug: Boolean,
    baseUrl: String,
    versionName: String,
    clientSecret: String,
): HttpClient = HttpClient(Darwin) {
    configureWebshooterHttpClient(
        json = json,
        authTokenManager = authTokenManager,
        sessionManager = sessionManager,
        crashReporter = crashReporter,
        isDebug = isDebug,
        baseUrl = baseUrl,
        userAgent = "Webshooter-iOS/$versionName",
        clientSecret = clientSecret,
    )
}.also { installIosMockInterceptor(it) }
