package se.kjellstrand.webshooter.data.competitions

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import se.kjellstrand.webshooter.data.competitions.local.CompetitionEntity
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.local.contentHash
import se.kjellstrand.webshooter.data.competitions.local.toDomain
import se.kjellstrand.webshooter.data.competitions.local.toEntity
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CompetitionsRepository @Inject constructor(
    private val competitionsRemoteDataSource: CompetitionsRemoteDataSource,
    private val dao: CompetitionsDao,
    private val gson: Gson,
    private val resultsRepository: ResultsRepository,
    private val resultsDao: ResultsDao
) {

    fun observeAll(): Flow<List<Datum>> =
        dao.observeAll()
            .map { entities -> entities.mapNotNull { it.toDomain(gson) } }
            .flowOn(Dispatchers.Default)

    suspend fun syncAll() {
        val today = LocalDate.now().toString()
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
                Log.w(TAG, "syncAll page $page failed", e)
                return
            }
            for (item in result.competitions.data) {
                val entity = item.toEntity(gson)
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
