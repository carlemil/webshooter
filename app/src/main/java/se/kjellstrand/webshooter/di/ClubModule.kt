package se.kjellstrand.webshooter.di

import kotlinx.serialization.json.Json
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.club.ClubRepository
import se.kjellstrand.webshooter.data.club.local.ClubDao
import se.kjellstrand.webshooter.data.club.remote.ClubRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ClubModule {

    @Provides
    @Singleton
    fun providesClubRemoteDataSource(retrofit: Retrofit): ClubRemoteDataSource {
        return retrofit.create(ClubRemoteDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesClubRepository(
        remoteDataSource: ClubRemoteDataSource,
        dao: ClubDao,
        json: Json
    ): ClubRepository {
        return ClubRepository(remoteDataSource, dao, json)
    }
}
