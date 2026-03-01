package se.kjellstrand.webshooter.data.competitionpatrols

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CompetitionPatrolsRepository @Inject constructor(
    private val remoteDataSource: CompetitionPatrolsRemoteDataSource
) {
    fun get(competitionId: Long): Flow<Resource<CompetitionPatrolsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            val result = try {
                remoteDataSource.getPatrols(competitionId)
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
