package se.kjellstrand.webshooter.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.kjellstrand.webshooter.BuildConfig
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.MockInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    private val cookieJar = object : CookieJar {
        private val cookieStore: MutableMap<String, Cookie> = mutableMapOf()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            println("saveFromResponse: $cookies")
            for (cookie in cookies) {
                val key = "${cookie.domain}${cookie.path}${cookie.name}"
                cookieStore[key] = cookie
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            println("loadForRequest: $cookieStore")
            return cookieStore.values.filter { cookie ->
                url.host.endsWith(cookie.domain) && url.encodedPath.startsWith(cookie.path)
            }
        }

        fun getAllCookies(): String {
            val cookies = cookieStore.values.map { cookie ->
                "${cookie.name}=${cookie.value}"
            }.joinToString(separator = "; ")
            return cookies
        }
    }

    private val httpLoggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val cookieHeaderInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        val allCookies = cookieJar.getAllCookies()

        val requestBuilder = originalRequest.newBuilder()
        requestBuilder.header("Cookie", allCookies)

        val newRequest = requestBuilder.build()
        chain.proceed(newRequest)
    }

    private val headersInterceptor = Interceptor { chain ->
        chain.proceed(
                chain.request().newBuilder()
                        .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header(
                        "Accept-Language",
                        "en,en-GB;q=0.9,sv-SE;q=0.8,sv;q=0.7,ru-KZ;q=0.6,ru;q=0.5,en-US;q=0.4"
                    )
                    .header("Connection", "keep-alive")
                    .header("DNT", "1")
                    .header("Host", "webshooter.se")
                    .header("Referer", "https://webshooter.se/app/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header(
                        "User-agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"
                    )
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header(
                        "sec-ch-ua",
                        "\"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\""
                    )
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "Windows")
            .build()
        )
    }

    private val authInterceptor = Interceptor { chain ->
        val token = AuthTokenManager.token
        chain.proceed(
            if (token == null) {
                chain.request().newBuilder()
            } else {
                chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
            }.build()
        )
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        mockInterceptor: MockInterceptor
    ): OkHttpClient {
        val currentFlavor = BuildConfig.FLAVOR
        val okHttpClient = OkHttpClient.Builder()
            .followRedirects(followRedirects = true)
            .followSslRedirects(followProtocolRedirects = true)

        okHttpClient.cookieJar(cookieJar)
        okHttpClient.addInterceptor(cookieHeaderInterceptor)
        okHttpClient.addInterceptor(authInterceptor)
        okHttpClient.addInterceptor(headersInterceptor)
        okHttpClient.addInterceptor(httpLoggingInterceptor)
        if (currentFlavor == "mock") {
            okHttpClient.addNetworkInterceptor(mockInterceptor)
        }

        return okHttpClient.build()
    }

    @Provides
    @Singleton
    fun providesRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideMockInterceptor(
        @ApplicationContext context: Context
    ): MockInterceptor {
        return MockInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideAuthTokenManager(@ApplicationContext context: Context): AuthTokenManager {
        return AuthTokenManager(context)
    }
}
