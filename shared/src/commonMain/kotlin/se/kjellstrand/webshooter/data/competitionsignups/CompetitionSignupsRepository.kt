package se.kjellstrand.webshooter.data.competitionsignups

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
import se.kjellstrand.webshooter.data.competitionsignups.local.CompetitionSignupsDao
import se.kjellstrand.webshooter.data.competitionsignups.local.toDomain
import se.kjellstrand.webshooter.data.competitionsignups.local.toEntity
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsResponse
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsPaged




open class CompetitionSignupsRepository constructor(
    private val remoteDataSource: CompetitionSignupsRemoteDataSource,
    private val dao: CompetitionSignupsDao,
    private val json: Json
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
                        val domains = cached.map { it.toDomain(json) }
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

            if (page == 1) dao.deleteByCompetition(competitionId)
            dao.insertAll(result.signups.data.map { it.toEntity(competitionId, json) })

            emit(Resource.Success(result))
        }
    }
}
