package se.kjellstrand.webshooter.data.competitions

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
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

class CompetitionsRepositorySyncNonCompletedTest {

    private fun datum(id: Long, status: String, statusHuman: String = status): Datum = Datum(
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
        statusHuman = statusHuman,
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

    private fun http404(): HttpException =
        HttpException(Response.error<Any>(404, "".toResponseBody("text/plain".toMediaType())))

    private fun http500(): HttpException =
        HttpException(Response.error<Any>(500, "".toResponseBody("text/plain".toMediaType())))

    private class FakeRemoteDataSource : CompetitionsRemoteDataSource {
        val pageResponses = mutableMapOf<Int, CompetitionsResponse>()
        val byIdResponses = mutableMapOf<Long, Any>() // CompetitionByIdResponse or Throwable
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
            return pageResponses[page]
                ?: error("FakeRemoteDataSource has no response queued for page $page")
        }

        override suspend fun getCompetitionById(id: Long): CompetitionByIdResponse {
            getCompetitionByIdCalls.add(id)
            val response = byIdResponses[id]
                ?: error("FakeRemoteDataSource has no response queued for id $id")
            if (response is Throwable) throw response
            return response as CompetitionByIdResponse
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
        override suspend fun getNonCompletedIds(): List<Long> = nonCompletedIdsResult
        override suspend fun insertAll(competitions: List<CompetitionEntity>) {
            insertedBatches.add(competitions)
        }
        override suspend fun deleteAll() {}
        override suspend fun deleteById(id: Long) { deletedIds.add(id) }
    }

    private fun buildRepo(
        remote: CompetitionsRemoteDataSource = FakeRemoteDataSource(),
        dao: CompetitionsDao = FakeDao()
    ) = CompetitionsRepository(remote, dao, Gson())

    private fun insertedIds(dao: FakeDao): Set<Long> =
        dao.insertedBatches.flatten().map { it.id }.toSet()

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `syncNonCompleted method exists on repository`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncNonCompleted" }
        assertNotNull("CompetitionsRepository should have a syncNonCompleted method", method)
    }

    @Test
    fun `syncNonCompleted is a suspend fun`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncNonCompleted" }
        assertNotNull(method)
        assertTrue(
            "syncNonCompleted should be suspend (has Continuation parameter)",
            method!!.parameterTypes.any { it.name.contains("Continuation") }
        )
    }

    @Test
    fun `syncNonCompleted fetches competitions with status all`(): Unit = runBlocking {
        val remote = FakeRemoteDataSource().apply {
            pageResponses[1] = response(page = 1, lastPage = 1, total = 0, items = emptyList())
        }
        val dao = FakeDao()
        buildRepo(remote, dao).syncNonCompleted()

        assertTrue("Should call getCompetitions at least once", remote.getCompetitionsCalls.isNotEmpty())
        assertEquals(
            "All getCompetitions calls should use status=all",
            listOf("all"),
            remote.getCompetitionsCalls.map { it.third }.distinct()
        )
    }

    @Test
    fun `syncNonCompleted pages through all responses until lastPage reached`(): Unit = runBlocking {
        val remote = FakeRemoteDataSource().apply {
            pageResponses[1] = response(1, 3, 5, listOf(datum(1, "open")))
            pageResponses[2] = response(2, 3, 5, listOf(datum(2, "open"), datum(3, "completed")))
            pageResponses[3] = response(3, 3, 5, listOf(datum(4, "ongoing"), datum(5, "completed")))
        }
        val dao = FakeDao()
        buildRepo(remote, dao).syncNonCompleted()

        val pages = remote.getCompetitionsCalls.map { it.first }.toSet()
        assertTrue("Should fetch page 1", pages.contains(1))
        assertTrue("Should fetch page 2", pages.contains(2))
        assertTrue("Should fetch page 3", pages.contains(3))
        assertEquals(
            "All 5 competitions (mixed statuses) should be upserted to DAO",
            setOf(1L, 2L, 3L, 4L, 5L),
            insertedIds(dao)
        )
    }

    @Test
    fun `syncNonCompleted refetches transitioned IDs by id and upserts result`(): Unit = runBlocking {
        // Local thinks 10, 20, 30 are non-completed
        // Server now returns only 10 and 30 as non-completed (20 disappeared from non-completed view)
        // → 20 should be refetched via getCompetitionById
        val remote = FakeRemoteDataSource().apply {
            pageResponses[1] = response(1, 1, 2, listOf(
                datum(10, "open"),
                datum(30, "ongoing")
            ))
            byIdResponses[20L] = CompetitionByIdResponse(competition = datum(20, "completed"))
        }
        val dao = FakeDao().apply {
            nonCompletedIdsResult = listOf(10L, 20L, 30L)
        }
        buildRepo(remote, dao).syncNonCompleted()

        assertEquals(
            "Should refetch exactly the transitioned ID (20)",
            listOf(20L),
            remote.getCompetitionByIdCalls
        )
        assertTrue(
            "DAO should be updated with the refetched competition (id=20)",
            insertedIds(dao).contains(20L)
        )
    }

    @Test
    fun `syncNonCompleted deletes a row when getCompetitionById returns 404`(): Unit = runBlocking {
        val remote = FakeRemoteDataSource().apply {
            pageResponses[1] = response(1, 1, 0, emptyList())
            byIdResponses[42L] = http404()
        }
        val dao = FakeDao().apply {
            nonCompletedIdsResult = listOf(42L)
        }
        buildRepo(remote, dao).syncNonCompleted()

        assertEquals(
            "DAO deleteById should be called for the missing competition",
            listOf(42L),
            dao.deletedIds
        )
    }

    @Test
    fun `syncNonCompleted swallows non-404 errors from getCompetitionById`(): Unit = runBlocking {
        val remote = FakeRemoteDataSource().apply {
            pageResponses[1] = response(1, 1, 0, emptyList())
            byIdResponses[7L] = http500()
        }
        val dao = FakeDao().apply {
            nonCompletedIdsResult = listOf(7L)
        }
        // Should not throw — swallow and continue
        buildRepo(remote, dao).syncNonCompleted()

        assertEquals(
            "Should still attempt the refetch",
            listOf(7L),
            remote.getCompetitionByIdCalls
        )
        assertFalse(
            "Should NOT delete the row on a non-404 error",
            dao.deletedIds.contains(7L)
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `syncCompleted method still exists`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncCompleted" }
        assertNotNull("syncCompleted should still exist on the repository", method)
    }

    @Test
    fun `get method still exists`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "get" }
        assertNotNull("get should still exist on the repository", method)
    }

    @Test
    fun `repository can still be constructed with the standard three-arg constructor`() {
        val repo = buildRepo()
        assertNotNull(repo)
    }
}
