package se.kjellstrand.webshooter.data

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = AuthTokenManager.token
        return chain.proceed(
            if (token == null) {
                chain.request().newBuilder()
            } else {
                chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
            }.build()
        )
    }
}
