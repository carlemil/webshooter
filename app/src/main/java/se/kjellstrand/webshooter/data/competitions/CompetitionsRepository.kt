package se.kjellstrand.webshooter.data.competitions

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
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
    private val gson: Gson
) {

    fun observeAll(): Flow<List<Datum>> =
        dao.observeAll()
            .map { entities -> entities.mapNotNull { it.toDomain(gson) } }
            .flowOn(Dispatchers.Default)

    suspend fun syncAll() {
        var page = 1
        val pageSize = 100
        while (true) {
            val result = try {
                competitionsRemoteDataSource.getCompetitions(page, pageSize, "all", 0, 0)
            } catch (e: Exception) {
                Log.w(TAG, "syncAll page $page failed", e)
                return
            }
            val items = result.competitions.data
            if (items.isNotEmpty()) {
                dao.insertAll(items.map { it.toEntity(gson) })
            }
            if (page >= result.competitions.lastPage) break
            page++
        }
    }

    companion object {
        private const val TAG = "CompetitionsRepository"
    }
}
