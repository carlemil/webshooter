package se.kjellstrand.webshooter.di

import android.content.Context
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    private val httpLoggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        mockInterceptor: MockInterceptor // Injected from Hilt
    ): OkHttpClient {
        val currentFlavor = BuildConfig.FLAVOR

        val builder = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
        if (currentFlavor == "mock") {
            builder.addInterceptor(mockInterceptor).build()
        }
        return builder.build()
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
}