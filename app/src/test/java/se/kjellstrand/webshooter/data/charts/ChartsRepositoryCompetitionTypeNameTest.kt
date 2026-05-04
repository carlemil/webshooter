package se.kjellstrand.webshooter.data.charts

import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitions.local.CompetitionEntity
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.results.local.ChartPointRow
import se.kjellstrand.webshooter.data.results.local.ClubStatsRow
import se.kjellstrand.webshooter.data.results.local.ParticipantRow
import se.kjellstrand.webshooter.data.results.local.ResultEntity
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.results.local.SeriesRow

class ChartsRepositoryCompetitionTypeNameTest {

    private class FakeResultsDao(
        var pointsForUser: List<ChartPointRow> = emptyList(),
        var pointsForUsers: List<ChartPointRow> = emptyList()
    ) : ResultsDao {
        override suspend fun getByCompetition(competitionId: Long): List<ResultEntity> = emptyList()
        override suspend fun insertAll(results: List<ResultEntity>) {}
        override suspend fun deleteByCompetition(competitionId: Long) {}
        override suspend fun getCompetitionIdsWithResults(): List<Long> = emptyList()
        override suspend fun getChartPointsForUser(userId: Long, today: String): List<ChartPointRow> =
            pointsForUser
        override suspend fun getChartPointsForUsers(
            userIds: List<Long>,
            competitionIds: List<Long>
        ): List<ChartPointRow> = pointsForUsers
        override suspend fun getAllParticipants(): List<ParticipantRow> = emptyList()
        override suspend fun getPrecisionParticipants(today: String): List<ParticipantRow> = emptyList()
        override suspend fun getPrecisionSeriesForUser(userId: Long, today: String): List<SeriesRow> = emptyList()
        override suspend fun getAllWeaponClasses(): List<String> = emptyList()
        override suspend fun getClubStats(userIds: List<Long>, year: Int, classPrefix: String, today: String): List<ClubStatsRow> = emptyList()
        override suspend fun getClubStatsAllYears(userIds: List<Long>, classPrefix: String, today: String): List<ClubStatsRow> = emptyList()
        override suspend fun getClubStatsYears(userIds: List<Long>, today: String): List<String> = emptyList()
    }

    private class FakeCompetitionsDao(
        var completed: List<CompetitionEntity> = emptyList()
    ) : CompetitionsDao {
        override fun observeAll(): Flow<List<CompetitionEntity>> = flowOf(completed)
        override suspend fun getCompletedCompetitions(today: String): List<CompetitionEntity> = completed
        override suspend fun getAll(): List<CompetitionEntity> = completed
        override suspend fun insertAll(competitions: List<CompetitionEntity>) {}
        override suspend fun deleteByIds(ids: List<Long>) {}
    }

    private fun competition(
        id: Long,
        name: String,
        date: String,
        resultsType: String,
        competitionTypeJson: String
    ) = CompetitionEntity(
        id = id, name = name, date = date, status = "completed", statusHuman = "completed",
        contactName = null, contactVenue = null, contactCity = null, contactEmail = null,
        contactTelephone = null, lat = 0.0, lng = 0.0, googleMaps = null, description = null,
        website = null, resultsType = resultsType, resultsTypeHuman = null,
        signupsOpeningDate = null, signupsClosingDate = null,
        allowSignupsAfterClosingDateHuman = null, startTimeHuman = null, finalTimeHuman = null,
        signupsCount = 0, patrolsCount = 0, allowTeams = 0,
        competitionTypeJson = competitionTypeJson,
        weaponGroupsJson = "[]", weaponClassesJson = "[]",
        userSignupsJson = "[]", clubJson = """{"id":0,"name":""}"""
    )

    private fun chartRow(
        competitionId: Long,
        date: String = "2025-01-01",
        weaponClass: String = "C",
        userId: Long = 100,
        averageScore: Double = 42.0,
        resultsType: String = "precision"
    ) = ChartPointRow(
        competitionId = competitionId,
        competitionName = "Comp $competitionId",
        date = date,
        resultsType = resultsType,
        weaponClassName = weaponClass,
        userId = userId,
        averageScore = averageScore
    )

