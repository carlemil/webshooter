package se.kjellstrand.webshooter.data.login.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface LoginRemoteDataSource {
    suspend fun login(request: LoginRequest): LoginResponse
}

class LoginRemoteDataSourceKtor(
    private val httpClient: HttpClient
) : LoginRemoteDataSource {

    override suspend fun login(request: LoginRequest): LoginResponse {
        // Tag the request so the bearer Auth plugin doesn't attach a (possibly
        // stale) Authorization header to the OAuth token exchange.
        val response: LoginResponse = httpClient.post("api/v4.1.9/oauth/token") {
            attributes.put(Auth.AuthCircuitBreaker, Unit)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        // Force the Auth plugin to drop its in-memory token cache so the next
        // request reads the freshly-stored tokens from AuthTokenManager.
        httpClient.plugin(Auth).providers
            .filterIsInstance<BearerAuthProvider>()
            .forEach { it.clearToken() }
        return response
    }
}
