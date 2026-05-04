package se.kjellstrand.webshooter.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.BuildConfig
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.MockInterceptor
import se.kjellstrand.webshooter.data.SessionManager
import se.kjellstrand.webshooter.data.login.remote.LoginResponse
import se.kjellstrand.webshooter.data.login.remote.RefreshTokenRequest
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
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        json: Json,
        authTokenManager: AuthTokenManager,
        sessionManager: SessionManager,
        mockInterceptor: MockInterceptor
    ): HttpClient = HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = if (BuildConfig.DEBUG) LogLevel.HEADERS else LogLevel.NONE
        }

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val access = authTokenManager.readToken() ?: return@loadTokens null
                    BearerTokens(access, authTokenManager.readRefreshToken() ?: "")
                }
                refreshTokens {
                    val refresh = authTokenManager.readRefreshToken()
                    if (refresh.isNullOrEmpty()) {
                        authTokenManager.clearToken()
                        sessionManager.emitSessionExpired()
                        return@refreshTokens null
                    }
                    try {
                        val response = client.post("api/v4.1.9/oauth/token") {
                            attributes.put(Auth.AuthCircuitBreaker, Unit)
                            contentType(ContentType.Application.Json)
                            setBody(
                                RefreshTokenRequest(
                                    client_secret = BuildConfig.CLIENT_SECRET,
                                    refresh_token = refresh
                                )
                            )
                        }
                        if (response.status.isSuccess()) {
                            val body = json.decodeFromString(LoginResponse.serializer(), response.bodyAsText())
                            authTokenManager.storeTokens(
                                body.accessToken,
                                body.refreshToken,
                                body.expiresIn
                            )
                            BearerTokens(body.accessToken, body.refreshToken)
                        } else {
                            authTokenManager.clearToken()
                            sessionManager.emitSessionExpired()
                            null
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Token refresh failed", e)
                        authTokenManager.clearToken()
                        sessionManager.emitSessionExpired()
                        null
                    }
                }
            }
        }

        install(DefaultRequest) {
            url(BuildConfig.BASE_URL)
            header("Accept", "application/json, text/plain, */*")
            header("Accept-Language", "en,en-GB;q=0.9,sv-SE;q=0.8,sv;q=0.7")
            header("Referer", "https://webshooter.se/app/")
            header("User-Agent", "Webshooter-Android/${BuildConfig.VERSION_NAME}")
            header("X-Requested-With", "XMLHttpRequest")
        }

        HttpResponseValidator {
            // Default validator throws ClientRequestException / ServerResponseException
            // for 4xx/5xx when expectSuccess = true. No custom handling needed here.
        }

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
        AuthTokenManager(context)

    /**
     * Force the Auth plugin to drop its cached tokens so the next request reads the
     * latest values from [AuthTokenManager]. Call after a successful login or logout.
     */
    fun clearAuthTokenCache(httpClient: HttpClient) {
        httpClient.plugin(Auth).providers
            .filterIsInstance<BearerAuthProvider>()
            .forEach { it.clearToken() }
    }

    companion object {
        private const val TAG = "NetworkModule"
    }
}
