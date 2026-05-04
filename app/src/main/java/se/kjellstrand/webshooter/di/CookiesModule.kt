package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import se.kjellstrand.webshooter.data.cookies.CookiesRepository
import se.kjellstrand.webshooter.data.cookies.remote.CookiesRemoteDataSource
import se.kjellstrand.webshooter.data.cookies.remote.CookiesRemoteDataSourceKtor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CookiesModule {

    @Provides
    @Singleton
    fun providesCookiesRemoteDataSource(httpClient: HttpClient): CookiesRemoteDataSource {
        return CookiesRemoteDataSourceKtor(httpClient)
    }

    @Provides
    @Singleton
    fun providesCookiesRepository(
        cookiesRemoteDataSource: CookiesRemoteDataSource
    ): CookiesRepository {
        return CookiesRepository(cookiesRemoteDataSource)
    }
}
