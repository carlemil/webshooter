package se.kjellstrand.webshooter.data.competitionteams

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitionteams.local.TeamsDao
import se.kjellstrand.webshooter.data.competitionteams.local.toDomain
import se.kjellstrand.webshooter.data.competitionteams.local.toEntity
import se.kjellstrand.webshooter.data.competitionteams.remote.CompetitionTeamsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionteams.remote.CompetitionTeamsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CompetitionTeamsRepository @Inject constructor(
    private val remoteDataSource: CompetitionTeamsRemoteDataSource,
    private val dao: TeamsDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "TeamsRepository"
    }

    fun get(competitionId: Long): Flow<Resource<CompetitionTeamsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))

            var hasCached = false
            try {
                val cached = dao.getByCompetition(competitionId)
                hasCached = cached.isNotEmpty()
                if (hasCached) {
                    emit(Resource.Success(CompetitionTeamsResponse(teams = cached.map { it.toDomain(gson) })))
                }
            } catch (e: Exception) {
                dao.deleteByCompetition(competitionId)
            }

            val result = try {
                remoteDataSource.getTeams(competitionId)
            } catch (e: IOException) {
                Log.w(TAG, "Network error", e)
                if (!hasCached) emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: HttpException) {
                Log.w(TAG, "Network error", e)
                if (!hasCached) emit(Resource.Error(UserError.HttpError))
                return@flow
            } catch (e: Exception) {
                Log.w(TAG, "Network error", e)
                if (!hasCached) emit(Resource.Error(UserError.UnknownError))
                return@flow
            }

            dao.deleteByCompetition(competitionId)
            dao.insertAll(result.teams.map { it.toEntity(competitionId, gson) })

            emit(Resource.Success(result))
        }
    }
}
