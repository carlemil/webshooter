package se.kjellstrand.webshooter.data.competitionpatrols

import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.common.cachedResourceFlow
import se.kjellstrand.webshooter.data.competitionpatrols.local.PatrolsDao
import se.kjellstrand.webshooter.data.competitionpatrols.local.toDomain
import se.kjellstrand.webshooter.data.competitionpatrols.local.toEntity
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsResponse




open class CompetitionPatrolsRepository constructor(
    private val remoteDataSource: CompetitionPatrolsRemoteDataSource,
    private val dao: PatrolsDao,
    private val json: Json
) {
    companion object {
        private const val TAG = "CompetitionPatrolsRepository"
    }

    fun get(competitionId: Long): Flow<Resource<CompetitionPatrolsResponse, UserError>> {
        return cachedResourceFlow(
            tag = TAG,
            fetchFromCache = {
                val cached = dao.getByCompetition(competitionId)
                if (cached.isNotEmpty()) CompetitionPatrolsResponse(patrols = cached.map { it.toDomain(json) }) else null
            },
            deleteCache = { dao.deleteByCompetition(competitionId) },
            fetchFromRemote = { remoteDataSource.getPatrols(competitionId) },
            saveToCache = { result ->
                dao.deleteByCompetition(competitionId)
                dao.insertAll(result.patrols.map { it.toEntity(competitionId, json) })
            }
        )
    }
}
