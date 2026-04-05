package se.kjellstrand.webshooter.data.competitionsignups

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitionsignups.local.CompetitionSignupsDao
import se.kjellstrand.webshooter.data.competitionsignups.local.toDomain
import se.kjellstrand.webshooter.data.competitionsignups.local.toEntity
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsResponse
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsPaged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CompetitionSignupsRepository @Inject constructor(
    private val remoteDataSource: CompetitionSignupsRemoteDataSource,
    private val dao: CompetitionSignupsDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "CompetitionSignupsRepository"
    }

    fun get(
        competitionId: Long,
        page: Int,
        perPage: Int
    ): Flow<Resource<CompetitionSignupsResponse, UserError>> {
        return flow {
            emit(Resource.Loading(true))

            if (page == 1) {
                try {
                    val cached = dao.getByCompetition(competitionId)
                    if (cached.isNotEmpty()) {
                        val domains = cached.map { it.toDomain(gson) }
                        emit(Resource.Success(CompetitionSignupsResponse(
                            signups = CompetitionSignupsPaged(
                                currentPage = 1,
                                data = domains,
                                lastPage = 1,
                                total = domains.size
                            )
                        )))
                    }
                } catch (e: Exception) {
                    dao.deleteByCompetition(competitionId)
                }
            }

            val result = try {
                remoteDataSource.getSignups(competitionId, page, perPage)
            } catch (e: IOException) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: HttpException) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.HttpError))
                return@flow
            } catch (e: Exception) {
                Log.w(TAG, "Error", e)
                emit(Resource.Error(UserError.UnknownError))
                return@flow
            }

            if (page == 1) dao.deleteByCompetition(competitionId)
            dao.insertAll(result.signups.data.map { it.toEntity(competitionId, gson) })

            emit(Resource.Success(result))
        }
    }
}
