package se.kjellstrand.webshooter.data.charts

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.lastOrNull
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.mysignups.SignupsRepository
import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.ResultsResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

data class ChartData(
    val dataPoints: List<ChartDataPoint>
)

@Singleton
class ChartsRepository @Inject constructor(
    private val signupsRepository: SignupsRepository,
    private val resultsRepository: ResultsRepository
) {
    companion object {
        private const val TAG = "ChartsRepository"

        fun computeAverageStationScore(stationPoints: List<Long>): Double {
            if (stationPoints.isEmpty()) return 0.0
            return stationPoints.average()
        }
    }

    fun getChartData(userId: Long): Flow<Resource<ChartData, UserError>> = flow {
        emit(Resource.Loading(true))

        val signupsResult = lastNonLoading(signupsRepository.getSignups())

        if (signupsResult == null || signupsResult is Resource.Error) {
            emit(Resource.Error((signupsResult as? Resource.Error)?.error ?: UserError.UnknownError))
            emit(Resource.Loading(false))
            return@flow
        }

        val signupGroups = (signupsResult as Resource.Success).data
        val allSignups = signupGroups.values.flatMap { it.signups }

        val dataPoints = mutableListOf<ChartDataPoint>()

        for (signup in allSignups) {
            val competitionId = signup.competitionsId
            val isToday = isCompetitionToday(signup.competition.date)

            val resultsFlow = if (isToday) {
                resultsRepository.get(competitionId)
            } else {
                resultsRepository.getPreferCached(competitionId)
            }

            val resultsResult = lastNonLoading(resultsFlow)

            if (resultsResult is Resource.Success) {
                val shooterResults = resultsResult.data.results.filter {
                    it.signup.user.userID == userId
                }
                dataPoints.addAll(extractDataPoints(shooterResults, signup))
            }
        }

        emit(Resource.Success(ChartData(dataPoints = dataPoints.sortedBy { it.date })))
        emit(Resource.Loading(false))
    }

    fun getShooterChartData(
        shooterIds: List<Long>,
        competitionIds: List<Long>
    ): Flow<Resource<Map<Long, ChartData>, UserError>> = flow {
        emit(Resource.Loading(true))

        val result = mutableMapOf<Long, MutableList<ChartDataPoint>>()
        shooterIds.forEach { result[it] = mutableListOf() }

        for (competitionId in competitionIds) {
            val resultsResult = lastNonLoading(resultsRepository.getPreferCached(competitionId))

            if (resultsResult is Resource.Success) {
                for (shooterId in shooterIds) {
                    val shooterResults = resultsResult.data.results.filter {
                        it.signup.user.userID == shooterId
                    }
                    for (r in shooterResults) {
                        val avgScore = computeAverageStationScore(
                            r.results.map { it.points }
                        )
                        result[shooterId]?.add(
                            ChartDataPoint(
                                competitionId = r.competitionsID,
                                competitionName = "",
                                date = "",
                                averageSerieScore = avgScore,
                                weaponClass = r.weaponClass.classname,
                                resultsType = ""
                            )
                        )
                    }
                }
            }
        }

        emit(Resource.Success(
            result.mapValues { (_, points) ->
                ChartData(dataPoints = points.sortedBy { it.date })
            }
        ))
        emit(Resource.Loading(false))
    }

    private fun extractDataPoints(
        shooterResults: List<Result>,
        signup: SignupEntry
    ): List<ChartDataPoint> {
        return shooterResults.map { result ->
            val avgScore = computeAverageStationScore(
                result.results.map { it.points }
            )
            ChartDataPoint(
                competitionId = signup.competitionsId,
                competitionName = signup.competition.name,
                date = signup.competition.date,
                averageSerieScore = avgScore,
                weaponClass = result.weaponClass.classname,
                resultsType = signup.competition.resultsType
            )
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

    private fun isCompetitionToday(dateStr: String): Boolean {
        return try {
            val competitionDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            competitionDate == LocalDate.now()
        } catch (e: Exception) {
            Log.w(TAG, "Could not parse date: $dateStr", e)
            false
        }
    }
}
