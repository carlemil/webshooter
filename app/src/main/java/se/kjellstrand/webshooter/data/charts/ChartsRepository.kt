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
import se.kjellstrand.webshooter.data.results.local.ResultsDao
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
        @Suppress("UNUSED_PARAMETER") competitionMetadata: Map<Long, CompetitionMeta>
    ): Flow<Resource<Map<Long, ChartData>, UserError>> = flow {
        emit(Resource.Loading(true))

        val result = mutableMapOf<Long, MutableList<ChartDataPoint>>()
        shooterIds.forEach { result[it] = mutableListOf() }

        val rows = resultsDao.getChartPointsForUsers(shooterIds, competitionIds)
        for (row in rows) {
            result[row.userId]?.add(
                ChartDataPoint(
                    competitionId = row.competitionId,
                    competitionName = row.competitionName,
                    date = row.date,
                    averageSerieScore = row.averageScore,
                    weaponClass = row.weaponClassName,
                    resultsType = row.resultsType
                )
            )
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

    private fun ResultsType.toApiString(): String = apiString
}
