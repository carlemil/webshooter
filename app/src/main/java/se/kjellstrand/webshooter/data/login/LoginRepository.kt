package se.kjellstrand.webshooter.data.login

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import retrofit2.Response
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSource
import se.kjellstrand.webshooter.data.login.remote.LoginRequest
import se.kjellstrand.webshooter.data.login.remote.LoginResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class LoginRepository @Inject constructor(
    private val loginRemoteDataSource: LoginRemoteDataSource,
    private val authTokenManager: AuthTokenManager
) {

    fun getCookies(): Flow<Resource<Response<Unit>, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            val result = try {
                loginRemoteDataSource.getCookies()
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.HttpError))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(UserError.UnknownError))
                return@flow
            }
            emit(Resource.Success(result))
            emit(Resource.Loading(false))
        }
    }

    fun login(
        email: String,
        username: String,
        password: String
    ): Flow<Resource<Response<LoginResponse>, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            val result = try {
                loginRemoteDataSource.login(
                    LoginRequest(
                        1,
                        "52FphTYzOrmuqH30ltL7LrBzhSEURIJiMFNp6Qt0",
                        email,
                        "password",
                        password,
                        username
                    )
                )
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.HttpError))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(UserError.UnknownError))
                return@flow
            }
            result.body()?.let { authTokenManager.storeToken(it.accessToken) }
            emit(Resource.Success(result))
            emit(Resource.Loading(false))
        }
    }
}
