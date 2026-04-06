package se.kjellstrand.webshooter.data.competitions

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.local.toDomain
import se.kjellstrand.webshooter.data.competitions.local.toEntity
import se.kjellstrand.webshooter.data.competitions.remote.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsResponse
import se.kjellstrand.webshooter.data.competitions.remote.Link
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CompetitionsRepository @Inject constructor(
    private val competitionsRemoteDataSource: CompetitionsRemoteDataSource,
    private val dao: CompetitionsDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "CompetitionsRepository"
    }

    fun get(
        page: Int,
        pageSize: Int
    ): Flow<Resource<CompetitionsResponse, UserError>> {
        return flow<Resource<CompetitionsResponse, UserError>> {
            emit(Resource.Loading(true))

            if (page == 1) {
                try {
                    val cached = dao.getAll()
                    if (cached.isNotEmpty()) {
                        val domains = cached.mapNotNull { entity ->
                            entity.toDomain(gson)
                        }
                        if (domains.isNotEmpty()) {
                            emit(Resource.Success(CompetitionsResponse(
                                competitions = Competitions(
                                    currentPage = 1,
                                    data = domains,
                                    from = 1,
                                    lastPage = 1,
                                    links = emptyList<Link>(),
                                    path = "",
                                    perPage = domains.size.toLong(),
                                    to = domains.size.toLong(),
                                    total = Long.MAX_VALUE,
                                    status = "",
                                    type = 0,
                                    competitionTypes = emptyList()
                                )
                            )))
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error", e)
                }
            }

            val result = try {
                competitionsRemoteDataSource.getCompetitions(page, pageSize, "all", 0, 0)
            } catch (e: IOException) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: HttpException) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.HttpError(e.code())))
                return@flow
            } catch (e: Exception) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.UnknownError))
                return@flow
            }

            val entities = result.competitions.data.map { it.toEntity(gson) }
            if (page == 1) {
                dao.replaceAll(entities)
            } else {
                dao.insertAll(entities)
            }

            emit(Resource.Success(result))
        }.flowOn(Dispatchers.Default)
    }
}
