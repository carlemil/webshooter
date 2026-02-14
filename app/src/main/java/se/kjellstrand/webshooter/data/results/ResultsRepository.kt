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
    private var resultsCache = null as? ResultsResponse

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
            resultsCache = result
            emit(Resource.Success(result))
            emit(Resource.Loading(false))
        }
    }

    fun getShooterResults(shooterId: Int): Flow<Resource<ResultsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))

            val cachedResults = resultsCache
            if (cachedResults != null) {
                val filteredResults = cachedResults.results.filter { it.signup.user.userID == shooterId.toLong() }
                emit(Resource.Success(cachedResults.copy(results = filteredResults)))
                emit(Resource.Loading(false))
                return@flow
            }

            emit(Resource.Loading(false))
        }
    }
}