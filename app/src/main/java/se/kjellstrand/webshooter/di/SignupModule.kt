package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import se.kjellstrand.webshooter.data.signup.SignupRepository
import se.kjellstrand.webshooter.data.signup.remote.SignupRemoteDataSource
import se.kjellstrand.webshooter.data.signup.remote.SignupRemoteDataSourceKtor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SignupModule {

    @Provides
    @Singleton
    fun providesSignupRemoteDataSource(httpClient: HttpClient): SignupRemoteDataSource {
        return SignupRemoteDataSourceKtor(httpClient)
    }

    @Provides
    @Singleton
    fun providesSignupRepository(remoteDataSource: SignupRemoteDataSource): SignupRepository {
        return SignupRepository(remoteDataSource)
    }
}
