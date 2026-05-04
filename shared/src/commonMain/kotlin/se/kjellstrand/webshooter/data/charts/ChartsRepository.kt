package se.kjellstrand.webshooter.data.charts

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.local.toDomain
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.local.ResultsDao



data class ChartDataPoint(
    val competitionId: Long,
    val competitionName: String,
    val date: String,
    val averageSerieScore: Double,
    val weaponClass: String,
    val resultsType: String,
    val competitionTypeName: String? = null
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
    val resultsType: String,
    val competitionTypeName: String? = null
)


class ChartsRepository constructor(
    private val competitionsDao: CompetitionsDao,
    private val resultsDao: ResultsDao,
    private val json: Json
) {
    companion object {
        private const val TAG = "ChartsRepository"
    }

    fun getChartData(userId: Long): Flow<Resource<ChartData, UserError>> = flow {
        emit(Resource.Loading(true))

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val rows = resultsDao.getChartPointsForUser(userId, today)

        val allWeaponClasses = resultsDao.getAllWeaponClasses()
        val allParticipants = resultsDao.getAllParticipants().map {
            Participant(userId = it.userId, fullname = it.fullname)
        }

        val allCompetitionMeta = mutableMapOf<Long, CompetitionMeta>()
        val competitionTypeNames = mutableMapOf<Long, String?>()
        for (competition in competitionsDao.getCompletedCompetitions(today).mapNotNull { it.toDomain(json) }) {
            val apiString = try {
                competition.resultsType.toApiString()
            } catch (e: Exception) {
                Napier.w("Unknown resultsType for competition ${competition.id}, skipping", tag = TAG)
                continue
            }
            val typeName = competition.competitionType?.name
            competitionTypeNames[competition.id] = typeName
            allCompetitionMeta[competition.id] = CompetitionMeta(
                name = competition.name,
                date = competition.date,
                resultsType = apiString,
                competitionTypeName = typeName
            )
        }

        val dataPoints = rows
            .map { row ->
                ChartDataPoint(
                    competitionId = row.competitionId,
                    competitionName = row.competitionName,
                    date = row.date,
                    averageSerieScore = row.averageScore,
                    weaponClass = row.weaponClassName,
                    resultsType = row.resultsType,
                    competitionTypeName = competitionTypeNames[row.competitionId]
                )
            }
            .sortedBy { it.date }

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

        val rows = resultsDao.getChartPointsForUsers(shooterIds, competitionIds)
        for (row in rows) {
            result[row.userId]?.add(
                ChartDataPoint(
                    competitionId = row.competitionId,
                    competitionName = row.competitionName,
                    date = row.date,
                    averageSerieScore = row.averageScore,
                    weaponClass = row.weaponClassName,
                    resultsType = row.resultsType,
                    competitionTypeName = competitionMetadata[row.competitionId]?.competitionTypeName
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
