package se.kjellstrand.webshooter.data.competitions

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.local.SyncPreferences
import se.kjellstrand.webshooter.data.competitions.local.toDomain
import se.kjellstrand.webshooter.data.competitions.local.toEntity
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CompetitionsRepository @Inject constructor(
    private val competitionsRemoteDataSource: CompetitionsRemoteDataSource,
    private val dao: CompetitionsDao,
    private val gson: Gson,
    private val syncPreferences: SyncPreferences
) {

    fun observeAll(): Flow<List<Datum>> =
        dao.observeAll()
            .map { entities -> entities.mapNotNull { it.toDomain(gson) } }
            .flowOn(Dispatchers.Default)

    suspend fun syncAll() {
        val now = System.currentTimeMillis()
        val lastFullSync = syncPreferences.getLastCompletedFullSyncMs()
        val needsFullCompleted = dao.getCompletedCount() == 0 || (now - lastFullSync) > WEEK_MS
        if (needsFullCompleted) {
            syncCompleted()
            syncPreferences.setLastCompletedFullSyncMs(now)
        }
        syncNonCompleted()
    }

    suspend fun syncNonCompleted() {
        val localNonCompletedIds = dao.getNonCompletedIds().toSet()

        val serverNonCompletedIds = mutableSetOf<Long>()
        val pageSize = 100
        var page = 1
        while (true) {
            val result = try {
                competitionsRemoteDataSource.getCompetitions(page, pageSize, "all", 0, 0)
            } catch (e: Exception) {
                Log.w(TAG, "syncNonCompleted page $page failed", e)
                return
            }
            val items = result.competitions.data
            if (items.isNotEmpty()) {
                dao.insertAll(items.map { it.toEntity(gson) })
            }
            items.forEach { if (it.status != "completed") serverNonCompletedIds.add(it.id) }
            if (page >= result.competitions.lastPage) break
            page++
        }

        val transitioned = localNonCompletedIds - serverNonCompletedIds
        for (id in transitioned) {
            try {
                val response = competitionsRemoteDataSource.getCompetitionById(id)
                dao.insertAll(listOf(response.competition.toEntity(gson)))
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    dao.deleteById(id)
                } else {
                    Log.w(TAG, "syncNonCompleted refetch of $id failed (${e.code()})", e)
                }
            } catch (e: Exception) {
                Log.w(TAG, "syncNonCompleted refetch of $id failed", e)
            }
        }
    }

    suspend fun syncCompleted(force: Boolean = false) {
        val cursor = if (force) null else dao.getMaxCompletedDate()
        var page = 1
        val pageSize = 100
        while (true) {
            val result = competitionsRemoteDataSource.getCompetitions(page, pageSize, "completed", 0, 0)
            val items = result.competitions.data
            val entities = items.map { it.toEntity(gson) }
            dao.insertAll(entities)
            val minDateOnPage = items.minOfOrNull { it.date }
            if (cursor != null && minDateOnPage != null && minDateOnPage <= cursor) break
            if (page >= result.competitions.lastPage) break
            page++
        }
    }

    companion object {
        private const val TAG = "CompetitionsRepository"
    }
}

private const val WEEK_MS = 7L * 24 * 60 * 60 * 1000