    private fun buildRepo(
        resultsDao: ResultsDao,
        competitionsDao: CompetitionsDao
    ): ChartsRepository = ChartsRepository(competitionsDao, resultsDao, Json { ignoreUnknownKeys = true; coerceInputValues = true; explicitNulls = false })

    private suspend fun successOf(repo: ChartsRepository, userId: Long): ChartData {
        val emissions = repo.getChartData(userId).toList()
        val success = emissions.firstOrNull { it is Resource.Success<*, *> }
                as? Resource.Success<ChartData, *>
        assertNotNull("getChartData should emit Success", success)
        return success!!.data
    }

    private suspend fun shooterSuccess(
        repo: ChartsRepository,
        shooterIds: List<Long>,
        competitionIds: List<Long>,
        meta: Map<Long, CompetitionMeta>
    ): Map<Long, ChartData> {
        val emissions = repo.getShooterChartData(shooterIds, competitionIds, meta).toList()
        val success = emissions.firstOrNull { it is Resource.Success<*, *> }
                as? Resource.Success<Map<Long, ChartData>, *>
        assertNotNull("getShooterChartData should emit Success", success)
        return success!!.data
    }

    /** Reads the `competitionTypeName` field by reflection so this test compiles
     *  even when the field has not been added yet — that is the whole point. */
    private fun typeNameOf(obj: Any): String? {
        val field = try {
            obj.javaClass.getDeclaredField("competitionTypeName")
        } catch (_: NoSuchFieldException) {
            return null
        }
        field.isAccessible = true
        return field.get(obj) as String?
    }

