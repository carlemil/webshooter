package se.kjellstrand.webshooter.data.myentries

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyEntriesRepository @Inject constructor(
    private val remoteDataSource: CompetitionsRemoteDataSource
) {
    fun getPage(page: Int, pageSize: Int): Flow<Resource<CompetitionsResponse, UserError>> = flow {
        emit(Resource.Loading(true))
        try {
            val result = remoteDataSource.getCompetitions(page, pageSize, "all", 0, 1)
            emit(Resource.Success(result))
        } catch (e: IOException) {
            e.printStackTrace()
            emit(Resource.Error(UserError.IOError))
        } catch (e: HttpException) {
            e.printStackTrace()
            emit(Resource.Error(UserError.HttpError))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(UserError.UnknownError))
        }
        emit(Resource.Loading(false))
    }
}
