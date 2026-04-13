package se.kjellstrand.webshooter.data.competitions

import android.content.SharedPreferences
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
import se.kjellstrand.webshooter.data.competitions.local.SyncPreferences
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionByIdResponse
import se.kjellstrand.webshooter.data.competitions.remote.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsResponse
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType

class CompetitionsRepositorySyncCompletedIncrementalTest {

    private fun datum(id: Long, date: String): Datum = Datum(
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
        status = "completed",
        statusHuman = "completed",
        startTimeHuman = "",
        finalTimeHuman = "",
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
                status = "completed",
                competitionTypes = emptyList()
            )
        )

    private class FakeRemote : CompetitionsRemoteDataSource {
        val pageResponses = mutableMapOf<Int, CompetitionsResponse>()
        val getCompetitionsCalls = mutableListOf<Triple<Int, Int, String>>()

        override suspend fun getCompetitions(
            page: Int, perPage: Int, status: String, type: Int, userSignup: Int
        ): CompetitionsResponse {
            getCompetitionsCalls.add(Triple(page, perPage, status))
            return pageResponses[page] ?: error("no response for page=$page")
        }

        override suspend fun getCompetitionById(id: Long): CompetitionByIdResponse =
            error("not used")
    }

    private class FakeDao(
        var maxCompletedDateResult: String? = null
    ) : CompetitionsDao {
        val insertedBatches = mutableListOf<List<CompetitionEntity>>()
        override suspend fun getAll(): List<CompetitionEntity> = emptyList()
        override fun observeAll(): Flow<List<CompetitionEntity>> = flowOf(emptyList())
        override suspend fun getCompletedCompetitions(): List<CompetitionEntity> = emptyList()
        override suspend fun getCompletedCount(): Int = 0
        override suspend fun getMaxCompletedDate(): String? = maxCompletedDateResult
        override suspend fun getNonCompletedIds(): List<Long> = emptyList()
        override suspend fun insertAll(competitions: List<CompetitionEntity>) {
            insertedBatches.add(competitions)
        }
        override suspend fun deleteAll() {}
        override suspend fun deleteById(id: Long) {}
    }

    private class NoopSharedPreferences : SharedPreferences {
        override fun getAll(): MutableMap<String, *> = mutableMapOf<String, Any>()
        override fun getString(key: String?, defValue: String?): String? = defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
        override fun getInt(key: String?, defValue: Int): Int = defValue
        override fun getLong(key: String?, defValue: Long): Long = defValue
        override fun getFloat(key: String?, defValue: Float): Float = defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue
        override fun contains(key: String?): Boolean = false
        override fun edit(): SharedPreferences.Editor = NoopEditor()
        override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        private class NoopEditor : SharedPreferences.Editor {
            override fun putString(key: String?, value: String?): SharedPreferences.Editor = this
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this
            override fun remove(key: String?): SharedPreferences.Editor = this
            override fun clear(): SharedPreferences.Editor = this
            override fun apply() {}
            override fun commit(): Boolean = true
        }
    }

    private fun buildRepo(
        remote: FakeRemote = FakeRemote(),
        dao: FakeDao = FakeDao()
    ): CompetitionsRepository =
        CompetitionsRepository(remote, dao, Gson(), SyncPreferences(NoopSharedPreferences()))

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `syncCompleted accepts a boolean force parameter`() {
        val methods = CompetitionsRepository::class.java.methods.filter { it.name == "syncCompleted" }
        assertTrue(
            "CompetitionsRepository.syncCompleted should accept a Boolean force parameter",
            methods.any { m ->
                m.parameterTypes.any { p ->
                    p == java.lang.Boolean.TYPE || p == java.lang.Boolean::class.javaObjectType
                }
            }
        )
    }

    @Test
    fun `syncCompleted stops after page 1 when page minDate is older or equal to cursor`(): Unit = runBlocking {
        val remote = FakeRemote().apply {
            pageResponses[1] = response(1, 3, listOf(
                datum(1, "2026-07-01"),
                datum(2, "2026-06-10")
            ))
            // Provide pages 2 and 3 too so the old code doesn't crash — we want a clean assertion failure
            pageResponses[2] = response(2, 3, listOf(datum(3, "2026-05-01")))
            pageResponses[3] = response(3, 3, listOf(datum(4, "2026-04-01")))
        }
        val dao = FakeDao(maxCompletedDateResult = "2026-06-15")
        val repo = buildRepo(remote = remote, dao = dao)

        repo.syncCompleted()

        val pagesFetched = remote.getCompetitionsCalls.map { it.first }
        assertEquals(
            "With cursor 2026-06-15 and page-1 minDate 2026-06-10, should stop after page 1. Actual pages = $pagesFetched",
            listOf(1),
            pagesFetched
        )
    }

    @Test
    fun `syncCompleted keeps paginating while page minDate is newer than cursor`(): Unit = runBlocking {
        val remote = FakeRemote().apply {
            // Page 1: all dates > cursor (keep going)
            pageResponses[1] = response(1, 3, listOf(
                datum(1, "2026-07-01"),
                datum(2, "2026-06-20")
            ))
            // Page 2: minDate <= cursor (stop)
            pageResponses[2] = response(2, 3, listOf(
                datum(3, "2026-06-16"),
                datum(4, "2026-06-10")
            ))
            // Page 3 provided as a safety net so old code can still run to completion
            pageResponses[3] = response(3, 3, listOf(datum(5, "2026-05-01")))
        }
        val dao = FakeDao(maxCompletedDateResult = "2026-06-15")
        val repo = buildRepo(remote = remote, dao = dao)

        repo.syncCompleted()

        val pagesFetched = remote.getCompetitionsCalls.map { it.first }
        assertEquals(
            "Should fetch page 1 (all newer) and page 2 (hit cursor) then stop. Actual pages = $pagesFetched",
            listOf(1, 2),
            pagesFetched
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `syncCompleted method still exists`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncCompleted" }
        assertNotNull("CompetitionsRepository should still have syncCompleted", method)
    }

    @Test
    fun `syncCompleted with null cursor fetches all pages (first-boot path)`(): Unit = runBlocking {
        val remote = FakeRemote().apply {
            pageResponses[1] = response(1, 3, listOf(datum(1, "2026-07-01")))
            pageResponses[2] = response(2, 3, listOf(datum(2, "2026-06-01")))
            pageResponses[3] = response(3, 3, listOf(datum(3, "2026-05-01")))
        }
        val dao = FakeDao(maxCompletedDateResult = null)
        val repo = buildRepo(remote = remote, dao = dao)

        repo.syncCompleted()

        val pagesFetched = remote.getCompetitionsCalls.map { it.first }
        assertEquals(
            "With null cursor, should fetch all pages. Actual pages = $pagesFetched",
            listOf(1, 2, 3),
            pagesFetched
        )
    }

    @Test
    fun `syncCompleted upserts each fetched page into dao`(): Unit = runBlocking {
        val remote = FakeRemote().apply {
            pageResponses[1] = response(1, 1, listOf(
                datum(1, "2026-07-01"),
                datum(2, "2026-06-20")
            ))
        }
        val dao = FakeDao(maxCompletedDateResult = null)
        val repo = buildRepo(remote = remote, dao = dao)

        repo.syncCompleted()

        assertEquals("Should insert one batch", 1, dao.insertedBatches.size)
        assertEquals("Batch should contain 2 items", 2, dao.insertedBatches[0].size)
    }

    @Test
    fun `syncCompleted uses pageSize 100`(): Unit = runBlocking {
        val remote = FakeRemote().apply {
            pageResponses[1] = response(1, 1, listOf(datum(1, "2026-07-01")))
        }
        val repo = buildRepo(remote = remote)

        repo.syncCompleted()

        val perPageValues = remote.getCompetitionsCalls.map { it.second }.distinct()
        assertEquals(
            "syncCompleted should call remote with pageSize=100",
            listOf(100),
            perPageValues
        )
    }
}
