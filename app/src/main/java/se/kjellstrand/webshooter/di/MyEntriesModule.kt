package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.myentries.MyEntriesRepository
import se.kjellstrand.webshooter.data.myentries.remote.SignupsRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MyEntriesModule {

    @Provides
    @Singleton
    fun providesSignupsRemoteDataSource(retrofit: Retrofit): SignupsRemoteDataSource {
        return retrofit.create(SignupsRemoteDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesMyEntriesRepository(
        remoteDataSource: SignupsRemoteDataSource
    ): MyEntriesRepository {
        return MyEntriesRepository(remoteDataSource)
    }
}
