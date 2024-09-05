package se.kjellstrand.webshooter.di

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
import javax.inject.Singleton
import se.kjellstrand.data.BuildConfig
import se.kjellstrand.webshooter.data.ShooterDatabase
import se.kjellstrand.webshooter.data.user.remote.UserRemoteDataSource

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
    fun providesUserRemoteDataSource() : UserRemoteDataSource {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.BASE_URL)
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