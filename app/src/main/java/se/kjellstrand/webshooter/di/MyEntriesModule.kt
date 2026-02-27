package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.myentries.MyEntriesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MyEntriesModule {

    @Provides
    @Singleton
    fun providesMyEntriesRepository(
        remoteDataSource: CompetitionsRemoteDataSource
    ): MyEntriesRepository {
        return MyEntriesRepository(remoteDataSource)
    }
}
