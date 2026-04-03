package se.kjellstrand.webshooter.data.mysignups

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.mysignups.local.SignupsDao
import se.kjellstrand.webshooter.data.mysignups.local.toDomain
import se.kjellstrand.webshooter.data.mysignups.local.toEntity
import se.kjellstrand.webshooter.data.mysignups.remote.SignupGroup
import se.kjellstrand.webshooter.data.mysignups.remote.SignupsRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignupsRepository @Inject constructor(
    private val remoteDataSource: SignupsRemoteDataSource,
    private val dao: SignupsDao,
    private val gson: Gson
) {
    fun getSignups(): Flow<Resource<Map<String, SignupGroup>, UserError>> = flow {
        emit(Resource.Loading(true))

        var hasCached = false
        try {
            val cached = dao.getAll()
            hasCached = cached.isNotEmpty()
            if (hasCached) {
                val grouped = cached.groupBy { it.groupKey }
                    .mapValues { (_, entities) -> SignupGroup(signups = entities.map { it.toDomain(gson) }) }
                emit(Resource.Success(grouped))
            }
        } catch (e: Exception) {
            dao.deleteAll()
        }

        try {
            val result = remoteDataSource.getSignups()
            dao.deleteAll()
            result.groupedSignups.forEach { (key, group) ->
                dao.insertAll(group.signups.map { it.toEntity(key, gson) })
            }
            emit(Resource.Success(result.groupedSignups))
        } catch (e: IOException) {
            e.printStackTrace()
            if (!hasCached) emit(Resource.Error(UserError.IOError))
        } catch (e: HttpException) {
            e.printStackTrace()
            if (!hasCached) emit(Resource.Error(UserError.HttpError))
        } catch (e: Exception) {
            e.printStackTrace()
            if (!hasCached) emit(Resource.Error(UserError.UnknownError))
        }
        emit(Resource.Loading(false))
    }
}
