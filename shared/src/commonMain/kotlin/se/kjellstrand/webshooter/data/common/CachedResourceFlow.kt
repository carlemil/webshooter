package se.kjellstrand.webshooter.data.common

import io.github.aakira.napier.Napier
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException

fun <T> cachedResourceFlow(
    tag: String,
    fetchFromCache: suspend () -> T?,
    deleteCache: suspend () -> Unit,
    fetchFromRemote: suspend () -> T,
    saveToCache: suspend (T) -> Unit
): Flow<Resource<T, UserError>> = flow {
    emit(Resource.Loading(true))

    var cachedData: T? = null
    try {
        cachedData = fetchFromCache()
        if (cachedData != null) {
            emit(Resource.Success(cachedData))
        }
    } catch (e: Exception) {
        deleteCache()
    }

    val result = try {
        fetchFromRemote()
    } catch (e: ResponseException) {
        Napier.w("Network error", e, tag)
        if (cachedData == null) emit(Resource.Error(UserError.HttpError(e.response.status.value, e.response.call.request.url.encodedPath)))
        return@flow
    } catch (e: SocketTimeoutException) {
        Napier.w("Network error", e, tag)
        if (cachedData == null) emit(Resource.Error(UserError.IOError))
        return@flow
    } catch (e: ConnectTimeoutException) {
        Napier.w("Network error", e, tag)
        if (cachedData == null) emit(Resource.Error(UserError.IOError))
        return@flow
    } catch (e: IOException) {
        Napier.w("Network error", e, tag)
        if (cachedData == null) emit(Resource.Error(UserError.IOError))
        return@flow
    } catch (e: Exception) {
        Napier.w("Network error", e, tag)
        if (cachedData == null) emit(Resource.Error(UserError.UnknownError))
        return@flow
    }

    saveToCache(result)
    emit(Resource.Success(result))
}
