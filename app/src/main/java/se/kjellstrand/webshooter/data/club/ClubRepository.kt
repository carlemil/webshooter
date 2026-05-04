package se.kjellstrand.webshooter.data.club

import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.club.local.ClubDao
import se.kjellstrand.webshooter.data.club.local.sanitizeNullStrings
import se.kjellstrand.webshooter.data.club.local.toDomain
import se.kjellstrand.webshooter.data.club.local.toEntity
import se.kjellstrand.webshooter.data.club.remote.ClubInfoResponse
import se.kjellstrand.webshooter.data.club.remote.ClubRemoteDataSource
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubRepository @Inject constructor(
    private val remoteDataSource: ClubRemoteDataSource,
    private val dao: ClubDao,
    private val json: Json
) {
    fun getUserClub(): Flow<Resource<ClubInfoResponse, UserError>> = flow {
        emit(Resource.Loading(true))

        var cached = try {
            dao.get()
        } catch (e: Exception) {
            null
        }
        try {
            if (cached != null) {
                emit(Resource.Success(cached.toDomain(json)))
            }
        } catch (e: Exception) {
            cached = null
        }

        try {
            val response = remoteDataSource.getUserClub()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val sanitized = body.copy(club = body.club.sanitizeNullStrings())
                dao.insert(sanitized.club.toEntity(json))
                emit(Resource.Success(sanitized))
            } else {
                if (cached == null) emit(Resource.Error(UserError.HttpError(response.code())))
            }
        } catch (e: IOException) {
            if (cached == null) emit(Resource.Error(UserError.IOError))
        } catch (e: HttpException) {
            if (cached == null) emit(Resource.Error(UserError.HttpError(e.code())))
        } catch (e: Exception) {
            if (cached == null) emit(Resource.Error(UserError.UnknownError))
        }
        emit(Resource.Loading(false))
    }
}
