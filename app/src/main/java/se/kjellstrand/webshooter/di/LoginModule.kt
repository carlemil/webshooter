package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import se.kjellstrand.webshooter.data.login.LoginRepository
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSource
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSourceKtor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LoginModule {

    @Provides
    @Singleton
    fun providesLoginRemoteDataSource(httpClient: HttpClient): LoginRemoteDataSource {
        return LoginRemoteDataSourceKtor(httpClient)
    }

    @Provides
    @Singleton
    fun providesLoginRepository(
        loginRemoteDataSource: LoginRemoteDataSource
    ): LoginRepository {
        return LoginRepository(loginRemoteDataSource)
    }
}
