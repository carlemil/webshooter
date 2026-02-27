package se.kjellstrand.webshooter.data.signups

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.signups.remote.SignupGroup
import se.kjellstrand.webshooter.data.signups.remote.SignupsRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignupsRepository @Inject constructor(
    private val remoteDataSource: SignupsRemoteDataSource
) {
    fun getSignups(): Flow<Resource<Map<String, SignupGroup>, UserError>> = flow {
        emit(Resource.Loading(true))
        try {
            val result = remoteDataSource.getSignups()
            emit(Resource.Success(result.groupedSignups))
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
