package se.kjellstrand.webshooter.data.login

import io.github.aakira.napier.Napier
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSource
import se.kjellstrand.webshooter.data.login.remote.LoginRequest
import se.kjellstrand.webshooter.data.login.remote.LoginResponse

open class LoginRepository(
    private val loginRemoteDataSource: LoginRemoteDataSource,
    private val clientSecret: String
) {
    companion object {
        private const val TAG = "LoginRepository"
    }

    fun login(
        email: String,
        username: String,
        password: String
    ): Flow<Resource<LoginResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            val result = try {
                loginRemoteDataSource.login(
                    LoginRequest(
                        client_secret = clientSecret,
                        email = email,
                        password = password,
                        username = username
                    )
                )
            } catch (e: ResponseException) {
                Napier.w("Error", e, TAG)
                emit(Resource.Error(UserError.HttpError(e.response.status.value, e.response.call.request.url.encodedPath)))
                return@flow
            } catch (e: SocketTimeoutException) {
                Napier.w("Error", e, TAG)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: ConnectTimeoutException) {
                Napier.w("Error", e, TAG)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: IOException) {
                Napier.w("Error", e, TAG)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: Exception) {
                Napier.w("Error", e, TAG)
                emit(Resource.Error(UserError.UnknownError))
                return@flow
            }
            emit(Resource.Success(result))
            emit(Resource.Loading(false))
        }
    }
}
