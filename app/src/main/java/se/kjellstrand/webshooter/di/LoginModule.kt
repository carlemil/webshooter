package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.login.LoginRepository
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LoginModule {

    @Provides
    @Singleton
    fun providesLoginRemoteDataSource(retrofit: Retrofit): LoginRemoteDataSource {
        return retrofit.create(LoginRemoteDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesLoginRepository(
        loginRemoteDataSource: LoginRemoteDataSource,
        authTokenManager: AuthTokenManager
    ): LoginRepository {
        return LoginRepository(loginRemoteDataSource, authTokenManager)
    }
}