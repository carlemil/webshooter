package se.kjellstrand.webshooter.data.charts

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitions.local.CompetitionEntity
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.results.local.ChartPointRow
import se.kjellstrand.webshooter.data.results.local.ParticipantRow
import se.kjellstrand.webshooter.data.results.local.ResultEntity
import se.kjellstrand.webshooter.data.results.local.ResultsDao

class ChartsRepositoryDaoQueryTest {

    private class FakeResultsDao(
        var pointsForUser: List<ChartPointRow> = emptyList(),
        var pointsForUsers: List<ChartPointRow> = emptyList(),
        var participants: List<ParticipantRow> = emptyList(),
        var weaponClasses: List<String> = emptyList()
    ) : ResultsDao {
        var getChartPointsForUserCalls = 0
        var lastUserId: Long? = null
        var getChartPointsForUsersCalls = 0
        var lastUserIds: List<Long>? = null
        var lastCompetitionIds: List<Long>? = null

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
        ): List<ChartPointRow> {
            getChartPointsForUsersCalls++
            lastUserIds = userIds
            lastCompetitionIds = competitionIds
            return pointsForUsers
        }
        override suspend fun getAllParticipants(): List<ParticipantRow> = participants
        override suspend fun getAllWeaponClasses(): List<String> = weaponClasses
    }

    private class FakeCompetitionsDao(
        var completed: List<CompetitionEntity> = emptyList()
    ) : CompetitionsDao {
        override suspend fun getAll(): List<CompetitionEntity> = completed
        override fun observeAll(): Flow<List<CompetitionEntity>> = flowOf(completed)
        override suspend fun getCompletedCompetitions(): List<CompetitionEntity> = completed
        override suspend fun getCompletedCount(): Int = completed.size
        override suspend fun getMaxCompletedDate(): String? = completed.maxOfOrNull { it.date }
        override suspend fun getNonCompletedIds(): List<Long> = emptyList()
        override suspend fun insertAll(competitions: List<CompetitionEntity>) {}
        override suspend fun deleteAll() {}
        override suspend fun deleteById(id: Long) {}
    }

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
        val repo: ChartsRepository
    )

    private fun buildRepo(
        resultsDao: ResultsDao,
        competitionsDao: CompetitionsDao
    ): TestRig {
        val repo = ChartsRepository(
            competitionsDao,
            resultsDao,
            Gson()
        )
        return TestRig(repo)
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
    fun `ChartsRepository constructor does not accept ResultsRepository`() {
        val constructors = ChartsRepository::class.java.declaredConstructors
        val hasResultsRepository = constructors.any { c ->
            c.parameterTypes.any { it.name.endsWith("ResultsRepository") }
        }
        assertFalse(
            "ChartsRepository should no longer depend on ResultsRepository",
            hasResultsRepository
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

    // --- Fixed behavior for getShooterChartData (should FAIL before fix, PASS after fix) ---

    private suspend fun shooterSuccess(
        rig: TestRig,
        shooterIds: List<Long>,
        competitionIds: List<Long>,
        meta: Map<Long, CompetitionMeta> = emptyMap()
    ): Map<Long, ChartData> {
        val emissions = rig.repo.getShooterChartData(shooterIds, competitionIds, meta).toList()
        val success = emissions.firstOrNull { it is Resource.Success<*, *> }
                as? Resource.Success<Map<Long, ChartData>, *>
        assertNotNull("getShooterChartData should emit a Success", success)
        return success!!.data
    }

    @Test
    fun `getShooterChartData calls ResultsDao getChartPointsForUsers exactly once`(): Unit = runBlocking {
        val dao = FakeResultsDao(pointsForUsers = emptyList())
        val rig = buildRepo(dao, FakeCompetitionsDao())

        shooterSuccess(rig, shooterIds = listOf(1L, 2L), competitionIds = listOf(10L, 20L))

        assertEquals(1, dao.getChartPointsForUsersCalls)
        assertEquals(listOf(1L, 2L), dao.lastUserIds)
        assertEquals(listOf(10L, 20L), dao.lastCompetitionIds)
    }

    @Test
    fun `getShooterChartData groups DAO rows by userId`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUsers = listOf(
                chartRow(1, "2025-01-01", userId = 100, averageScore = 30.0),
                chartRow(2, "2025-02-01", userId = 100, averageScore = 35.0),
                chartRow(1, "2025-01-01", userId = 200, averageScore = 40.0)
            )
        )
        val rig = buildRepo(dao, FakeCompetitionsDao())

        val map = shooterSuccess(
            rig,
            shooterIds = listOf(100L, 200L),
            competitionIds = listOf(1L, 2L)
        )

        assertEquals(2, map[100L]?.dataPoints?.size)
        assertEquals(1, map[200L]?.dataPoints?.size)
        assertEquals(40.0, map[200L]?.dataPoints?.first()?.averageSerieScore ?: 0.0, 0.0001)
    }

    @Test
    fun `getShooterChartData includes empty list for shooters with no rows`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUsers = listOf(chartRow(1, "2025-01-01", userId = 100))
        )
        val rig = buildRepo(dao, FakeCompetitionsDao())

        val map = shooterSuccess(
            rig,
            shooterIds = listOf(100L, 999L),
            competitionIds = listOf(1L)
        )

        assertNotNull("Shooter 999 must appear with an empty entry", map[999L])
        assertTrue(map[999L]!!.dataPoints.isEmpty())
    }

    @Test
    fun `getShooterChartData sorts each shooter data points by date`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUsers = listOf(
                chartRow(3, "2025-03-01", userId = 100),
                chartRow(1, "2025-01-01", userId = 100),
                chartRow(2, "2025-02-01", userId = 100)
            )
        )
        val rig = buildRepo(dao, FakeCompetitionsDao())

        val map = shooterSuccess(
            rig,
            shooterIds = listOf(100L),
            competitionIds = listOf(1L, 2L, 3L)
        )

        assertEquals(
            listOf("2025-01-01", "2025-02-01", "2025-03-01"),
            map[100L]!!.dataPoints.map { it.date }
        )
    }

    @Test
    fun `getShooterChartData uses only ResultsDao for data`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUsers = listOf(chartRow(1, "2025-01-01", userId = 100))
        )
        val rig = buildRepo(dao, FakeCompetitionsDao())

        shooterSuccess(rig, shooterIds = listOf(100L), competitionIds = listOf(1L))

        // Single SQL call replaces the old per-competition fetch loop.
        assertEquals(1, dao.getChartPointsForUsersCalls)
    }

    // --- Guard tests for getShooterChartData (should PASS before and after fix) ---

    @Test
    fun `getShooterChartData still emits Loading true and Loading false`(): Unit = runBlocking {
        val dao = FakeResultsDao(pointsForUsers = emptyList())
        val rig = buildRepo(dao, FakeCompetitionsDao())

        val emissions = rig.repo
            .getShooterChartData(listOf(1L), listOf(1L), emptyMap())
            .toList()
        assertTrue(emissions.first() is Resource.Loading)
        assertTrue(emissions.last() is Resource.Loading)
        assertTrue((emissions.first() as Resource.Loading).isLoading)
        assertFalse((emissions.last() as Resource.Loading).isLoading)
    }
}
