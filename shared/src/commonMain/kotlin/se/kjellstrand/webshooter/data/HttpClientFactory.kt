package se.kjellstrand.webshooter.data

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.login.remote.LoginResponse
import se.kjellstrand.webshooter.data.login.remote.RefreshTokenRequest

private const val LOG_TAG = "WebshooterHTTP"

/**
 * Apply the cross-platform Webshooter HTTP client configuration to an
 * [HttpClientConfig]. Called by both the Android and iOS factories so the
 * two platforms stay byte-identical except for engine and a couple of
 * deployment-only constants (base URL, user agent, client secret).
 *
 * The platform-specific logger sink is Napier — which routes to logcat on
 * Android and to the iOS logging system on Darwin.
 */
fun HttpClientConfig<*>.configureWebshooterHttpClient(
    json: Json,
    authTokenManager: AuthTokenManager,
    sessionManager: SessionManager,
    isDebug: Boolean,
    baseUrl: String,
    userAgent: String,
    clientSecret: String,
) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(json)
    }

    install(Logging) {
        level = if (isDebug) LogLevel.ALL else LogLevel.NONE
        logger = object : Logger {
            override fun log(message: String) {
                Napier.d(message, tag = LOG_TAG)
            }
        }
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
            // Never proactively attach the bearer token to the OAuth token
            // endpoint. A stale access token from a previous session would
            // otherwise be sent alongside a password-grant or refresh-token
            // request, which the backend rejects with 500.
            sendWithoutRequest { request ->
                val segments = request.url.encodedPathSegments
                val n = segments.size
                !(n >= 2 && segments[n - 2] == "oauth" && segments[n - 1] == "token")
            }
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
                                client_secret = clientSecret,
                                refresh_token = refresh
                            )
                        )
                    }
                    if (response.status.isSuccess()) {
                        val body = json.decodeFromString(
                            LoginResponse.serializer(),
                            response.bodyAsText()
                        )
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
                    Napier.w("Token refresh failed", e, tag = LOG_TAG)
                    authTokenManager.clearToken()
                    sessionManager.emitSessionExpired()
                    null
                }
            }
        }
    }

    install(DefaultRequest) {
        url(baseUrl)
        header("Accept", "application/json, text/plain, */*")
        header("Accept-Language", "en,en-GB;q=0.9,sv-SE;q=0.8,sv;q=0.7")
        header("Referer", "https://webshooter.se/app/")
        header("User-Agent", userAgent)
        header("X-Requested-With", "XMLHttpRequest")
    }

    HttpResponseValidator {
        // Default validator throws ClientRequestException / ServerResponseException
        // for 4xx/5xx when expectSuccess = true. No custom handling needed here.
    }
}
