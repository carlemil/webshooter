package se.kjellstrand.webshooter.data.charts

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.local.toDomain
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.local.ResultsDao
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
    private val resultsRepository: ResultsRepository,
    private val competitionsDao: CompetitionsDao,
    private val resultsDao: ResultsDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "ChartsRepository"
    }

    fun getChartData(userId: Long): Flow<Resource<ChartData, UserError>> = flow {
        emit(Resource.Loading(true))

        val rows = resultsDao.getChartPointsForUser(userId)
        val dataPoints = rows
            .map { row ->
                ChartDataPoint(
                    competitionId = row.competitionId,
                    competitionName = row.competitionName,
                    date = row.date,
                    averageSerieScore = row.averageScore,
                    weaponClass = row.weaponClassName,
                    resultsType = row.resultsType
                )
            }
            .sortedBy { it.date }

        val allWeaponClasses = resultsDao.getAllWeaponClasses()
        val allParticipants = resultsDao.getAllParticipants().map {
            Participant(userId = it.userId, fullname = it.fullname)
        }

        val allCompetitionMeta = mutableMapOf<Long, CompetitionMeta>()
        for (competition in competitionsDao.getCompletedCompetitions().mapNotNull { it.toDomain(gson) }) {
            val apiString = try {
                competition.resultsType.toApiString()
            } catch (e: Exception) {
                Log.w(TAG, "Unknown resultsType for competition ${competition.id}, skipping")
                continue
            }
            allCompetitionMeta[competition.id] = CompetitionMeta(
                name = competition.name,
                date = competition.date,
                resultsType = apiString
            )
        }

        emit(
            Resource.Success(
                ChartData(
                    dataPoints = dataPoints,
                    allWeaponClasses = allWeaponClasses,
                    allParticipants = allParticipants,
                    allCompetitionMeta = allCompetitionMeta.toMap()
                )
            )
        )
        emit(Resource.Loading(false))
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
                @Suppress("UNNECESSARY_SAFE_CALL")
                val validResults = resultsResult.data.results.filter {
                    it.signup?.user != null
                }
                for (shooterId in shooterIds) {
                    val shooterResults = validResults.filter {
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
