package se.kjellstrand.webshooter.data.cookies

import io.github.aakira.napier.Napier
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.cookies.remote.CookiesRemoteDataSource




open class CookiesRepository constructor(
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
                Napier.w("Error", e, TAG)
                emit(Resource.Error(UserError.HttpError(e.response.status.value, e.response.call.request.url.encodedPath)))
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
            emit(Resource.Loading(false))
        }
    }
}
