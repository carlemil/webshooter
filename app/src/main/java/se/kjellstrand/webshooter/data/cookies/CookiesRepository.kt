package se.kjellstrand.webshooter.data.cookies

import android.util.Log
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.cookies.remote.CookiesRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CookiesRepository @Inject constructor(
    private val cookiesRemoteDataSource: CookiesRemoteDataSource
) {
    companion object {
        private const val TAG = "CookiesRepository"
    }

    fun getCookies(): Flow<Resource<Unit, UserError>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                cookiesRemoteDataSource.getCookies()
                emit(Resource.Success(Unit))
            } catch (e: ResponseException) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.HttpError(e.response.status.value)))
                return@flow
            } catch (e: SocketTimeoutException) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: ConnectTimeoutException) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: IOException) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: Exception) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.UnknownError))
                return@flow
            }
            emit(Resource.Loading(false))
        }
    }
}
