package se.kjellstrand.webshooter.data.login

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import retrofit2.Response
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.BuildConfig
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSource
import se.kjellstrand.webshooter.data.login.remote.LoginRequest
import se.kjellstrand.webshooter.data.login.remote.LoginResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class LoginRepository @Inject constructor(
    private val loginRemoteDataSource: LoginRemoteDataSource
) {
    companion object {
        private const val TAG = "LoginRepository"
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
                        client_secret = BuildConfig.CLIENT_SECRET,
                        email = email,
                        password = password,
                        username = username
                    )
                )
            } catch (e: IOException) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: HttpException) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.HttpError(e.code())))
                return@flow
            } catch (e: Exception) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.UnknownError))
                return@flow
            }
            emit(Resource.Success(result))
            emit(Resource.Loading(false))
        }
    }
}
