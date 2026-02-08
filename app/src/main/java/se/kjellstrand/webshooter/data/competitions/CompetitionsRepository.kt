package se.kjellstrand.webshooter.data.competitions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.CompetitionStatus
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CompetitionsRepository @Inject constructor(
    private val competitionsRemoteDataSource: CompetitionsRemoteDataSource
) {
    fun get(
        page: Int,
        pageSize: Int,
        status: CompetitionStatus
    ): Flow<Resource<CompetitionsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            val result = try {
                competitionsRemoteDataSource.getCompetitions(
                    page,
                    pageSize,
                    status.status,
                    0
                )
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.IOError))
                emit(Resource.Loading(false))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.HttpError))
                emit(Resource.Loading(false))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(UserError.UnknownError))
                emit(Resource.Loading(false))
                return@flow
            }
            emit(Resource.Success(result))

            emit(Resource.Loading(false))
        }
    }
}
