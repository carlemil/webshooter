package se.kjellstrand.webshooter.data.competitionteams

import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.common.cachedResourceFlow
import se.kjellstrand.webshooter.data.competitionteams.local.TeamsDao
import se.kjellstrand.webshooter.data.competitionteams.local.toDomain
import se.kjellstrand.webshooter.data.competitionteams.local.toEntity
import se.kjellstrand.webshooter.data.competitionteams.remote.CompetitionTeamsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionteams.remote.CompetitionTeamsResponse




open class CompetitionTeamsRepository constructor(
    private val remoteDataSource: CompetitionTeamsRemoteDataSource,
    private val dao: TeamsDao,
    private val json: Json
) {
    companion object {
        private const val TAG = "CompetitionTeamsRepository"
    }

    fun get(competitionId: Long): Flow<Resource<CompetitionTeamsResponse, UserError>> {
        return cachedResourceFlow(
            tag = TAG,
            fetchFromCache = {
                val cached = dao.getByCompetition(competitionId)
                if (cached.isNotEmpty()) CompetitionTeamsResponse(teams = cached.map { it.toDomain(json) }) else null
            },
            deleteCache = { dao.deleteByCompetition(competitionId) },
            fetchFromRemote = { remoteDataSource.getTeams(competitionId) },
            saveToCache = { result ->
                dao.deleteByCompetition(competitionId)
                dao.insertAll(result.teams.map { it.toEntity(competitionId, json) })
            }
        )
    }
}
