package se.kjellstrand.webshooter.data.common

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException

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
    } catch (e: IOException) {
        Log.w(tag, "Network error", e)
        if (cachedData == null) emit(Resource.Error(UserError.IOError))
        return@flow
    } catch (e: HttpException) {
        Log.w(tag, "Network error", e)
        if (cachedData == null) emit(Resource.Error(UserError.HttpError))
        return@flow
    } catch (e: Exception) {
        Log.w(tag, "Network error", e)
        if (cachedData == null) emit(Resource.Error(UserError.UnknownError))
        return@flow
    }

    saveToCache(result)
    emit(Resource.Success(result))
}
