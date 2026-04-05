package se.kjellstrand.webshooter.di

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.kjellstrand.webshooter.BuildConfig
import se.kjellstrand.webshooter.data.AuthCookieJar
import se.kjellstrand.webshooter.data.AuthInterceptor
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.CookieHeadersInterceptor
import se.kjellstrand.webshooter.data.GeneralHeadersInterceptor
import se.kjellstrand.webshooter.data.MockInterceptor
import se.kjellstrand.webshooter.data.SessionManager
import se.kjellstrand.webshooter.data.TokenAuthenticator
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        generalHeadersInterceptor: GeneralHeadersInterceptor,
        cookieHeadersInterceptor: CookieHeadersInterceptor,
        mockInterceptor: MockInterceptor,
        cookieJar: AuthCookieJar,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
            .followRedirects(followRedirects = true)
            .followSslRedirects(followProtocolRedirects = true)

        okHttpClient.cookieJar(cookieJar)
        okHttpClient.addInterceptor(generalHeadersInterceptor)
        okHttpClient.addInterceptor(authInterceptor)
        okHttpClient.addInterceptor(cookieHeadersInterceptor)
        okHttpClient.addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        })
        okHttpClient.addInterceptor(mockInterceptor)
        okHttpClient.authenticator(tokenAuthenticator)

        return okHttpClient.build()
    }

    @Provides
    @Singleton
    fun providesRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
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

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        authTokenManager: AuthTokenManager,
        loginRemoteDataSource: dagger.Lazy<LoginRemoteDataSource>,
        sessionManager: SessionManager
    ): TokenAuthenticator {
        return TokenAuthenticator(authTokenManager, loginRemoteDataSource, sessionManager)
    }
}
