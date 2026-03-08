package se.kjellstrand.webshooter.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import se.kjellstrand.webshooter.data.settings.local.UserProfileDao
import se.kjellstrand.webshooter.data.settings.remote.SettingsRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SettingsModule {

    @Provides
    @Singleton
    fun providesSettingsRemoteDataSource(retrofit: Retrofit): SettingsRemoteDataSource {
        return retrofit.create(SettingsRemoteDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesSettingsRepository(
        remoteDataSource: SettingsRemoteDataSource,
        dao: UserProfileDao,
        gson: Gson
    ): SettingsRepository {
        return SettingsRepository(remoteDataSource, dao, gson)
    }
}
