package se.kjellstrand.webshooter.data.competitions

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
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
import se.kjellstrand.webshooter.data.competitions.remote.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsResponse
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType

class CompetitionsRepositorySyncAllTest {

    private fun datum(id: Long, status: String): Datum = Datum(
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
        status = status,
        statusHuman = status,
        startTimeHuman = "08:00",
        finalTimeHuman = "17:00",
        allowSignupsAfterClosingDateHuman = "",
        resultsTypeHuman = "Precision",
        competitionType = CompetitionType(id = 1, name = "Precision"),
        weaponClasses = emptyList(),
        userSignups = emptyList(),
        club = Club(id = 1, name = "Club")
    )

    private fun response(page: Int, lastPage: Int, items: List<Datum>): CompetitionsResponse =
        CompetitionsResponse(
            competitions = Competitions(
                currentPage = page.toLong(),
                data = items,
                lastPage = lastPage.toLong(),
                total = items.size.toLong(),
                status = "all",
                competitionTypes = emptyList()
            )
        )

    private class FakeRemoteDataSource : CompetitionsRemoteDataSource {
        val pageResponses = mutableMapOf<Int, CompetitionsResponse>()
        val getCompetitionsCalls = mutableListOf<Triple<Int, Int, String>>()

        override suspend fun getCompetitions(
            page: Int,
            perPage: Int,
            status: String,
            type: Int,
            userSignup: Int
        ): CompetitionsResponse {
            getCompetitionsCalls.add(Triple(page, perPage, status))
            return pageResponses[page]
                ?: error("FakeRemoteDataSource has no response for page=$page")
        }
    }

    private class FakeDao : CompetitionsDao {
        val insertedBatches = mutableListOf<List<CompetitionEntity>>()

        override fun observeAll(): Flow<List<CompetitionEntity>> = flowOf(emptyList())
        override suspend fun getCompletedCompetitions(): List<CompetitionEntity> = emptyList()
        override suspend fun insertAll(competitions: List<CompetitionEntity>) {
            insertedBatches.add(competitions)
        }
    }

    private fun buildRepo(
        remote: FakeRemoteDataSource = FakeRemoteDataSource().apply {
            pageResponses[1] = response(1, 1, emptyList())
        },
        dao: FakeDao = FakeDao()
    ): Triple<CompetitionsRepository, FakeDao, FakeRemoteDataSource> {
        val repo = CompetitionsRepository(remote, dao, Gson())
        return Triple(repo, dao, remote)
    }

    private fun insertedIds(dao: FakeDao): Set<Long> =
        dao.insertedBatches.flatten().map { it.id }.toSet()

    // --- Fixed behavior ---

    @Test
    fun `syncAll exists on repository`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncAll" }
        assertNotNull("CompetitionsRepository should have syncAll", method)
    }

    @Test
    fun `syncAll is a suspend fun`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncAll" }
        assertNotNull(method)
        assertTrue(
            "syncAll should be suspend",
            method!!.parameterTypes.any { it.name.contains("Continuation") }
        )
    }

    @Test
    fun `syncAll calls getCompetitions with status=all`(): Unit = runBlocking {
        val remote = FakeRemoteDataSource().apply {
            pageResponses[1] = response(1, 1, emptyList())
        }
        val (repo, _, _) = buildRepo(remote = remote)

        repo.syncAll()

        assertTrue("Should call getCompetitions at least once", remote.getCompetitionsCalls.isNotEmpty())
        assertEquals(
            "All getCompetitions calls should use status=all",
            listOf("all"),
            remote.getCompetitionsCalls.map { it.third }.distinct()
        )
    }

    @Test
    fun `syncAll pages through all responses until lastPage`(): Unit = runBlocking {
        val remote = FakeRemoteDataSource().apply {
            pageResponses[1] = response(1, 3, listOf(datum(1, "open")))
            pageResponses[2] = response(2, 3, listOf(datum(2, "completed")))
            pageResponses[3] = response(3, 3, listOf(datum(3, "upcoming")))
        }
        val dao = FakeDao()
        val (repo, _, _) = buildRepo(remote = remote, dao = dao)

        repo.syncAll()

        val pages = remote.getCompetitionsCalls.map { it.first }.toSet()
        assertEquals("Should fetch pages 1..3", setOf(1, 2, 3), pages)
        assertEquals(
            "All three competitions should be upserted",
            setOf(1L, 2L, 3L),
            insertedIds(dao)
        )
    }

    @Test
    fun `syncAll returns cleanly when the remote call throws`(): Unit = runBlocking {
        val remote = object : CompetitionsRemoteDataSource {
            override suspend fun getCompetitions(
                page: Int, perPage: Int, status: String, type: Int, userSignup: Int
            ): CompetitionsResponse = error("boom")
        }
        val dao = FakeDao()
        val repo = CompetitionsRepository(remote, dao, Gson())

        // Should not throw — syncAll swallows and logs.
        repo.syncAll()

        assertTrue("No rows should be inserted when the network fails", dao.insertedBatches.isEmpty())
    }
}