    /** Build a CompetitionMeta via reflection so we can pass a competitionTypeName
     *  even if the data class only has it after the fix. Falls back to the existing
     *  3-arg constructor (name/date/resultsType) and writes the field afterwards. */
    private fun metaWithTypeName(
        name: String,
        date: String,
        resultsType: String,
        competitionTypeName: String
    ): CompetitionMeta {
        val ctor = CompetitionMeta::class.java.declaredConstructors
            .firstOrNull { c ->
                c.parameterTypes.size >= 4 &&
                c.parameterTypes.take(4).all { it == String::class.java }
            }
        if (ctor != null) {
            return ctor.newInstance(name, date, resultsType, competitionTypeName) as CompetitionMeta
        }
        val baseCtor = CompetitionMeta::class.java.declaredConstructors.first()
        val args = baseCtor.parameterTypes.map { paramType ->
            when (paramType) {
                String::class.java -> ""
                else -> null
            }
        }.toMutableList()
        @Suppress("UNCHECKED_CAST")
        val meta = baseCtor.newInstance(*args.toTypedArray()) as CompetitionMeta
        // 3-arg fallback; field write below would fail for val fields, so just let
        // the test call sites that depend on the field assert null and fail RED.
        return meta
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ChartDataPoint has a competitionTypeName field`() {
        val field = ChartDataPoint::class.java.declaredFields.find { it.name == "competitionTypeName" }
        assertNotNull("ChartDataPoint should declare a competitionTypeName field", field)
    }

    @Test
    fun `CompetitionMeta has a competitionTypeName field`() {
        val field = CompetitionMeta::class.java.declaredFields.find { it.name == "competitionTypeName" }
        assertNotNull("CompetitionMeta should declare a competitionTypeName field", field)
    }

    @Test
    fun `getChartData populates competitionTypeName on data points from JSON`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUser = listOf(
                chartRow(competitionId = 1, resultsType = "precision"),
                chartRow(competitionId = 2, resultsType = "precision")
            )
        )
        val competitionsDao = FakeCompetitionsDao(
            completed = listOf(
                competition(1, "Magnum Cup", "2025-01-01", "PRECISION",
                    competitionTypeJson = """{"id":11,"name":"Magnumprecision"}"""),
                competition(2, "Standard Cup", "2025-02-01", "PRECISION",
                    competitionTypeJson = """{"id":1,"name":"Precision"}""")
            )
        )
        val repo = buildRepo(dao, competitionsDao)

        val data = successOf(repo, userId = 100L)

        val byId = data.dataPoints.associateBy { it.competitionId }
        assertEquals("Magnumprecision", typeNameOf(byId[1L]!!))
        assertEquals("Precision", typeNameOf(byId[2L]!!))
    }

    @Test
    fun `getChartData populates competitionTypeName on CompetitionMeta entries`(): Unit = runBlocking {
        val dao = FakeResultsDao()
        val competitionsDao = FakeCompetitionsDao(
            completed = listOf(
                competition(7, "Magnum Cup", "2025-01-01", "PRECISION",
                    competitionTypeJson = """{"id":11,"name":"Magnumprecision"}"""),
                competition(8, "Field Cup", "2025-02-01", "FIELD",
                    competitionTypeJson = """{"id":3,"name":"Fältskytte"}""")
            )
        )
        val repo = buildRepo(dao, competitionsDao)

        val data = successOf(repo, userId = 100L)

        assertEquals("Magnumprecision", typeNameOf(data.allCompetitionMeta[7L]!!))
        assertEquals("Fältskytte", typeNameOf(data.allCompetitionMeta[8L]!!))
    }

    @Test
    fun `getShooterChartData populates competitionTypeName from competitionMetadata`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUsers = listOf(
                chartRow(competitionId = 1, userId = 100, resultsType = "precision"),
                chartRow(competitionId = 2, userId = 100, resultsType = "precision")
            )
        )
        val repo = buildRepo(dao, FakeCompetitionsDao())

        val meta = mapOf(
            1L to metaWithTypeName(
                name = "Magnum Cup", date = "2025-01-01",
                resultsType = "precision", competitionTypeName = "Magnumprecision"
            ),
            2L to metaWithTypeName(
                name = "Standard Cup", date = "2025-02-01",
                resultsType = "precision", competitionTypeName = "Precision"
            )
        )

        val map = shooterSuccess(repo, shooterIds = listOf(100L), competitionIds = listOf(1L, 2L), meta = meta)

        val byId = map[100L]!!.dataPoints.associateBy { it.competitionId }
        assertEquals("Magnumprecision", typeNameOf(byId[1L]!!))
        assertEquals("Precision", typeNameOf(byId[2L]!!))
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartDataPoint still has existing fields`() {
        val fields = ChartDataPoint::class.java.declaredFields.map { it.name }.toSet()
        assertTrue("competitionId field must remain", "competitionId" in fields)
        assertTrue("date field must remain", "date" in fields)
        assertTrue("averageSerieScore field must remain", "averageSerieScore" in fields)
        assertTrue("weaponClass field must remain", "weaponClass" in fields)
        assertTrue("resultsType field must remain", "resultsType" in fields)
    }

    @Test
    fun `getChartData maps existing fields correctly`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUser = listOf(
                chartRow(competitionId = 1, weaponClass = "A", averageScore = 30.0, resultsType = "field")
            )
        )
        val competitionsDao = FakeCompetitionsDao(
            completed = listOf(
                competition(1, "Comp 1", "2025-01-01", "FIELD",
                    competitionTypeJson = """{"id":3,"name":"Fältskytte"}""")
            )
        )
        val repo = buildRepo(dao, competitionsDao)

        val data = successOf(repo, userId = 100L)

        assertEquals(1, data.dataPoints.size)
        val point = data.dataPoints[0]
        assertEquals(1L, point.competitionId)
        assertEquals("A", point.weaponClass)
        assertEquals(30.0, point.averageSerieScore, 0.0001)
        assertEquals("field", point.resultsType)
    }

    @Test
    fun `getChartData competitionTypeName is null when meta has no entry for competitionId`(): Unit = runBlocking {
        val dao = FakeResultsDao(
            pointsForUser = listOf(chartRow(competitionId = 99, resultsType = "precision"))
        )
        val competitionsDao = FakeCompetitionsDao(completed = emptyList())
        val repo = buildRepo(dao, competitionsDao)

        val data = successOf(repo, userId = 100L)

        assertEquals(1, data.dataPoints.size)
        assertNull(
            "competitionTypeName must be null when no metadata exists for the row's competitionId",
            typeNameOf(data.dataPoints[0])
        )
    }
}
