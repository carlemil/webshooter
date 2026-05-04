package se.kjellstrand.webshooter.data.results

import io.github.aakira.napier.Napier
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.results.local.toDomain
import se.kjellstrand.webshooter.data.results.local.toEntity
import se.kjellstrand.webshooter.data.results.remote.ResultsRemoteDataSource
import se.kjellstrand.webshooter.data.results.remote.ResultsResponse




open class ResultsRepository constructor(
    private val resultsRemoteDataSource: ResultsRemoteDataSource,
    private val dao: ResultsDao,
    private val json: Json
) {
    companion object {
        private const val TAG = "ResultsRepository"
    }

    fun get(
        competitionId: Long,
        skipRefreshIfCached: Boolean = false
    ): Flow<Resource<ResultsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))

            var hasCached = false
            try {
                val cached = dao.getByCompetition(competitionId)
                if (cached.isNotEmpty()) {
                    hasCached = true
                    emit(Resource.Success(ResultsResponse(results = cached.mapNotNull { it.toDomain(json) })))
                    if (skipRefreshIfCached) return@flow
                }
            } catch (e: Exception) {
                dao.deleteByCompetition(competitionId)
            }

            val result = try {
                resultsRemoteDataSource.getResults(competitionId)
            } catch (e: ResponseException) {
                Napier.w("Error", e, TAG)
                if (!hasCached) emit(Resource.Error(UserError.HttpError(e.response.status.value)))
                return@flow
            } catch (e: SocketTimeoutException) {
                Napier.w("Error", e, TAG)
                if (!hasCached) emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: ConnectTimeoutException) {
                Napier.w("Error", e, TAG)
                if (!hasCached) emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: IOException) {
                Napier.w("Error", e, TAG)
                if (!hasCached) emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: Exception) {
                Napier.w("Error", e, TAG)
                if (!hasCached) emit(Resource.Error(UserError.UnknownError))
                return@flow
            }

            dao.deleteByCompetition(competitionId)
            dao.insertAll(result.results.map { it.toEntity(competitionId, json) })

            emit(Resource.Success(result))
        }
    }

    open suspend fun refreshResultsFor(competitionId: Long) {
        try {
            val fresh = resultsRemoteDataSource.getResults(competitionId)
            dao.deleteByCompetition(competitionId)
            dao.insertAll(fresh.results.map { it.toEntity(competitionId, json) })
        } catch (e: Exception) {
            Napier.w("refreshResultsFor($competitionId) failed; invalidating cache", e, TAG)
            try {
                dao.deleteByCompetition(competitionId)
            } catch (_: Exception) {
            }
        }
    }

    fun getShooterResults(competitionId: Long, shooterId: Long): Flow<Resource<ResultsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))

            try {
                val cached = dao.getByCompetition(competitionId)
                if (cached.isNotEmpty()) {
                    val filtered = cached.mapNotNull { it.toDomain(json) }
                        .filter { it.signup.user.userID == shooterId }
                    emit(Resource.Success(ResultsResponse(results = filtered)))
                    return@flow
                }
            } catch (e: Exception) {
                dao.deleteByCompetition(competitionId)
            }

            val result = try {
                resultsRemoteDataSource.getResults(competitionId)
            } catch (e: ResponseException) {
                Napier.w("Error", e, TAG)
                emit(Resource.Error(UserError.HttpError(e.response.status.value)))
                return@flow
            } catch (e: SocketTimeoutException) {
                Napier.w("Error", e, TAG)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: ConnectTimeoutException) {
                Napier.w("Error", e, TAG)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: IOException) {
                Napier.w("Error", e, TAG)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: Exception) {
                Napier.w("Error", e, TAG)
                emit(Resource.Error(UserError.UnknownError))
                return@flow
            }

            dao.deleteByCompetition(competitionId)
            dao.insertAll(result.results.map { it.toEntity(competitionId, json) })

            val filtered = result.results.filter { it.signup.user.userID == shooterId }
            emit(Resource.Success(result.copy(results = filtered)))
        }
    }
}
