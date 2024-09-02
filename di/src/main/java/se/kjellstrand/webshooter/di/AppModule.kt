package se.kjellstrand.webshooter.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.kjellstrand.webshooter.data.local.ShooterDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//
//    private val client: OkHttpClient = OkHttpClient.Builder()
//        .addInterceptor(interceptor)
//        .build()
//
//    @Provides
//    @Singleton
//    fun providesMovieApi() : MovieApi {
//        return Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create())
//            .baseUrl(MovieApi.BASE_URL)
//            .client(client)
//            .build()
//            .create(MovieApi::class.java)
//    }

    @Provides
    @Singleton
    fun providesMovieDatabase(app: Application): ShooterDatabase {
        return Room.databaseBuilder(
            app,
            ShooterDatabase::class.java,
            "moviedb.db"
        ).build()
    }

}