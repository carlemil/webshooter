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
    fun get(): Flow<Resource<ResultsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            val auth = "Bearer eyJ0eXA"
            val result = try {
                resultsRemoteDataSource.getResults(auth, 196)
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
            emit(Resource.Success(result))

            emit(Resource.Loading(false))
        }
    }
}
