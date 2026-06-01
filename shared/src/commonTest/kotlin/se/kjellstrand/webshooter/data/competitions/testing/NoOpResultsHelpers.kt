package se.kjellstrand.webshooter.data.competitions.testing

import kotlinx.serialization.json.Json
import okio.IOException
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.local.ChartPointRow
import se.kjellstrand.webshooter.data.results.local.ClubStatsRow
import se.kjellstrand.webshooter.data.results.local.ParticipantRow
import se.kjellstrand.webshooter.data.results.local.ResultEntity
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.results.local.SeriesRow
import se.kjellstrand.webshooter.data.results.remote.ResultsRemoteDataSource
import se.kjellstrand.webshooter.data.results.remote.ResultsResponse

open class NoOpResultsDao : ResultsDao {
    val deletedCompetitionIds = mutableListOf<Long>()
    val insertedBatches = mutableListOf<List<ResultEntity>>()
    val seeded = mutableMapOf<Long, MutableList<ResultEntity>>()

    fun seed(competitionId: Long, rows: List<ResultEntity>) {
        seeded[competitionId] = rows.toMutableList()
    }

    override suspend fun getByCompetition(competitionId: Long): List<ResultEntity> =
        seeded[competitionId]?.toList() ?: emptyList()

    override suspend fun insertAll(results: List<ResultEntity>) {
        insertedBatches.add(results)
        results.groupBy { it.competitionsId }.forEach { (cid, rows) ->
            seeded.getOrPut(cid) { mutableListOf() }.addAll(rows)
        }
    }

    override suspend fun deleteByCompetition(competitionId: Long) {
        deletedCompetitionIds.add(competitionId)
        seeded.remove(competitionId)
    }

    override suspend fun getCompetitionIdsWithResults(): List<Long> =
        seeded.filterValues { it.isNotEmpty() }.keys.toList()

    override suspend fun getChartPointsForUser(userId: Long, today: String): List<ChartPointRow> = emptyList()
    override suspend fun getChartPointsForUsers(userIds: List<Long>, competitionIds: List<Long>): List<ChartPointRow> = emptyList()
    override suspend fun getPrecisionSeriesForUser(userId: Long, today: String): List<SeriesRow> = emptyList()
    override suspend fun getAllParticipants(): List<ParticipantRow> = emptyList()
    override suspend fun getPrecisionParticipants(today: String): List<ParticipantRow> = emptyList()
    override suspend fun getAllWeaponClasses(): List<String> = emptyList()
    override suspend fun getClubStats(userIds: List<Long>, year: Int, classPrefix: String, today: String): List<ClubStatsRow> = emptyList()
    override suspend fun getClubStatsAllYears(userIds: List<Long>, classPrefix: String, today: String): List<ClubStatsRow> = emptyList()
    override suspend fun getClubStatsYears(userIds: List<Long>, today: String): List<String> = emptyList()
}

private class StubResultsRemoteDataSource : ResultsRemoteDataSource {
    override suspend fun getResults(id: Long): ResultsResponse =
        throw IOException("StubResultsRemoteDataSource: not used in this test")
}

open class NoOpResultsRepository(
    private val refreshAction: (suspend (Long) -> Unit)? = null,
    private val backingDao: ResultsDao = NoOpResultsDao(),
    private val remote: ResultsRemoteDataSource = StubResultsRemoteDataSource()
) : ResultsRepository(remote, backingDao, testJson()) {

    val refreshCalls = mutableListOf<Long>()
    // kotlinx.coroutines.test.runTest pins the test body to a single
    // dispatcher, so a plain Int counter is enough — no atomicity needed
    // for the scenarios these tests exercise.
    private var concurrency: Int = 0
    var maxConcurrency: Int = 0

    override suspend fun refreshResultsFor(competitionId: Long): RefreshOutcome {
        concurrency += 1
        if (concurrency > maxConcurrency) maxConcurrency = concurrency
        return try {
            refreshCalls.add(competitionId)
            refreshAction?.invoke(competitionId)
            RefreshOutcome.Success
        } finally {
            concurrency -= 1
        }
    }
}

fun testJson(): Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

fun resultEntity(id: Long, competitionId: Long, userId: Long = 0L): ResultEntity = ResultEntity(
    id = id,
    competitionsId = competitionId,
    signupsId = 0,
    placement = 0,
    figureHits = 0,
    hits = 0,
    points = 0,
    stdMedal = null,
    signupJson = "{}",
    weaponClassJson = "{}",
    stationResultsJson = "[]",
    userId = userId,
    userFullname = "",
    weaponClassName = "",
    averagePoints = 0.0,
    averageHits = 0.0
)

