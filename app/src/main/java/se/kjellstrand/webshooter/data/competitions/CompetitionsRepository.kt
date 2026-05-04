package se.kjellstrand.webshooter.data.competitions

import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.competitions.local.CompetitionEntity
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.local.contentHash
import se.kjellstrand.webshooter.data.competitions.local.toDomain
import se.kjellstrand.webshooter.data.competitions.local.toEntity
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CompetitionsRepository @Inject constructor(
    private val competitionsRemoteDataSource: CompetitionsRemoteDataSource,
    private val dao: CompetitionsDao,
    private val json: Json,
    private val resultsRepository: ResultsRepository,
    private val resultsDao: ResultsDao
) {

    fun observeAll(): Flow<List<Datum>> =
        dao.observeAll()
            .map { entities -> entities.mapNotNull { it.toDomain(json) } }
            .flowOn(Dispatchers.Default)

    suspend fun syncAll() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val existing = dao.getAll().associateBy { it.id }
        val idsWithResults = resultsDao.getCompetitionIdsWithResults().toHashSet()
        val toRefreshIds = mutableSetOf<Long>()
        val freshEntities = mutableListOf<CompetitionEntity>()
        var page = 1
        val pageSize = 100

        while (true) {
            val result = try {
                competitionsRemoteDataSource.getCompetitions(page, pageSize, "all", 0, 0)
            } catch (e: Exception) {
                Napier.w("syncAll page $page failed", e, TAG)
                return
            }
            for (item in result.competitions.data) {
                val entity = item.toEntity(json)
                freshEntities += entity
                if (entity.date > today) continue
                val prev = existing[entity.id]
                val contentChanged = prev != null && prev.contentHash() != entity.contentHash()
                val missingResults = entity.id !in idsWithResults
                if (contentChanged || missingResults) {
                    toRefreshIds += entity.id
                }
            }
            if (page >= result.competitions.lastPage) break
            page++
        }

        if (freshEntities.isNotEmpty()) {
            dao.insertAll(freshEntities)
        }

        val apiIds = freshEntities.mapTo(HashSet()) { it.id }
        val deletedIds = existing.keys - apiIds
        if (deletedIds.isNotEmpty()) {
            deletedIds.forEach { resultsDao.deleteByCompetition(it) }
            dao.deleteByIds(deletedIds.toList())
        }

        if (toRefreshIds.isNotEmpty()) {
            coroutineScope {
                val sem = Semaphore(REFRESH_CONCURRENCY)
                toRefreshIds.map { id ->
                    async { sem.withPermit { resultsRepository.refreshResultsFor(id) } }
                }.awaitAll()
            }
        }
    }

    companion object {
        private const val TAG = "CompetitionsRepository"
        private const val REFRESH_CONCURRENCY = 4
    }
}
