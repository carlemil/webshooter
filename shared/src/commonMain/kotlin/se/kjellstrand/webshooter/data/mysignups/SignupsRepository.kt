package se.kjellstrand.webshooter.data.mysignups

import io.github.aakira.napier.Napier
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.mysignups.local.SignupsDao
import se.kjellstrand.webshooter.data.mysignups.local.toDomain
import se.kjellstrand.webshooter.data.mysignups.local.toEntity
import se.kjellstrand.webshooter.data.mysignups.remote.SignupGroup
import se.kjellstrand.webshooter.data.mysignups.remote.SignupsRemoteDataSource




class SignupsRepository constructor(
    private val remoteDataSource: SignupsRemoteDataSource,
    private val dao: SignupsDao,
    private val json: Json
) {
    companion object {
        private const val TAG = "SignupsRepository"
    }

    fun getSignups(): Flow<Resource<Map<String, SignupGroup>, UserError>> = flow {
        emit(Resource.Loading(true))

        var hasCached = false
        try {
            val cached = dao.getAll()
            hasCached = cached.isNotEmpty()
            if (hasCached) {
                val grouped = cached.groupBy { it.groupKey }
                    .mapValues { (_, entities) -> SignupGroup(signups = entities.mapNotNull { it.toDomain(json) }) }
                emit(Resource.Success(grouped))
            }
        } catch (e: Exception) {
            dao.deleteAll()
        }

        try {
            val result = remoteDataSource.getSignups()
            dao.deleteAll()
            result.groupedSignups.forEach { (key, group) ->
                dao.insertAll(group.signups.map { it.toEntity(key, json) })
            }
            emit(Resource.Success(result.groupedSignups))
        } catch (e: ResponseException) {
            Napier.w("Error", e, TAG)
            if (!hasCached) emit(Resource.Error(UserError.HttpError(e.response.status.value)))
        } catch (e: SocketTimeoutException) {
            Napier.w("Error", e, TAG)
            if (!hasCached) emit(Resource.Error(UserError.IOError))
        } catch (e: ConnectTimeoutException) {
            Napier.w("Error", e, TAG)
            if (!hasCached) emit(Resource.Error(UserError.IOError))
        } catch (e: IOException) {
            Napier.w("Error", e, TAG)
            if (!hasCached) emit(Resource.Error(UserError.IOError))
        } catch (e: Exception) {
            Napier.w("Error", e, TAG)
            if (!hasCached) emit(Resource.Error(UserError.UnknownError))
        }
        emit(Resource.Loading(false))
    }
}
