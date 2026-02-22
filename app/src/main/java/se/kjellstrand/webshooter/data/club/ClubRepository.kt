package se.kjellstrand.webshooter.data.club

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.club.remote.ClubInfoResponse
import se.kjellstrand.webshooter.data.club.remote.ClubRemoteDataSource
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubRepository @Inject constructor(
    private val remoteDataSource: ClubRemoteDataSource
) {
    fun getUserClub(): Flow<Resource<ClubInfoResponse, UserError>> = flow {
        emit(Resource.Loading(true))
        try {
            val response = remoteDataSource.getUserClub()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(Resource.Success(body))
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
