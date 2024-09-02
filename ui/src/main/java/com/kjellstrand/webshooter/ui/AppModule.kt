package com.kjellstrand.webshooter.ui

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.kjellstrand.webshooter.data.local.ShooterDatabase
import se.kjellstrand.webshooter.data.remote.UserRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    @Provides
    @Singleton
    fun providesUserApi() : UserRemoteDataSource {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(UserRemoteDataSource.BASE_URL)
            .client(client)
            .build()
            .create(UserRemoteDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesShooterDatabase(app: Application): ShooterDatabase {
        return Room.databaseBuilder(
            app,
            ShooterDatabase::class.java,
            "shooterdb.db"
        ).build()
    }

}