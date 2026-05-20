package se.kjellstrand.webshooter.di

import android.content.Context
import io.ktor.client.HttpClient
import okhttp3.Interceptor
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
import se.kjellstrand.webshooter.data.vision.HoleDetector
import se.kjellstrand.webshooter.ui.platform.AndroidCalendarOpener
import se.kjellstrand.webshooter.ui.platform.AndroidUrlLauncher
import se.kjellstrand.webshooter.ui.platform.CalendarOpener
import se.kjellstrand.webshooter.ui.platform.UrlLauncher

/**
 * Android Koin module — supplies host-bound singletons (Context-scoped
 * factories, OkHttp interceptors, deployment config). Pair with
 * [sharedModule] via [initKoin] from `ShooterApplication.onCreate`.
 *
 * [extraOkHttpInterceptors] is the seam that lets `MockInterceptor`
 * (which depends on Android `R.raw.*` resources) stay in `:app` and still
 * be wired into the HttpClient that's now built here in `:shared`.
 */
fun androidPlatformModule(
    context: Context,
    config: WebshooterConfig,
    extraOkHttpInterceptors: List<Interceptor> = emptyList(),
): Module = module {
    single { config }
    single { createAppDatabase(context) }
    single<AuthTokenManager> { createAuthTokenManager(context) }
    single<SecurePrefs> { createSecurePrefs(context) }
    single<UrlLauncher> { AndroidUrlLauncher(context) }
    single<CalendarOpener> { AndroidCalendarOpener(context) }
    single<HttpClient> {
        createWebshooterHttpClient(
            json = get(),
            authTokenManager = get(),
            sessionManager = get<SessionManager>(),
            isDebug = config.isDebug,
            baseUrl = config.baseUrl,
            versionName = config.versionName,
            clientSecret = config.clientSecret,
            extraOkHttpInterceptors = extraOkHttpInterceptors,
        )
    }
    // best.onnx ships as an Android asset (see :app/src/main/assets/best.onnx).
    // Constructed lazily on first navigation to the Markera screen.
    single<HoleDetector> {
        val bytes = context.assets.open(HOLE_DETECTOR_MODEL_ASSET).readBytes()
        HoleDetector(bytes, inputSize = HOLE_DETECTOR_INPUT_SIZE)
    }
}

private const val HOLE_DETECTOR_MODEL_ASSET = "best.onnx"
private const val HOLE_DETECTOR_INPUT_SIZE = 640
