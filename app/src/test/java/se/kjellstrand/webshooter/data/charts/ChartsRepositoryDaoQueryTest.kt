package se.kjellstrand.webshooter.data.charts

import com.google.gson.Gson
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitions.local.CompetitionEntity
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.local.ChartPointRow
import se.kjellstrand.webshooter.data.results.local.ParticipantRow
import se.kjellstrand.webshooter.data.results.local.ResultEntity
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.results.remote.ResultsRemoteDataSource
import se.kjellstrand.webshooter.data.results.remote.ResultsResponse

class ChartsRepositoryDaoQueryTest {

    private class FakeResultsDao(
        var pointsForUser: List<ChartPointRow> = emptyList(),
        var participants: List<ParticipantRow> = emptyList(),
        var weaponClasses: List<String> = emptyList()
    ) : ResultsDao {
        var getChartPointsForUserCalls = 0
        var lastUserId: Long? = null

        override suspend fun getByCompetition(competitionId: Long): List<ResultEntity> = emptyList()
        override suspend fun insertAll(results: List<ResultEntity>) {}
        override suspend fun deleteByCompetition(competitionId: Long) {}
        override suspend fun getChartPointsForUser(userId: Long): List<ChartPointRow> {
            getChartPointsForUserCalls++
            lastUserId = userId
            return pointsForUser
        }
        override suspend fun getChartPointsForUsers(
            userIds: List<Long>,
            competitionIds: List<Long>
        ): List<ChartPointRow> = emptyList()
        override suspend fun getAllParticipants(): List<ParticipantRow> = participants
        override suspend fun getAllWeaponClasses(): List<String> = weaponClasses
    }

    private class FakeCompetitionsDao(
        var completed: List<CompetitionEntity> = emptyList()
    ) : CompetitionsDao {
        override suspend fun getAll(): List<CompetitionEntity> = completed
        override suspend fun getCompletedCompetitions(): List<CompetitionEntity> = completed
        override suspend fun getCompletedCount(): Int = completed.size
        override suspend fun insertAll(competitions: List<CompetitionEntity>) {}
        override suspend fun deleteAll() {}
    }

    private class FakeRemote : ResultsRemoteDataSource {
        var callCount = 0
        override suspend fun getResults(id: Long): ResultsResponse {
            callCount++
            return ResultsResponse(results = emptyList())
        }
    }

    private fun stubResultsRepository(dao: ResultsDao, remote: FakeRemote): ResultsRepository =
        ResultsRepository(remote, dao, Gson())

    private fun competition(id: Long, name: String, date: String, type: String) = CompetitionEntity(
        id = id, name = name, date = date, status = "completed", statusHuman = "completed",
        contactName = null, contactVenue = null, contactCity = null, contactEmail = null,
        contactTelephone = null, lat = 0.0, lng = 0.0, googleMaps = null, description = null,
        website = null, resultsType = type, resultsTypeHuman = null,
        signupsOpeningDate = null, signupsClosingDate = null,
        allowSignupsAfterClosingDateHuman = null, startTimeHuman = null, finalTimeHuman = null,
        signupsCount = 0, patrolsCount = 0, allowTeams = 0,
        competitionTypeJson = "{}", weaponGroupsJson = "[]", weaponClassesJson = "[]",
        userSignupsJson = "[]", clubJson = "{}"
    )

    private fun chartRow(
        competitionId: Long,
        date: String,
        weaponClass: String = "C",
        userId: Long = 100,
        averageScore: Double = 42.0,
        resultsType: String = "precision",
        competitionName: String = "Comp $competitionId"
    ) = ChartPointRow(
        competitionId = competitionId,
        competitionName = competitionName,
        date = date,
        resultsType = resultsType,
        weaponClassName = weaponClass,
        userId = userId,
        averageScore = averageScore
    )

    private class TestRig(
        val repo: ChartsRepository,
        val remote: FakeRemote
    )

    private fun buildRepo(
        resultsDao: ResultsDao,
        competitionsDao: CompetitionsDao
    ): TestRig {
        val remote = FakeRemote()
        val repo = ChartsRepository(
            stubResultsRepository(resultsDao, remote),
            competitionsDao,
            resultsDao,
            Gson()
        )
        return TestRig(repo, remote)
    }

    private suspend fun successOf(repo: ChartsRepository, userId: Long): ChartData {
        val emissions = repo.getChartData(userId).toList()
        val success = emissions.firstOrNull { it is Resource.Success<*, *> }
                as? Resource.Success<ChartData, *>
        assertNotNull("getChartData should emit a Success", success)
        return success!!.data
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `getChartData calls ResultsDao getChartPointsForUser exactly once`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUser = listOf(chartRow(1, "2025-01-01"))
        )
        val competitionsDao = FakeCompetitionsDao(
            completed = listOf(competition(1, "Comp 1", "2025-01-01", "PRECISION"))
        )
        val repo = buildRepo(dao, competitionsDao)

        successOf(repo.repo, userId = 100L)

