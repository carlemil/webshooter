package se.kjellstrand.webshooter.data.results

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.results.remote.ResultsRemoteDataSource
import se.kjellstrand.webshooter.data.results.remote.ResultsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class ResultsRepository @Inject constructor(
    private val resultsRemoteDataSource: ResultsRemoteDataSource
) {
    private val resultsCache = mutableMapOf<Int, ResultsResponse>()

    fun get(competitionId: Int): Flow<Resource<ResultsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            val auth = "Bearer eyJ0eXA"
            val result = try {
                resultsRemoteDataSource.getResults(auth, competitionId)
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
            resultsCache[competitionId] = result
            emit(Resource.Success(result))
        }
    }

    fun getShooterResults(competitionId: Int, shooterId: Int): Flow<Resource<ResultsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))

            val cachedResults = resultsCache[competitionId]
            if (cachedResults != null) {
                val filteredResults = cachedResults.results.filter { it.signup.user.userID == shooterId.toLong() }
                emit(Resource.Success(cachedResults.copy(results = filteredResults)))
                return@flow
            }

            val auth = "Bearer eyJ0eXA"
            val result = try {
                resultsRemoteDataSource.getResults(auth, competitionId)
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

            resultsCache[competitionId] = result

            val filteredResults = result.results.filter { it.signup.user.userID == shooterId.toLong() }
            emit(Resource.Success(result.copy(results = filteredResults)))
        }
    }
}