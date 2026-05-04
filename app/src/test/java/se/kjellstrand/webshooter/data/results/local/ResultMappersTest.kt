package se.kjellstrand.webshooter.data.results.local

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import se.kjellstrand.webshooter.data.common.ClassnameGeneral
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.Signup
import se.kjellstrand.webshooter.data.results.remote.StationResult
import se.kjellstrand.webshooter.data.results.remote.User

class ResultMappersTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private fun sampleResult(
        stations: List<StationResult> = listOf(
            StationResult(id = 1, figureHits = 1, hits = 4, points = 30),
            StationResult(id = 2, figureHits = 2, hits = 6, points = 50),
            StationResult(id = 3, figureHits = 3, hits = 5, points = 40)
        ),
        userId: Long = 42L,
        fullname: String = "Jane Doe",
        weaponClassName: String = "C"
    ) = Result(
        id = 1L,
        signupsID = 20L,
        placement = 2L,
        figureHits = 6L,
        hits = 15L,
        points = 120L,
        stdMedal = null,
        signup = Signup(
            user = User(
                name = "Jane",
                lastname = "Doe",
                userID = userId,
                fullname = fullname
            ),
            club = null
        ),
        weaponClass = WeaponClass(
            id = 3,
            classname = weaponClassName,
            classnameGeneral = ClassnameGeneral.C
        ),
        results = stations
    )

    private fun validEntity() = ResultEntity(
        id = 1L,
        competitionsId = 10L,
        signupsId = 20L,
        placement = 1L,
        figureHits = 5L,
        hits = 10L,
        points = 100L,
        stdMedal = null,
        signupJson = """{"id":1,"competitions_id":10,"patrols_id":1,"patrols_finals_id":0,"lane_finals":0,"patrols_distinguish_id":0,"lane_distinguish":0,"start_time":"","end_time":"","lane":1,"weaponclasses_id":3,"registration_fee":0,"clubs_id":1,"start_before":"","start_after":"","share_patrol_with":0,"shoot_not_simultaneously_with":0,"requires_approval":0,"is_approved_by":0,"created_by":0,"created_at":"","special_wishes":"","first_last_patrol_human":"","start_time_human":"","end_time_human":"","user":{"name":"Test","lastname":"User","api_token":"abc","user_id":1,"fullname":"Test User","clubs_id":1,"status":"active","clubs":[]}}""",
        weaponClassJson = """{"id":3,"weapongroups_id":1,"classname":"A","championship":0,"classname_general":"A"}""",
        stationResultsJson = "[]"
    )

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `toDomain returns null for corrupted signupJson`() {
        val entity = validEntity().copy(signupJson = "not valid json {{{")
        assertNull(entity.toDomain(json))
    }

    @Test
    fun `toDomain returns null for corrupted weaponClassJson`() {
        val entity = validEntity().copy(weaponClassJson = "corrupt!!!")
        assertNull(entity.toDomain(json))
    }

    @Test
    fun `toDomain returns null for corrupted stationResultsJson`() {
        val entity = validEntity().copy(stationResultsJson = "{bad}")
        assertNull(entity.toDomain(json))
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `toDomain maps valid entity correctly`() {
        val result = validEntity().toDomain(json)
        assertNotNull(result)
        assertEquals(1L, result!!.id)
        assertEquals(20L, result.signupsID)
        assertEquals(100L, result.points)
    }

    @Test
    fun `toDomain handles empty stationResults list`() {
        val result = validEntity().copy(stationResultsJson = "[]").toDomain(json)
        assertNotNull(result)
        assertTrue(result!!.results.isEmpty())
    }

    // --- Fixed behavior for toEntity denormalization (should FAIL before fix, PASS after fix) ---

    @Test
    fun `toEntity copies userId from signup user`() {
        val entity = sampleResult(userId = 99L).toEntity(competitionId = 7L, json = json)
        assertEquals(99L, entity.userId)
    }

    @Test
    fun `toEntity copies userFullname from signup user`() {
        val entity = sampleResult(fullname = "Alice Tester").toEntity(7L, json)
        assertEquals("Alice Tester", entity.userFullname)
    }

    @Test
    fun `toEntity copies weaponClassName from weaponClass`() {
        val entity = sampleResult(weaponClassName = "B").toEntity(7L, json)
        assertEquals("B", entity.weaponClassName)
    }

    @Test
    fun `toEntity computes averagePoints from station results`() {
        // points: 30, 50, 40 -> average 40.0
        val entity = sampleResult().toEntity(7L, json)
        assertEquals(40.0, entity.averagePoints, 0.0001)
    }

    @Test
    fun `toEntity computes averageHits from station results`() {
        // hits: 4, 6, 5 -> average 5.0
        val entity = sampleResult().toEntity(7L, json)
        assertEquals(5.0, entity.averageHits, 0.0001)
    }

    @Test
    fun `toEntity returns zero averages when station results empty`() {
        val entity = sampleResult(stations = emptyList()).toEntity(7L, json)
        assertEquals(0.0, entity.averagePoints, 0.0001)
        assertEquals(0.0, entity.averageHits, 0.0001)
    }

    // --- Guard tests for toEntity (should PASS before and after fix) ---

    @Test
    fun `toEntity preserves competitionId and core scalar fields`() {
        val entity = sampleResult().toEntity(competitionId = 77L, json = json)
        assertEquals(77L, entity.competitionsId)
        assertEquals(1L, entity.id)
        assertEquals(20L, entity.signupsId)
        assertEquals(2L, entity.placement)
        assertEquals(120L, entity.points)
    }

    @Test
    fun `toEntity still serializes signup weaponClass and stationResults to JSON`() {
        val entity = sampleResult().toEntity(7L, json)
        assertTrue(entity.signupJson.contains("Jane Doe"))
        assertTrue(entity.weaponClassJson.contains("classname"))
        assertTrue(entity.stationResultsJson.startsWith("["))
    }
}
