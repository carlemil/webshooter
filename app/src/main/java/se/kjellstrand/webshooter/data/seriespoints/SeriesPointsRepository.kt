package se.kjellstrand.webshooter.data.seriespoints

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.charts.Participant
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.results.remote.StationResult
import javax.inject.Inject
import javax.inject.Singleton

data class CompetitionSeries(
    val competitionId: Long,
    val competitionName: String,
    val date: String,
    val weaponClass: String,
    val seriesPoints: List<Long>
)

data class SeriesPointsData(
    val competitions: List<CompetitionSeries>,
    val allWeaponClasses: List<String>,
    val allParticipants: List<Participant>
)

@Singleton
class SeriesPointsRepository @Inject constructor(
    private val resultsDao: ResultsDao,
    private val json: Json
) {
    companion object {
        private const val TAG = "SeriesPointsRepository"
    }

    fun getSeriesPoints(userId: Long): Flow<Resource<SeriesPointsData, UserError>> = flow {
        emit(Resource.Loading(true))

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val rows = resultsDao.getPrecisionSeriesForUser(userId, today)
        val competitions = rows.mapNotNull { row ->
            val stations: List<StationResult>? = try {
                json.decodeFromString<List<StationResult>>(row.stationResultsJson)
            } catch (e: SerializationException) {
                Napier.w("Failed to parse stationResultsJson for ${row.competitionId}", e, TAG)
                null
            }
            if (stations.isNullOrEmpty()) return@mapNotNull null
            CompetitionSeries(
                competitionId = row.competitionId,
                competitionName = row.competitionName,
                date = row.date,
                weaponClass = row.weaponClassName,
                seriesPoints = stations.map { it.points }
            )
        }.sortedBy { it.date }

        val allWeaponClasses = resultsDao.getAllWeaponClasses()
        val allParticipants = resultsDao.getPrecisionParticipants(today).map {
            Participant(userId = it.userId, fullname = it.fullname)
        }

        emit(
            Resource.Success(
                SeriesPointsData(
                    competitions = competitions,
                    allWeaponClasses = allWeaponClasses,
                    allParticipants = allParticipants
                )
            )
        )
        emit(Resource.Loading(false))
    }
}
