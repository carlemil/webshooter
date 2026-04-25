package se.kjellstrand.webshooter.data.seriespoints

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
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
    private val gson: Gson
) {
    companion object {
        private const val TAG = "SeriesPointsRepository"
    }

    fun getSeriesPoints(userId: Long): Flow<Resource<SeriesPointsData, UserError>> = flow {
        emit(Resource.Loading(true))

        val today = LocalDate.now().toString()
        val rows = resultsDao.getPrecisionSeriesForUser(userId, today)
        val type = object : TypeToken<List<StationResult>>() {}.type
        val competitions = rows.mapNotNull { row ->
            val stations: List<StationResult>? = try {
                gson.fromJson(row.stationResultsJson, type)
            } catch (e: JsonSyntaxException) {
                Log.w(TAG, "Failed to parse stationResultsJson for ${row.competitionId}", e)
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
