package se.kjellstrand.webshooter.data

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSource
import se.kjellstrand.webshooter.data.login.remote.RefreshTokenRequest
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TokenAuthenticator(
    private val authTokenManager: AuthTokenManager,
    private val loginRemoteDataSource: dagger.Lazy<LoginRemoteDataSource>,
    private val sessionManager: SessionManager
) : Authenticator {

    private val lock = ReentrantLock()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry loops — give up after 1 attempt
        if (responseCount(response) > 1) {
            return null
        }

        val staleToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        lock.withLock {
            val currentToken = AuthTokenManager.token

            // Another thread already refreshed the token — retry with the new one
            if (currentToken != null && currentToken != staleToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = authTokenManager.readRefreshToken()
            if (refreshToken == null) {
                authTokenManager.clearToken()
                sessionManager.emitSessionExpired()
                return null
            }

            return try {
                val refreshResponse = loginRemoteDataSource.get()
                    .refreshToken(RefreshTokenRequest(refresh_token = refreshToken))
                    .execute()

                val body = refreshResponse.body()
                if (refreshResponse.isSuccessful && body != null) {
                    authTokenManager.storeTokens(
                        body.accessToken,
                        body.refreshToken,
                        body.expiresIn
                    )
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${body.accessToken}")
                        .build()
                } else {
                    authTokenManager.clearToken()
                    sessionManager.emitSessionExpired()
                    null
                }
            } catch (e: Exception) {
                authTokenManager.clearToken()
                sessionManager.emitSessionExpired()
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
