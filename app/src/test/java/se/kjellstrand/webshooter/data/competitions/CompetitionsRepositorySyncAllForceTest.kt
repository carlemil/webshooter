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

class CompetitionsRepositorySyncAllForceTest {

    private fun datum(id: Long, status: String, date: String = "2026-01-01"): Datum = Datum(
        id = id,
        name = "Comp $id",
        allowTeams = 0,
        lat = 0.0,
        lng = 0.0,
        description = "",
        resultsType = ResultsType.PRECISION,
        date = date,
        signupsOpeningDate = "",
        signupsClosingDate = "",
        weaponGroups = emptyList(),
        signupsCount = 0,
        patrolsCount = 0,
        status = status,
        statusHuman = status,
        startTimeHuman = "",
        finalTimeHuman = "",
        allowSignupsAfterClosingDateHuman = "",
        resultsTypeHuman = "Precision",
        competitionType = CompetitionType(id = 1, name = "Precision"),
        weaponClasses = emptyList(),
        userSignups = emptyList(),
        club = Club(id = 1, name = "Club")
    )

    private fun response(page: Int, lastPage: Int, items: List<Datum>, status: String): CompetitionsResponse =
        CompetitionsResponse(
            competitions = Competitions(
                currentPage = page.toLong(),
                data = items,
                lastPage = lastPage.toLong(),
                total = items.size.toLong(),
                status = status,
                competitionTypes = emptyList()
            )
        )

    private class FakeRemote : CompetitionsRemoteDataSource {
        val pageResponses = mutableMapOf<Pair<Int, String>, CompetitionsResponse>()
        val getCompetitionsCalls = mutableListOf<Triple<Int, Int, String>>()

        override suspend fun getCompetitions(
            page: Int, perPage: Int, status: String, type: Int, userSignup: Int
        ): CompetitionsResponse {
            getCompetitionsCalls.add(Triple(page, perPage, status))
            return pageResponses[page to status]
                ?: error("no response for page=$page status=$status")
        }

        override suspend fun getCompetitionById(id: Long): CompetitionByIdResponse =
            error("not used")
    }

    private class FakeDao(
        var maxCompletedDateResult: String? = null,
        var completedCountValue: Int = 0
    ) : CompetitionsDao {
        val insertedBatches = mutableListOf<List<CompetitionEntity>>()
        override suspend fun getAll(): List<CompetitionEntity> = emptyList()
        override fun observeAll(): Flow<List<CompetitionEntity>> = flowOf(emptyList())
        override suspend fun getCompletedCompetitions(): List<CompetitionEntity> = emptyList()
        override suspend fun getCompletedCount(): Int = completedCountValue
        override suspend fun getMaxCompletedDate(): String? = maxCompletedDateResult
        override suspend fun getNonCompletedIds(): List<Long> = emptyList()
        override suspend fun insertAll(competitions: List<CompetitionEntity>) {
            insertedBatches.add(competitions)
        }
        override suspend fun deleteAll() {}
        override suspend fun deleteById(id: Long) {}
    }

    private fun buildRepo(
        remote: FakeRemote = FakeRemote(),
        dao: FakeDao = FakeDao()
    ): CompetitionsRepository =
        CompetitionsRepository(remote, dao, Gson())

    private fun emptyAllPage() = response(1, 1, emptyList(), "all")
    private fun emptyCompletedPage() = response(1, 1, emptyList(), "completed")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `syncAll accepts a boolean force parameter`() {
        val methods = CompetitionsRepository::class.java.methods.filter { it.name == "syncAll" }
        assertTrue(
            "CompetitionsRepository.syncAll should accept a Boolean force parameter",
            methods.any { m ->
                m.parameterTypes.any { p ->
                    p == java.lang.Boolean.TYPE || p == java.lang.Boolean::class.javaObjectType
                }
            }
        )
    }

    @Test
    fun `syncAll runs syncCompleted even when db already has completed rows`(): Unit = runBlocking {
        // Pre-WEEK_MS-removal: weekly gate skipped syncCompleted when DB had completed rows
        // and last sync was recent. Post-removal: syncCompleted runs every time (incremental is cheap).
        val remote = FakeRemote().apply {
            pageResponses[1 to "all"] = emptyAllPage()
            pageResponses[1 to "completed"] = emptyCompletedPage()
        }
        val dao = FakeDao(completedCountValue = 50) // already populated

        val repo = buildRepo(remote = remote, dao = dao)
        repo.syncAll()

        val statusesCalled = remote.getCompetitionsCalls.map { it.third }
        assertTrue(
            "syncAll should ALWAYS call status=completed (no weekly gate). Statuses called = $statusesCalled",
            statusesCalled.contains("completed")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `syncAll method still exists`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncAll" }
        assertNotNull("CompetitionsRepository should still have syncAll", method)
    }

    @Test
    fun `syncAll is a suspend fun`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncAll" }
        assertNotNull(method)
        assertTrue(
            "syncAll should be a suspend fun (has a Continuation parameter)",
            method!!.parameterTypes.any { it.name.contains("Continuation") }
        )
    }

    @Test
    fun `syncAll still calls syncNonCompleted`(): Unit = runBlocking {
        val remote = FakeRemote().apply {
            pageResponses[1 to "all"] = emptyAllPage()
            pageResponses[1 to "completed"] = emptyCompletedPage()
        }
        val repo = buildRepo(remote = remote)

        repo.syncAll()

        val statusesCalled = remote.getCompetitionsCalls.map { it.third }
        assertTrue(
            "syncAll should still call status=all (syncNonCompleted). Statuses called = $statusesCalled",
            statusesCalled.contains("all")
        )
    }
}
