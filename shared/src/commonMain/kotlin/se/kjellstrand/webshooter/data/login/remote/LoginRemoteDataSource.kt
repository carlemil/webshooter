package se.kjellstrand.webshooter.data.login.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.pluginOrNull
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
        val response: LoginResponse = httpClient.post("api/v4.1.9/oauth/token") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        // Force the Auth plugin to drop its in-memory token cache so the next
        // request reads the freshly-stored tokens from AuthTokenManager.
        // pluginOrNull keeps test clients (which skip Auth) from blowing up.
        httpClient.pluginOrNull(Auth)?.providers
            ?.filterIsInstance<BearerAuthProvider>()
            ?.forEach { it.clearToken() }
        return response
    }
}
