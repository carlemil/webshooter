package se.kjellstrand.webshooter.data.competitionpatrols

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitionpatrols.local.PatrolsDao
import se.kjellstrand.webshooter.data.competitionpatrols.local.toDomain
import se.kjellstrand.webshooter.data.competitionpatrols.local.toEntity
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CompetitionPatrolsRepository @Inject constructor(
    private val remoteDataSource: CompetitionPatrolsRemoteDataSource,
    private val dao: PatrolsDao,
    private val gson: Gson
) {
    fun get(competitionId: Long): Flow<Resource<CompetitionPatrolsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))

            var hasCached = false
            try {
                val cached = dao.getByCompetition(competitionId)
                hasCached = cached.isNotEmpty()
                if (hasCached) {
                    emit(Resource.Success(CompetitionPatrolsResponse(patrols = cached.map { it.toDomain(gson) })))
                }
            } catch (e: Exception) {
                dao.deleteByCompetition(competitionId)
            }

            val result = try {
                remoteDataSource.getPatrols(competitionId)
            } catch (e: IOException) {
                e.printStackTrace()
                if (!hasCached) emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                if (!hasCached) emit(Resource.Error(UserError.HttpError))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                if (!hasCached) emit(Resource.Error(UserError.UnknownError))
                return@flow
            }

            dao.deleteByCompetition(competitionId)
            dao.insertAll(result.patrols.map { it.toEntity(competitionId, gson) })

            emit(Resource.Success(result))
        }
    }
}
