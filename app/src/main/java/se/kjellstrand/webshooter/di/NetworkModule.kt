package se.kjellstrand.webshooter.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.kjellstrand.webshooter.BuildConfig
import se.kjellstrand.webshooter.data.AuthCookieJar
import se.kjellstrand.webshooter.data.AuthInterceptor
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.CookieHeadersInterceptor
import se.kjellstrand.webshooter.data.GeneralHeadersInterceptor
import se.kjellstrand.webshooter.data.MockInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        generalHeadersInterceptor: GeneralHeadersInterceptor,
        cookieHeadersInterceptor: CookieHeadersInterceptor,
        mockInterceptor: MockInterceptor,
        cookieJar: AuthCookieJar,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val currentFlavor = BuildConfig.FLAVOR
        val okHttpClient = OkHttpClient.Builder()
            .followRedirects(followRedirects = true)
            .followSslRedirects(followProtocolRedirects = true)

        okHttpClient.cookieJar(cookieJar)
        okHttpClient.addInterceptor(generalHeadersInterceptor)
        okHttpClient.addInterceptor(authInterceptor)
        okHttpClient.addInterceptor(cookieHeadersInterceptor)
        okHttpClient.addInterceptor(HttpLoggingInterceptor().apply { level = BODY })

        if (currentFlavor == "mock") {
            okHttpClient.addInterceptor(mockInterceptor)
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
    fun provideAuthCookieJar(): AuthCookieJar {
        return AuthCookieJar()
    }

    @Singleton
    @Provides
    fun provideHeadersInterceptor(
        cookieJar: AuthCookieJar
    ): CookieHeadersInterceptor {
        return CookieHeadersInterceptor(cookieJar)
    }

    @Singleton
    @Provides
    fun provideAuthInterceptor(): AuthInterceptor {
        return AuthInterceptor()
    }

    @Singleton
    @Provides
    fun provideCookieHeaderInterceptor(): GeneralHeadersInterceptor {
        return GeneralHeadersInterceptor()
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
