package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.club.ClubRepository
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
    fun providesClubRepository(remoteDataSource: ClubRemoteDataSource): ClubRepository {
        return ClubRepository(remoteDataSource)
    }
}
