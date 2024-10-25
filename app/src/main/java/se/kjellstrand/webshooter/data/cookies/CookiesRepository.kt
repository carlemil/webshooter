package se.kjellstrand.webshooter.data.cookies

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import retrofit2.Response
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.cookies.remote.CookiesRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CookiesRepository @Inject constructor(
    private val cookiesRemoteDataSource: CookiesRemoteDataSource
) {
    fun getCookies(): Flow<Resource<Response<Unit>, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            val result = try {
                cookiesRemoteDataSource.getCookies()
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
