package se.kjellstrand.webshooter.data

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class CookieHeadersInterceptor @Inject constructor(
    private val cookieJar: AuthCookieJar
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val allCookies = cookieJar.getAllCookies()

        val requestBuilder = originalRequest.newBuilder()
        requestBuilder.header("Cookie", allCookies)

        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }
}
