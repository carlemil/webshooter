package se.kjellstrand.webshooter.data.competitions

import kotlinx.serialization.json.Json
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.Test
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.competitions.local.CompetitionEntity
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.local.toEntity
import se.kjellstrand.webshooter.data.competitions.remote.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsResponse
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.competitions.testing.NoOpResultsDao
import se.kjellstrand.webshooter.data.competitions.testing.NoOpResultsRepository
import se.kjellstrand.webshooter.data.competitions.testing.resultEntity

class CompetitionsRepositorySyncAllTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private fun datum(
        id: Long,
        status: String,
        signupsCount: Long = 0,
        date: String = PAST_DATE
    ): Datum = Datum(
        id = id,
        name = "Comp $id",
        allowTeams = 0,
        lat = 0.0,
        lng = 0.0,
        description = "",
        resultsType = ResultsType.PRECISION,
        date = date,
        signupsOpeningDate = "2025-12-01",
        signupsClosingDate = "2025-12-15",
        weaponGroups = emptyList(),
        signupsCount = signupsCount,
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

    private companion object {
        // Stable far-past / far-future dates so wall-clock drift can't flip results.
        const val PAST_DATE = "2024-01-01"
        const val FUTURE_DATE = "2099-12-31"
    }

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
        val pageThrows = mutableMapOf<Int, () -> Nothing>()
        val getCompetitionsCalls = mutableListOf<Triple<Int, Int, String>>()

        override suspend fun getCompetitions(
            page: Int,
            perPage: Int,
            status: String,
            type: Int,
            userSignup: Int
        ): CompetitionsResponse {
            getCompetitionsCalls.add(Triple(page, perPage, status))
            pageThrows[page]?.invoke()
            return pageResponses[page]
                ?: error("FakeRemoteDataSource has no response for page=$page")
        }
    }

    private class FakeDao(
        seeded: List<CompetitionEntity> = emptyList()
    ) : CompetitionsDao {
        val rows = seeded.associateBy { it.id }.toMutableMap()
        val insertedBatches = mutableListOf<List<CompetitionEntity>>()
        val deletedIdBatches = mutableListOf<List<Long>>()

        override fun observeAll(): Flow<List<CompetitionEntity>> = flowOf(rows.values.toList())
        override suspend fun getCompletedCompetitions(today: String): List<CompetitionEntity> = emptyList()
        override suspend fun getAll(): List<CompetitionEntity> = rows.values.toList()
        override suspend fun insertAll(competitions: List<CompetitionEntity>) {
            insertedBatches.add(competitions)
            competitions.forEach { rows[it.id] = it }
        }
        override suspend fun deleteByIds(ids: List<Long>) {
            deletedIdBatches.add(ids)
            ids.forEach { rows.remove(it) }
        }
        override suspend fun setNoResults(id: Long, value: Boolean) {
            rows[id]?.let { rows[id] = it.copy(noResults = value) }
        }
    }

    private data class Rig(
        val repo: CompetitionsRepository,
        val dao: FakeDao,
        val remote: FakeRemoteDataSource,
        val resultsRepo: NoOpResultsRepository,
        val resultsDao: NoOpResultsDao
    )

    private fun buildRig(
        seeded: List<CompetitionEntity> = emptyList(),
        pages: Map<Int, CompetitionsResponse> = mapOf(1 to response(1, 1, emptyList())),
        pageThrows: Map<Int, () -> Nothing> = emptyMap(),
        seededResultsByCompetitionId: Map<Long, List<Long>> = emptyMap()
    ): Rig {
        val remote = FakeRemoteDataSource().apply {
            pageResponses.putAll(pages)
            this.pageThrows.putAll(pageThrows)
        }
        val dao = FakeDao(seeded = seeded)
        val resultsDao = NoOpResultsDao()
        seededResultsByCompetitionId.forEach { (cid, resultIds) ->
            resultsDao.seed(cid, resultIds.map { resultEntity(it, cid) })
        }
        val resultsRepo = NoOpResultsRepository()
        val repo = CompetitionsRepository(remote, dao, json, resultsRepo, resultsDao)
        return Rig(repo, dao, remote, resultsRepo, resultsDao)
    }

    private fun insertedIds(dao: FakeDao): Set<Long> =
        dao.insertedBatches.flatten().map { it.id }.toSet()

    // --- Existing behavior (regression guards) ---
    //
    // The previous `syncAll exists` and `syncAll is a suspend fun` reflection
    // tests were dropped during the commonTest migration — every behavioural
    // test below calls `repo.syncAll()` from inside a `runTest { … }` block,
    // which is a stronger compile-time check than the runtime reflection
    // ever was.

    @Test
    fun `syncAll calls getCompetitions with status=all`()= runTest {
        val rig = buildRig()

        rig.repo.syncAll()

        assertTrue( rig.remote.getCompetitionsCalls.isNotEmpty(), "Should call getCompetitions at least once")
        assertEquals(
            listOf("all"),
            rig.remote.getCompetitionsCalls.map { it.third }.distinct()
        ,            "All getCompetitions calls should use status=all")
    }

    @Test
    fun `syncAll pages through all responses until lastPage`()= runTest {
        val rig = buildRig(
            pages = mapOf(
                1 to response(1, 3, listOf(datum(1, "open"))),
                2 to response(2, 3, listOf(datum(2, "completed"))),
                3 to response(3, 3, listOf(datum(3, "upcoming")))
            )
        )

        rig.repo.syncAll()

        val pages = rig.remote.getCompetitionsCalls.map { it.first }.toSet()
        assertEquals( setOf(1, 2, 3), pages, "Should fetch pages 1..3")
        assertEquals(
            setOf(1L, 2L, 3L),
            insertedIds(rig.dao)
        ,            "All three competitions should be upserted")
    }

    @Test
    fun `syncAll returns cleanly when the remote call throws`()= runTest {
        val rig = buildRig(
            pages = emptyMap(),
            pageThrows = mapOf(1 to { error("boom") })
        )

        rig.repo.syncAll()

        assertTrue( rig.dao.insertedBatches.isEmpty(), "No rows should be inserted when the network fails")
    }

    // --- Change-detection: hash diff triggers eager refetch ---

    @Test
    fun `syncAll triggers refreshResultsFor when a cached competition has changed`()= runTest {
        val cachedOpen = datum(1, "open").toEntity(json)
        val rig = buildRig(
            seeded = listOf(cachedOpen),
            pages = mapOf(1 to response(1, 1, listOf(datum(1, "completed"))))
        )

        rig.repo.syncAll()

        assertEquals(listOf(1L), rig.resultsRepo.refreshCalls)
    }

    @Test
    fun `syncAll does not refresh when cached row is identical AND results are present`()= runTest {
        val cached = datum(1, "completed").toEntity(json)
        val rig = buildRig(
            seeded = listOf(cached),
            pages = mapOf(1 to response(1, 1, listOf(datum(1, "completed")))),
            seededResultsByCompetitionId = mapOf(1L to listOf(101L))
        )

        rig.repo.syncAll()

        assertTrue(
            rig.resultsRepo.refreshCalls.isEmpty()
        ,            "No refresh should occur when content hash is identical and results are cached, got ${rig.resultsRepo.refreshCalls}")
    }

    // --- Missing-results sync (past competitions only) ---

    @Test
    fun `syncAll fetches results for a brand-new past competition`()= runTest {
        val rig = buildRig(
            seeded = emptyList(),
            pages = mapOf(1 to response(1, 1, listOf(datum(1, "completed", date = PAST_DATE))))
        )

        rig.repo.syncAll()

        assertEquals(
            listOf(1L),
            rig.resultsRepo.refreshCalls
        ,            "Brand-new past competitions must have their results fetched eagerly")
    }

    @Test
    fun `syncAll fetches results for an unchanged past competition that has no cached results`()= runTest {
        val cached = datum(1, "completed").toEntity(json)
        val rig = buildRig(
            seeded = listOf(cached),
            pages = mapOf(1 to response(1, 1, listOf(datum(1, "completed")))),
            seededResultsByCompetitionId = emptyMap()
        )

        rig.repo.syncAll()

        assertEquals(
            listOf(1L),
            rig.resultsRepo.refreshCalls
        ,            "Missing results must trigger a fetch even when the row hasn't changed")
    }

    // --- Future competitions are skipped (no results yet) ---

    @Test
    fun `syncAll does not fetch results for a brand-new future competition`()= runTest {
        val rig = buildRig(
            seeded = emptyList(),
            pages = mapOf(1 to response(1, 1, listOf(datum(1, "open", date = FUTURE_DATE))))
        )

        rig.repo.syncAll()

        assertTrue(
            rig.resultsRepo.refreshCalls.isEmpty()
        ,            "Future competitions have no results yet, must not trigger a fetch, got ${rig.resultsRepo.refreshCalls}")
        assertTrue(
            insertedIds(rig.dao).contains(1L)
        ,            "Future competition row should still be upserted")
    }

    @Test
    fun `syncAll does not fetch results when a future competition's content changes`()= runTest {
        val before = datum(1, "open", signupsCount = 10, date = FUTURE_DATE).toEntity(json)
        val rig = buildRig(
            seeded = listOf(before),
            pages = mapOf(1 to response(1, 1, listOf(datum(1, "open", signupsCount = 11, date = FUTURE_DATE))))
        )

        rig.repo.syncAll()

        assertTrue(
            rig.resultsRepo.refreshCalls.isEmpty()
        ,            "Future-competition content changes must not trigger a results fetch, got ${rig.resultsRepo.refreshCalls}")
        val upsertedSignupsCount = rig.dao.insertedBatches.flatten().firstOrNull { it.id == 1L }?.signupsCount
        assertEquals(
            11L,
            upsertedSignupsCount
        ,            "The new signupsCount must still land in the competitions table")
    }

    @Test
    fun `syncAll detects a change in signupsCount`()= runTest {
        val before = datum(1, "open", signupsCount = 10).toEntity(json)
        val rig = buildRig(
            seeded = listOf(before),
            pages = mapOf(1 to response(1, 1, listOf(datum(1, "open", signupsCount = 11))))
        )

        rig.repo.syncAll()

        assertEquals(listOf(1L), rig.resultsRepo.refreshCalls)
    }

    // --- Pagination failure must NOT invalidate, refetch, or delete ---

    @Test
    fun `syncAll does not refresh when pagination fails after a detected change`()= runTest {
        val cached = datum(1, "open").toEntity(json)
        val rig = buildRig(
            seeded = listOf(cached),
            pages = mapOf(
                1 to response(1, 2, listOf(datum(1, "completed")))
            ),
            pageThrows = mapOf(2 to { error("boom") })
        )

        rig.repo.syncAll()

        assertTrue(
            rig.resultsRepo.refreshCalls.isEmpty()
        ,            "Eager refetch must not run on partial pagination, got ${rig.resultsRepo.refreshCalls}")
    }

    @Test
    fun `syncAll preserves rows that look deleted-upstream when pagination fails`()= runTest {
        val cachedKept = datum(1, "open").toEntity(json)
        val cachedOnlyInDb = datum(2, "open").toEntity(json)
        val rig = buildRig(
            seeded = listOf(cachedKept, cachedOnlyInDb),
            pages = mapOf(1 to response(1, 2, listOf(datum(1, "open")))),
            pageThrows = mapOf(2 to { error("boom") }),
            seededResultsByCompetitionId = mapOf(2L to listOf(101L))
        )

        rig.repo.syncAll()

        assertTrue(
            rig.dao.deletedIdBatches.isEmpty()
        ,            "Must not delete competitions on partial sync, got ${rig.dao.deletedIdBatches}")
        assertTrue(
            rig.resultsDao.deletedCompetitionIds.isEmpty()
        ,            "Must not delete cached results on partial sync, got ${rig.resultsDao.deletedCompetitionIds}")
    }

    // --- Deleted-upstream cleanup ---

    @Test
    fun `syncAll purges competitions and their results when they disappear from the API`()= runTest {
        val keep = datum(1, "completed").toEntity(json)
        val gone = datum(2, "completed").toEntity(json)
        val rig = buildRig(
            seeded = listOf(keep, gone),
            pages = mapOf(1 to response(1, 1, listOf(datum(1, "completed")))),
            seededResultsByCompetitionId = mapOf(1L to listOf(11L), 2L to listOf(21L, 22L))
        )

        rig.repo.syncAll()

        assertEquals(
            listOf(2L),
            rig.resultsDao.deletedCompetitionIds
        ,            "Cached results for the deleted competition must be removed")
        val deletedFromCompetitions = rig.dao.deletedIdBatches.flatten().toSet()
        assertEquals(
            setOf(2L),
            deletedFromCompetitions
        ,            "The deleted competition row must be purged")
    }

    // --- Bounded concurrency ---

    @Test
    fun `syncAll caps eager refresh concurrency at 4`()= runTest {
        val seeded = (1..20L).map { datum(it, "open").toEntity(json) }
        val pageItems = (1..20L).map { datum(it, "completed") }
        val resultsRepo = object : NoOpResultsRepository() {
            override suspend fun refreshResultsFor(competitionId: Long): RefreshOutcome {
                // Trigger super to record concurrency tracking, but also yield a bit to
                // give other coroutines a chance to overlap.
                val outcome = super.refreshResultsFor(competitionId)
                delay(5)
                return outcome
            }
        }

        val remote = FakeRemoteDataSource().apply {
            pageResponses[1] = response(1, 1, pageItems)
        }
        val dao = FakeDao(seeded = seeded)
        val resultsDao = NoOpResultsDao()
        val repo = CompetitionsRepository(remote, dao, json, resultsRepo, resultsDao)

        repo.syncAll()

        assertEquals( 20, resultsRepo.refreshCalls.size, "All changed competitions should be refreshed")
        assertTrue(
            resultsRepo.maxConcurrency in 1..4
        ,            "Concurrency must not exceed 4, observed ${resultsRepo.maxConcurrency}")
    }
}
