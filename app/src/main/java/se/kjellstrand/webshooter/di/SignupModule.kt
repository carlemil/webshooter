package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.signup.SignupRepository
import se.kjellstrand.webshooter.data.signup.remote.SignupRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SignupModule {

    @Provides
    @Singleton
    fun providesSignupRemoteDataSource(retrofit: Retrofit): SignupRemoteDataSource {
        return retrofit.create(SignupRemoteDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesSignupRepository(remoteDataSource: SignupRemoteDataSource): SignupRepository {
        return SignupRepository(remoteDataSource)
    }
}
