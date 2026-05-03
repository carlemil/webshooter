package se.kjellstrand.webshooter.data.competitions

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.competitions.local.CompetitionEntity
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.local.toEntity
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsResponse
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.competitions.testing.NoOpResultsRepository
import se.kjellstrand.webshooter.data.competitions.testing.NoOpResultsDao

class CompetitionsRepositoryObserveAllTest {

    private val gson = Gson()

    private fun datum(id: Long): Datum = Datum(
        id = id,
        name = "Comp $id",
        allowTeams = 0,
        lat = 0.0,
        lng = 0.0,
        description = "",
        resultsType = ResultsType.PRECISION,
        date = "2026-01-01",
        signupsOpeningDate = "2025-12-01",
        signupsClosingDate = "2025-12-15",
        weaponGroups = emptyList(),
        signupsCount = 0,
        patrolsCount = 0,
        status = "open",
        statusHuman = "open",
        startTimeHuman = "08:00",
        finalTimeHuman = "17:00",
        allowSignupsAfterClosingDateHuman = "",
        resultsTypeHuman = "Precision",
        competitionType = CompetitionType(id = 1, name = "Precision"),
        weaponClasses = emptyList(),
        userSignups = emptyList(),
        club = Club(id = 1, name = "Club")
    )

    private class FakeRemote : CompetitionsRemoteDataSource {
        override suspend fun getCompetitions(
            page: Int, perPage: Int, status: String, type: Int, userSignup: Int
        ): CompetitionsResponse = error("not used in this test")
    }

    private class FakeDao(
        var observed: List<CompetitionEntity> = emptyList()
    ) : CompetitionsDao {
        override fun observeAll(): Flow<List<CompetitionEntity>> = flowOf(observed)
        override suspend fun getCompletedCompetitions(today: String): List<CompetitionEntity> = emptyList()
        override suspend fun getAll(): List<CompetitionEntity> = observed
        override suspend fun insertAll(competitions: List<CompetitionEntity>) {}
        override suspend fun deleteByIds(ids: List<Long>) {}
    }

    private fun buildRepo(dao: FakeDao = FakeDao()) =
        CompetitionsRepository(FakeRemote(), dao, gson, NoOpResultsRepository(), NoOpResultsDao())

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `observeAll method exists on repository`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "observeAll" }
        assertNotNull("CompetitionsRepository should have observeAll", method)
    }

    @Test
    fun `observeAll returns Flow and is not a suspend fun`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "observeAll" }
        assertNotNull(method)
        assertTrue(
            "observeAll should NOT be suspend (Flow returns reactively)",
            method!!.parameterTypes.none { it.name.contains("Continuation") }
        )
        assertTrue(
            "observeAll should return a Flow",
            Flow::class.java.isAssignableFrom(method.returnType)
        )
    }

    @Test
    fun `observeAll emits domain Datum list when DAO emits entities`(): Unit = runBlocking {
        val entities = listOf(datum(1).toEntity(gson), datum(2).toEntity(gson))
        val dao = FakeDao(observed = entities)
        val repo = buildRepo(dao)

        val emitted = repo.observeAll().first()

        assertEquals(2, emitted.size)
        assertEquals(setOf(1L, 2L), emitted.map { it.id }.toSet())
    }

    @Test
    fun `observeAll filters out entities that fail to deserialize`(): Unit = runBlocking {
        // One valid entity, one with corrupt JSON in competitionTypeJson → toDomain returns null → filtered out
        val good = datum(10).toEntity(gson)
        val bad = good.copy(id = 11, competitionTypeJson = "{not valid json")
        val dao = FakeDao(observed = listOf(good, bad))
        val repo = buildRepo(dao)

        val emitted = repo.observeAll().first()

        assertEquals(1, emitted.size)
        assertEquals(10L, emitted.first().id)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `repository still has syncAll`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncAll" }
        assertNotNull("syncAll should still exist", method)
    }
}
