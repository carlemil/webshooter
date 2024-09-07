package se.kjellstrand.webshooter.data.login

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.Resource
import se.kjellstrand.webshooter.data.UserError
import se.kjellstrand.webshooter.data.login.local.LoginEntity
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSource
import se.kjellstrand.webshooter.data.login.remote.LoginRequest
import se.kjellstrand.webshooter.data.mappers.toLoginEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class LoginRepository @Inject constructor(
    private val loginRemoteDataSource: LoginRemoteDataSource
) {
    fun login(
        email: String,
        username: String,
        password: String
    ): Flow<Resource<LoginEntity, UserError>> {
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
            emit(Resource.Success(result.toLoginEntity()))

            emit(Resource.Loading(false))
        }
    }
}
