package se.kjellstrand.webshooter.data.signup

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.signup.remote.SignupRemoteDataSource




class SignupRepository constructor(
    private val remoteDataSource: SignupRemoteDataSource
) {
    fun signup(
        competitionId: Long,
        weaponClassId: Long,
        userId: Long,
        note: String
    ): Flow<Resource<Unit, UserError>> = flow {
        emit(Resource.Loading(true))
        try {
            val fields = buildMap<String, String> {
                put("competitions_id", competitionId.toString())
                put("weaponclasses_id", weaponClassId.toString())
                put("users_id", userId.toString())
                if (note.isNotBlank()) put("note", note)
            }
            remoteDataSource.signup(fields)
            emit(Resource.Success(Unit))
        } catch (e: ResponseException) {
            emit(Resource.Error(UserError.HttpError(e.response.status.value)))
        } catch (e: SocketTimeoutException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: ConnectTimeoutException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: IOException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: Exception) {
            emit(Resource.Error(UserError.UnknownError))
        }
        emit(Resource.Loading(false))
    }

    fun removeSignup(signupId: Long): Flow<Resource<Unit, UserError>> = flow {
        emit(Resource.Loading(true))
        try {
            remoteDataSource.removeSignup(signupId)
            emit(Resource.Success(Unit))
        } catch (e: ResponseException) {
            emit(Resource.Error(UserError.HttpError(e.response.status.value)))
        } catch (e: SocketTimeoutException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: ConnectTimeoutException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: IOException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: Exception) {
            emit(Resource.Error(UserError.UnknownError))
        }
        emit(Resource.Loading(false))
    }
}
