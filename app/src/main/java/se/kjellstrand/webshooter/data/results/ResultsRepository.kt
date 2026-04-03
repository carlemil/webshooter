package se.kjellstrand.webshooter.data.results

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.results.local.toDomain
import se.kjellstrand.webshooter.data.results.local.toEntity
import se.kjellstrand.webshooter.data.results.remote.ResultsRemoteDataSource
import se.kjellstrand.webshooter.data.results.remote.ResultsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class ResultsRepository @Inject constructor(
    private val resultsRemoteDataSource: ResultsRemoteDataSource,
    private val dao: ResultsDao,
    private val gson: Gson
) {
    fun get(competitionId: Int): Flow<Resource<ResultsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))

            var hasCached = false
            try {
                val cached = dao.getByCompetition(competitionId)
                hasCached = cached.isNotEmpty()
                if (hasCached) {
                    emit(Resource.Success(ResultsResponse(results = cached.map { it.toDomain(gson) })))
                }
            } catch (e: Exception) {
                dao.deleteByCompetition(competitionId)
            }

            val result = try {
                resultsRemoteDataSource.getResults(competitionId)
            } catch (e: IOException) {
                e.printStackTrace()
                if (!hasCached) emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                if (!hasCached) emit(Resource.Error(UserError.HttpError))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                if (!hasCached) emit(Resource.Error(UserError.UnknownError))
                return@flow
            }

            dao.deleteByCompetition(competitionId)
            dao.insertAll(result.results.map { it.toEntity(gson) })

            emit(Resource.Success(result))
        }
    }

    fun getPreferCached(competitionId: Int): Flow<Resource<ResultsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val cached = dao.getByCompetition(competitionId)
                if (cached.isNotEmpty()) {
                    emit(Resource.Success(ResultsResponse(results = cached.map { it.toDomain(gson) })))
                    return@flow
                }
            } catch (e: Exception) {
                dao.deleteByCompetition(competitionId)
            }

            val result = try {
                resultsRemoteDataSource.getResults(competitionId)
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.HttpError))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(UserError.UnknownError))
                return@flow
            }

            dao.deleteByCompetition(competitionId)
            dao.insertAll(result.results.map { it.toEntity(gson) })
            emit(Resource.Success(result))
        }
    }

    fun getShooterResults(competitionId: Int, shooterId: Int): Flow<Resource<ResultsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))

            var hasCached = false
            try {
                val cached = dao.getByCompetition(competitionId)
                if (cached.isNotEmpty()) {
                    hasCached = true
                    val filtered = cached.map { it.toDomain(gson) }
                        .filter { it.signup.user.userID == shooterId.toLong() }
                    emit(Resource.Success(ResultsResponse(results = filtered)))
                    return@flow
                }
            } catch (e: Exception) {
                dao.deleteByCompetition(competitionId)
            }

            val result = try {
                resultsRemoteDataSource.getResults(competitionId)
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.HttpError))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(UserError.UnknownError))
                return@flow
            }

            dao.deleteByCompetition(competitionId)
            dao.insertAll(result.results.map { it.toEntity(gson) })

            val filtered = result.results.filter { it.signup.user.userID == shooterId.toLong() }
            emit(Resource.Success(result.copy(results = filtered)))
        }
    }
}
