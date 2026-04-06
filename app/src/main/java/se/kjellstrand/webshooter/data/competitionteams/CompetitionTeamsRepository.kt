package se.kjellstrand.webshooter.data.competitionteams

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.common.cachedResourceFlow
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
        private const val TAG = "CompetitionTeamsRepository"
    }

    fun get(competitionId: Long): Flow<Resource<CompetitionTeamsResponse, UserError>> {
        return cachedResourceFlow(
            tag = TAG,
            fetchFromCache = {
                val cached = dao.getByCompetition(competitionId)
                if (cached.isNotEmpty()) CompetitionTeamsResponse(teams = cached.map { it.toDomain(gson) }) else null
            },
            deleteCache = { dao.deleteByCompetition(competitionId) },
            fetchFromRemote = { remoteDataSource.getTeams(competitionId) },
            saveToCache = { result ->
                dao.deleteByCompetition(competitionId)
                dao.insertAll(result.teams.map { it.toEntity(competitionId, gson) })
            }
        )
    }
}
