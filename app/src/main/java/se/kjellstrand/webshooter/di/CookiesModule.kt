package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.cookies.CookiesRepository
import se.kjellstrand.webshooter.data.cookies.remote.CookiesRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CookiesModule {

    @Provides
    @Singleton
    fun providesCookiesRemoteDataSource(retrofit: Retrofit): CookiesRemoteDataSource {
        return retrofit.create(CookiesRemoteDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesCookiesRepository(
        cookiesRemoteDataSource: CookiesRemoteDataSource
    ): CookiesRepository {
        return CookiesRepository(cookiesRemoteDataSource)
    }
}