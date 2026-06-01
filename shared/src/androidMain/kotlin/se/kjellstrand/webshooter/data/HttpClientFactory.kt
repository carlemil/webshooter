@file:JvmName("AndroidHttpClientFactory")

package se.kjellstrand.webshooter.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import se.kjellstrand.webshooter.data.telemetry.CrashReporter

/**
 * Android factory: builds the Webshooter [HttpClient] over the OkHttp engine
 * and applies the cross-platform [configureWebshooterHttpClient] block.
 * [extraOkHttpInterceptors] lets the app inject Android-resource-bound
 * interceptors (e.g. MockInterceptor) without keeping their construction
 * here in the shared module.
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
    extraOkHttpInterceptors: List<Interceptor> = emptyList(),
): HttpClient = HttpClient(OkHttp) {
    configureWebshooterHttpClient(
        json = json,
        authTokenManager = authTokenManager,
        sessionManager = sessionManager,
        crashReporter = crashReporter,
        isDebug = isDebug,
        baseUrl = baseUrl,
        userAgent = "Webshooter-Android/$versionName",
        clientSecret = clientSecret,
    )
    engine {
        extraOkHttpInterceptors.forEach { addInterceptor(it) }
    }
}
