package se.kjellstrand.webshooter.data.competitionsignups

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CompetitionSignupsRepository @Inject constructor(
    private val remoteDataSource: CompetitionSignupsRemoteDataSource
) {
    fun get(
        competitionId: Long,
        page: Int,
        perPage: Int
    ): Flow<Resource<CompetitionSignupsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            val result = try {
                remoteDataSource.getSignups(competitionId, page, perPage)
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
        }
    }
}
