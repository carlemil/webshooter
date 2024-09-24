package se.kjellstrand.webshooter.data.competitions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.Resource
import se.kjellstrand.webshooter.data.UserError
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CompetitionsRepository @Inject constructor(
    private val competitionsRemoteDataSource: CompetitionsRemoteDataSource
) {
    fun get(): Flow<Resource<CompetitionsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            val auth = "Bearer eyJ0eXA"
            val result = try {
                competitionsRemoteDataSource.getCompetitions(auth,1,10,"open",0)
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
