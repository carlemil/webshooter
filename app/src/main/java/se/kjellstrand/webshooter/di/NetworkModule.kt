package se.kjellstrand.webshooter.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.BuildConfig
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.MockInterceptor
import se.kjellstrand.webshooter.data.SessionManager
import se.kjellstrand.webshooter.data.configureWebshooterHttpClient
import se.kjellstrand.webshooter.data.createAuthTokenManager
import se.kjellstrand.webshooter.data.secure.SecurePrefs
import se.kjellstrand.webshooter.data.secure.createSecurePrefs
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        // Match the old Gson behavior: emit fields even when they equal their
        // declared default. The OAuth endpoint requires client_id and
        // grant_type, both of which use defaults in LoginRequest.
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        json: Json,
        authTokenManager: AuthTokenManager,
        sessionManager: SessionManager,
        mockInterceptor: MockInterceptor
    ): HttpClient = HttpClient(OkHttp) {
        configureWebshooterHttpClient(
            json = json,
            authTokenManager = authTokenManager,
            sessionManager = sessionManager,
            isDebug = BuildConfig.DEBUG,
            baseUrl = BuildConfig.BASE_URL,
            userAgent = "Webshooter-Android/${BuildConfig.VERSION_NAME}",
            clientSecret = BuildConfig.CLIENT_SECRET,
        )
        engine {
            addInterceptor(mockInterceptor)
        }
    }

    @Provides
    @Singleton
    fun provideMockInterceptor(
        @ApplicationContext context: Context
    ): MockInterceptor = MockInterceptor(context)

    @Provides
    @Singleton
    fun provideAuthTokenManager(@ApplicationContext context: Context): AuthTokenManager =
        createAuthTokenManager(context)

    @Provides
    @Singleton
    fun provideSecurePrefs(@ApplicationContext context: Context): SecurePrefs =
        createSecurePrefs(context)

    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager = SessionManager()

    /**
     * Force the Auth plugin to drop its cached tokens so the next request reads the
     * latest values from [AuthTokenManager]. Call after a successful login or logout.
     */
    fun clearAuthTokenCache(httpClient: HttpClient) {
        httpClient.plugin(Auth).providers
            .filterIsInstance<BearerAuthProvider>()
            .forEach { it.clearToken() }
    }
}
