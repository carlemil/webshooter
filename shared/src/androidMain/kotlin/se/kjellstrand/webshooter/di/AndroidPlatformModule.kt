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
}
