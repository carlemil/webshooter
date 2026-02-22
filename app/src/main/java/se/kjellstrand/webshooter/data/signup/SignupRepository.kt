package se.kjellstrand.webshooter.data.signup

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.signup.remote.SignupData
import se.kjellstrand.webshooter.data.signup.remote.SignupRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignupRepository @Inject constructor(
    private val remoteDataSource: SignupRemoteDataSource
) {
    fun signup(
        competitionId: Long,
        weaponClassId: Long,
        userId: Long,
        note: String
    ): Flow<Resource<SignupData, UserError>> = flow {
        emit(Resource.Loading(true))
        try {
            val fields = buildMap<String, String> {
                put("competitions_id", competitionId.toString())
                put("weaponclasses_id", weaponClassId.toString())
                put("users_id", userId.toString())
                if (note.isNotBlank()) put("note", note)
            }
            val response = remoteDataSource.signup(fields)
            val data = response.body()?.signup
            if (response.isSuccessful && data != null) {
                emit(Resource.Success(data))
            } else {
                emit(Resource.Error(UserError.HttpError))
            }
        } catch (e: IOException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: HttpException) {
            emit(Resource.Error(UserError.HttpError))
        } catch (e: Exception) {
            emit(Resource.Error(UserError.UnknownError))
        }
        emit(Resource.Loading(false))
    }
}
