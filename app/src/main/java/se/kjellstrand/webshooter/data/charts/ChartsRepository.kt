package se.kjellstrand.webshooter.data.charts

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.local.toDomain
import se.kjellstrand.webshooter.data.competitions.local.toEntity
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.ResultsResponse
import javax.inject.Inject
import javax.inject.Singleton

data class ChartDataPoint(
    val competitionId: Long,
    val competitionName: String,
    val date: String,
    val averageSerieScore: Double,
    val weaponClass: String,
    val resultsType: String
)

data class Participant(
    val userId: Long,
    val fullname: String
)

data class ChartData(
    val dataPoints: List<ChartDataPoint>,
    val allWeaponClasses: List<String> = emptyList(),
    val allParticipants: List<Participant> = emptyList(),
    val allCompetitionMeta: Map<Long, CompetitionMeta> = emptyMap()
)

data class CompetitionMeta(
    val name: String,
    val date: String,
    val resultsType: String
)

@Singleton
class ChartsRepository @Inject constructor(
    private val competitionsRemoteDataSource: CompetitionsRemoteDataSource,
    private val resultsRepository: ResultsRepository,
    private val competitionsDao: CompetitionsDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "ChartsRepository"
    }

    fun getChartData(userId: Long): Flow<Resource<ChartData, UserError>> = flow {
        emit(Resource.Loading(true))

        try {
            syncNewCompletedCompetitions()
        } catch (e: Exception) {
            Log.w(TAG, "Network sync failed, using local data only", e)
        }

        val competitions = competitionsDao.getCompletedCompetitions()
            .mapNotNull { it.toDomain(gson) }

        val allCompetitionMeta = mutableMapOf<Long, CompetitionMeta>()
        val competitionsWithType = competitions.mapNotNull { competition ->
            val resultsType = try {
                competition.resultsType.toApiString()
            } catch (e: Exception) {
                Log.w(TAG, "Unknown resultsType for competition ${competition.id}, skipping")
                return@mapNotNull null
            }
            allCompetitionMeta[competition.id] = CompetitionMeta(
                name = competition.name,
                date = competition.date,
                resultsType = resultsType
            )
            competition to resultsType
        }

        val dataPoints = mutableListOf<ChartDataPoint>()
        val allWeaponClasses = sortedSetOf<String>()
        val allParticipants = mutableMapOf<Long, String>()

        for ((competition, resultsType) in competitionsWithType) {
            val resultsResult = lastNonLoading(
                resultsRepository.getPreferCached(competition.id)
            )
            if (resultsResult !is Resource.Success) continue

            resultsResult.data.results.forEach { result ->
                allWeaponClasses.add(result.weaponClass.classname)
                val user = result.signup.user
                allParticipants.putIfAbsent(user.userID, user.fullname)
            }
            resultsResult.data.results
                .filter { it.signup.user.userID == userId }
                .forEach { result ->
                    val avg = computeAverageScore(result, resultsType)
                    dataPoints.add(
                        ChartDataPoint(
                            competitionId = competition.id,
                            competitionName = competition.name,
                            date = competition.date,
                            averageSerieScore = avg,
                            weaponClass = result.weaponClass.classname,
                            resultsType = resultsType
                        )
                    )
                }
        }

        emit(
            Resource.Success(
                ChartData(
                    dataPoints = dataPoints.sortedBy { it.date },
                    allWeaponClasses = allWeaponClasses.toList(),
                    allParticipants = allParticipants.map { (id, name) ->
                        Participant(userId = id, fullname = name)
                    }.sortedBy { it.fullname },
                    allCompetitionMeta = allCompetitionMeta.toMap()
                )
            )
        )
        emit(Resource.Loading(false))
    }

    private suspend fun syncNewCompletedCompetitions() {
        val existingIds = competitionsDao.getCompletedCompetitions()
            .map { it.id }
            .toSet()

        var page = 1
        var keepFetching = true

        while (keepFetching) {
            val response = competitionsRemoteDataSource.getCompetitions(
                page = page,
                perPage = 10,
                status = "completed",
                type = 0,
                userSignup = 0
            )
            val pageData = response.competitions.data
            if (pageData.isEmpty()) break

            val newCompetitions = mutableListOf<se.kjellstrand.webshooter.data.competitions.local.CompetitionEntity>()
            for (competition in pageData) {
                if (competition.id in existingIds) {
                    keepFetching = false
                    break
                }
                newCompetitions.add(competition.toEntity(gson))
            }

            if (newCompetitions.isNotEmpty()) {
                competitionsDao.insertAll(newCompetitions)
            }

            if (!keepFetching || pageData.size < 10 || page >= response.competitions.lastPage) {
                break
            }
            page++
        }
    }

    fun getShooterChartData(
        shooterIds: List<Long>,
        competitionIds: List<Long>,
        competitionMetadata: Map<Long, CompetitionMeta>
    ): Flow<Resource<Map<Long, ChartData>, UserError>> = flow {
        emit(Resource.Loading(true))

        val result = mutableMapOf<Long, MutableList<ChartDataPoint>>()
        shooterIds.forEach { result[it] = mutableListOf() }

        for (competitionId in competitionIds) {
            val meta = competitionMetadata[competitionId]
            val resultsResult = lastNonLoading(resultsRepository.getPreferCached(competitionId))

            if (resultsResult is Resource.Success) {
                for (shooterId in shooterIds) {
                    val shooterResults = resultsResult.data.results.filter {
                        it.signup.user.userID == shooterId
                    }
                    for (r in shooterResults) {
                        val resultsType = meta?.resultsType ?: ""
                        val avg = computeAverageScore(r, resultsType)
                        result[shooterId]?.add(
                            ChartDataPoint(
                                competitionId = competitionId,
                                competitionName = meta?.name ?: "",
                                date = meta?.date ?: "",
                                averageSerieScore = avg,
                                weaponClass = r.weaponClass.classname,
                                resultsType = resultsType
                            )
                        )
                    }
                }
            }
        }

        emit(
            Resource.Success(
                result.mapValues { (_, points) ->
                    ChartData(dataPoints = points.sortedBy { it.date })
                }
            )
        )
        emit(Resource.Loading(false))
    }

    private fun computeAverageScore(result: Result, resultsType: String): Double {
        val stations = result.results
        if (stations.isEmpty()) return 0.0
        return if (resultsType == "field" || resultsType == "pointfield") {
            stations.map { it.hits }.average()
        } else {
            stations.map { it.points }.average()
        }
    }

    private suspend fun <T, E : se.kjellstrand.webshooter.data.common.Error> lastNonLoading(
        flow: Flow<Resource<T, E>>
    ): Resource<T, E>? {
        var last: Resource<T, E>? = null
        flow.collect { resource ->
            if (resource !is Resource.Loading) {
                last = resource
            }
        }
        return last
    }

    private fun ResultsType.toApiString(): String = apiString
}
