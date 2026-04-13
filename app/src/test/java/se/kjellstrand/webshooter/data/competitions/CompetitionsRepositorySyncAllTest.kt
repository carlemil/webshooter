package se.kjellstrand.webshooter.data.competitions

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.competitions.local.CompetitionEntity
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionByIdResponse
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

    private fun response(page: Int, lastPage: Int, total: Long, items: List<Datum>): CompetitionsResponse =
        CompetitionsResponse(
            competitions = Competitions(
                currentPage = page.toLong(),
                data = items,
                lastPage = lastPage.toLong(),
                total = total,
                status = "all",
                competitionTypes = emptyList()
            )
        )

    private class FakeRemoteDataSource : CompetitionsRemoteDataSource {
        val pageResponses = mutableMapOf<Pair<Int, String>, CompetitionsResponse>()
        val byIdResponses = mutableMapOf<Long, Any>()
        val getCompetitionsCalls = mutableListOf<Triple<Int, Int, String>>()
        val getCompetitionByIdCalls = mutableListOf<Long>()

        override suspend fun getCompetitions(
            page: Int,
            perPage: Int,
            status: String,
            type: Int,
            userSignup: Int
        ): CompetitionsResponse {
            getCompetitionsCalls.add(Triple(page, perPage, status))
            return pageResponses[page to status]
                ?: error("FakeRemoteDataSource has no response for page=$page status=$status")
        }

        override suspend fun getCompetitionById(id: Long): CompetitionByIdResponse {
            getCompetitionByIdCalls.add(id)
            val r = byIdResponses[id] ?: error("no byId response for $id")
            if (r is Throwable) throw r
            return r as CompetitionByIdResponse
        }
    }

    private class FakeDao : CompetitionsDao {
        var nonCompletedIdsResult: List<Long> = emptyList()
        var completedCount: Int = 0
        val insertedBatches = mutableListOf<List<CompetitionEntity>>()
        val deletedIds = mutableListOf<Long>()

        override suspend fun getAll(): List<CompetitionEntity> = emptyList()
        override fun observeAll(): Flow<List<CompetitionEntity>> = flowOf(emptyList())
        override suspend fun getCompletedCompetitions(): List<CompetitionEntity> = emptyList()
        override suspend fun getCompletedCount(): Int = completedCount
        override suspend fun getMaxCompletedDate(): String? = null
        override suspend fun getNonCompletedIds(): List<Long> = nonCompletedIdsResult
        override suspend fun insertAll(competitions: List<CompetitionEntity>) {
            insertedBatches.add(competitions)
        }
        override suspend fun deleteAll() {}
        override suspend fun deleteById(id: Long) { deletedIds.add(id) }
    }

    private fun emptyAllPage() = response(1, 1, 0, emptyList())
    private fun emptyCompletedPage() = response(1, 1, 0, emptyList())

    private fun buildRepo(
        remote: FakeRemoteDataSource = FakeRemoteDataSource().apply {
            pageResponses[1 to "all"] = emptyAllPage()
            pageResponses[1 to "completed"] = emptyCompletedPage()
        },
        dao: FakeDao = FakeDao()
    ): Triple<CompetitionsRepository, FakeDao, FakeRemoteDataSource> {
        val repo = CompetitionsRepository(remote, dao, Gson())
        return Triple(repo, dao, remote)
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `syncAll method exists on repository`() {
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
    fun `syncAll on first startup runs syncCompleted`(): Unit = runBlocking {
        val remote = FakeRemoteDataSource().apply {
            pageResponses[1 to "all"] = emptyAllPage()
            pageResponses[1 to "completed"] = response(1, 1, 0, listOf(datum(1, "completed")))
        }
        val dao = FakeDao().apply { completedCount = 0 } // first startup: no completed locally
        val (repo, _, _) = buildRepo(remote = remote, dao = dao)

        repo.syncAll()

        val statusesCalled = remote.getCompetitionsCalls.map { it.third }
        assertTrue(
            "syncAll should call getCompetitions with status=completed when DB is empty",
            statusesCalled.contains("completed")
        )
    }

    @Test
    fun `syncAll always runs syncNonCompleted`(): Unit = runBlocking {
        val remote = FakeRemoteDataSource().apply {
            pageResponses[1 to "all"] = emptyAllPage()
            pageResponses[1 to "completed"] = emptyCompletedPage()
        }
        val (repo, _, _) = buildRepo(remote = remote)

        repo.syncAll()

        val statusesCalled = remote.getCompetitionsCalls.map { it.third }
        assertTrue(
            "syncAll should always call getCompetitions with status=all (syncNonCompleted)",
            statusesCalled.contains("all")
        )
    }

    @Test
    fun `syncCompleted no longer short-circuits when apiTotal equals localCount`(): Unit = runBlocking {
        // Old behavior: if apiTotal <= localCount, return early without re-fetching.
        // New behavior: weekly cadence is the only skip condition; the count check is removed.
        val remote = FakeRemoteDataSource().apply {
            // Page 1 reports total = 5, matching localCount.
            // Old code would call getCompetitions(1, 1, "completed", ...) once and return.
            // New code should still call it again with pageSize=100 to fetch the actual page.
            pageResponses[1 to "completed"] = response(1, 1, 5, listOf(datum(1, "completed")))
        }
        val dao = FakeDao().apply { completedCount = 5 }
        val (repo, _, _) = buildRepo(remote = remote, dao = dao)

        repo.syncCompleted()

        // The new implementation should fetch the actual page (pageSize=100), not just probe with size=1.
        val completedCalls = remote.getCompetitionsCalls.filter { it.third == "completed" }
        assertTrue(
            "syncCompleted should fetch the full page, not just probe — calls were $completedCalls",
            completedCalls.any { it.second == 100 }
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `syncCompleted method still exists`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncCompleted" }
        assertNotNull("syncCompleted should still exist", method)
    }

    @Test
    fun `syncNonCompleted method still exists`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncNonCompleted" }
        assertNotNull("syncNonCompleted should still exist", method)
    }
}