        assertEquals(1, dao.getChartPointsForUserCalls)
        assertEquals(100L, dao.lastUserId)
    }

    @Test
    fun `getChartData maps DAO rows directly to ChartDataPoint`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUser = listOf(
                chartRow(1, "2025-01-01", weaponClass = "A", averageScore = 30.0, resultsType = "precision"),
                chartRow(2, "2025-02-01", weaponClass = "B", averageScore = 40.0, resultsType = "field")
            )
        )
        val competitionsDao = FakeCompetitionsDao(
            completed = listOf(
                competition(1, "Comp 1", "2025-01-01", "PRECISION"),
                competition(2, "Comp 2", "2025-02-01", "FIELD")
            )
        )
        val repo = buildRepo(dao, competitionsDao)

        val data = successOf(repo.repo, userId = 100L)

        assertEquals(2, data.dataPoints.size)
        val first = data.dataPoints[0]
        assertEquals(1L, first.competitionId)
        assertEquals("A", first.weaponClass)
        assertEquals(30.0, first.averageSerieScore, 0.0001)
        assertEquals("precision", first.resultsType)
    }

    @Test
    fun `getChartData sorts data points by date ascending`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUser = listOf(
                chartRow(2, "2025-03-01"),
                chartRow(1, "2025-01-01"),
                chartRow(3, "2025-02-01")
            )
        )
        val competitionsDao = FakeCompetitionsDao(
            completed = listOf(
                competition(1, "Comp 1", "2025-01-01", "PRECISION"),
                competition(2, "Comp 2", "2025-03-01", "PRECISION"),
                competition(3, "Comp 3", "2025-02-01", "PRECISION")
            )
        )
        val repo = buildRepo(dao, competitionsDao)

        val data = successOf(repo.repo, userId = 100L)

        assertEquals(listOf("2025-01-01", "2025-02-01", "2025-03-01"), data.dataPoints.map { it.date })
    }

    @Test
    fun `getChartData populates allParticipants from dao getAllParticipants`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUser = emptyList(),
            participants = listOf(
                ParticipantRow(userId = 1, fullname = "Alice"),
                ParticipantRow(userId = 2, fullname = "Bob")
            )
        )
        val competitionsDao = FakeCompetitionsDao()
        val repo = buildRepo(dao, competitionsDao)

        val data = successOf(repo.repo, userId = 100L)

        assertEquals(2, data.allParticipants.size)
        assertEquals(setOf(1L, 2L), data.allParticipants.map { it.userId }.toSet())
        assertEquals(setOf("Alice", "Bob"), data.allParticipants.map { it.fullname }.toSet())
    }

    @Test
    fun `getChartData populates allWeaponClasses from dao getAllWeaponClasses`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            weaponClasses = listOf("A", "B", "C")
        )
        val competitionsDao = FakeCompetitionsDao()
        val repo = buildRepo(dao, competitionsDao)

        val data = successOf(repo.repo, userId = 100L)

        assertEquals(listOf("A", "B", "C"), data.allWeaponClasses)
    }

    @Test
    fun `getChartData does not call ResultsRepository getPreferCached`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUser = listOf(chartRow(1, "2025-01-01"))
        )
        val competitionsDao = FakeCompetitionsDao(
            completed = listOf(competition(1, "Comp 1", "2025-01-01", "PRECISION"))
        )
        val rig = buildRepo(dao, competitionsDao)

        successOf(rig.repo, userId = 100L)

        // The DAO has no rows for getByCompetition, so the old loop falls
        // through to ResultsRemoteDataSource.getResults, which FakeRemote counts.
        assertEquals(
            "getChartData must not invoke ResultsRepository.getPreferCached",
            0,
            rig.remote.callCount
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `getChartData still emits Loading true and Loading false around Success`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUser = listOf(chartRow(1, "2025-01-01"))
        )
        val competitionsDao = FakeCompetitionsDao(
            completed = listOf(competition(1, "Comp 1", "2025-01-01", "PRECISION"))
        )
        val repo = buildRepo(dao, competitionsDao)

        val emissions = repo.repo.getChartData(100L).toList()
        assertTrue(
            "Should emit Loading(true) first",
            emissions.first() is Resource.Loading && (emissions.first() as Resource.Loading).isLoading
        )
        assertTrue(
            "Should emit Loading(false) last",
            emissions.last() is Resource.Loading && !(emissions.last() as Resource.Loading).isLoading
        )
    }

    @Test
    fun `getChartData allCompetitionMeta contains entries for completed competitions`(): Unit = runBlocking {
        val dao = FakeResultsDao(pointsForUser = emptyList())
        val competitionsDao = FakeCompetitionsDao(
            completed = listOf(
                competition(1, "Alpha", "2025-01-01", "PRECISION"),
                competition(2, "Beta", "2025-02-01", "FIELD")
            )
        )
        val repo = buildRepo(dao, competitionsDao)

        val data = successOf(repo.repo, userId = 100L)

        assertEquals(2, data.allCompetitionMeta.size)
        assertEquals("Alpha", data.allCompetitionMeta[1L]?.name)
        assertEquals("field", data.allCompetitionMeta[2L]?.resultsType)
    }
}
