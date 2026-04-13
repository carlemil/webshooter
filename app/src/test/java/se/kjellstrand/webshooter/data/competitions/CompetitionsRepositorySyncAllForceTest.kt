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

    private class FakeSharedPreferences : SharedPreferences {
        private val store = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = store.toMutableMap()
        override fun getString(key: String?, defValue: String?): String? = (store[key] as? String) ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            @Suppress("UNCHECKED_CAST") (store[key] as? MutableSet<String>) ?: defValues
        override fun getInt(key: String?, defValue: Int): Int = (store[key] as? Int) ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = (store[key] as? Long) ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = (store[key] as? Float) ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = (store[key] as? Boolean) ?: defValue
        override fun contains(key: String?): Boolean = store.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        override fun edit(): SharedPreferences.Editor = FakeEditor()

        private inner class FakeEditor : SharedPreferences.Editor {
            override fun putString(key: String?, value: String?): SharedPreferences.Editor { store[key!!] = value; return this }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor { store[key!!] = values; return this }
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor { store[key!!] = value; return this }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor { store[key!!] = value; return this }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor { store[key!!] = value; return this }
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { store[key!!] = value; return this }
            override fun remove(key: String?): SharedPreferences.Editor { store.remove(key); return this }
            override fun clear(): SharedPreferences.Editor { store.clear(); return this }
            override fun apply() {}
            override fun commit(): Boolean = true
        }
    }

    private fun buildRepo(
        remote: FakeRemote = FakeRemote(),
        dao: FakeDao = FakeDao(),
        prefs: SharedPreferences = FakeSharedPreferences()
    ): CompetitionsRepository =
        CompetitionsRepository(remote, dao, Gson(), SyncPreferences(prefs))

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
    fun `syncAll always runs syncCompleted even with recent timestamp and non-empty db`(): Unit = runBlocking {
        // Pre-fix: weekly gate skips syncCompleted when DB has completed rows AND last sync was recent.
        // Post-fix: weekly gate is removed; syncCompleted runs every time (incremental is cheap).
        val prefs = FakeSharedPreferences()
        prefs.edit().putLong("last_completed_full_sync_ms", System.currentTimeMillis()).commit() // just now
        val remote = FakeRemote().apply {
            pageResponses[1 to "all"] = emptyAllPage()
            pageResponses[1 to "completed"] = emptyCompletedPage()
        }
        val dao = FakeDao(completedCountValue = 50) // already populated, gate would skip pre-fix

        val repo = buildRepo(remote = remote, dao = dao, prefs = prefs)
        repo.syncAll()

        val statusesCalled = remote.getCompetitionsCalls.map { it.third }
        assertTrue(
            "syncAll should ALWAYS call status=completed (no weekly gate). Statuses called = $statusesCalled",
            statusesCalled.contains("completed")
        )
    }

    @Test
    fun `syncAll no longer writes last_completed_full_sync_ms`(): Unit = runBlocking {
        // Pre-fix: first-boot path writes the timestamp via syncPreferences.setLastCompletedFullSyncMs.
        // Post-fix: that write is removed entirely.
        val prefs = FakeSharedPreferences()
        val remote = FakeRemote().apply {
            pageResponses[1 to "all"] = emptyAllPage()
            pageResponses[1 to "completed"] = emptyCompletedPage()
        }
        val dao = FakeDao(completedCountValue = 0) // first-boot path

        val repo = buildRepo(remote = remote, dao = dao, prefs = prefs)
        repo.syncAll()

        val stored = prefs.getLong("last_completed_full_sync_ms", 0L)
        assertEquals(
            "syncAll should not write last_completed_full_sync_ms anymore",
            0L,
            stored
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
