package se.kjellstrand.webshooter.data.results.local

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class ResultMappersTest {

    private val gson = Gson()

    private fun validEntity() = ResultEntity(
        id = 1L,
        competitionsId = 10L,
        signupsId = 20L,
        placement = 1L,
        figureHits = 5L,
        hits = 10L,
        points = 100L,
        weaponClassesId = 3L,
        stdMedal = null,
        signupJson = """{"id":1,"competitions_id":10,"patrols_id":1,"patrols_finals_id":0,"lane_finals":0,"patrols_distinguish_id":0,"lane_distinguish":0,"start_time":"","end_time":"","lane":1,"weaponclasses_id":3,"registration_fee":0,"clubs_id":1,"start_before":"","start_after":"","share_patrol_with":0,"shoot_not_simultaneously_with":0,"requires_approval":0,"is_approved_by":0,"created_by":0,"created_at":"","special_wishes":"","first_last_patrol_human":"","start_time_human":"","end_time_human":"","user":{"name":"Test","lastname":"User","api_token":"abc","user_id":1,"fullname":"Test User","clubs_id":1,"status":"active","clubs":[]}}""",
        weaponClassJson = """{"id":3,"weapongroups_id":1,"classname":"A","championship":0,"classname_general":"A"}""",
        stationResultsJson = "[]"
    )

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `toDomain returns null for corrupted signupJson`() {
        val entity = validEntity().copy(signupJson = "not valid json {{{")
        assertNull(entity.toDomain(gson))
    }

    @Test
    fun `toDomain returns null for corrupted weaponClassJson`() {
        val entity = validEntity().copy(weaponClassJson = "corrupt!!!")
        assertNull(entity.toDomain(gson))
    }

    @Test
    fun `toDomain returns null for corrupted stationResultsJson`() {
        val entity = validEntity().copy(stationResultsJson = "{bad}")
        assertNull(entity.toDomain(gson))
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `toDomain maps valid entity correctly`() {
        val result = validEntity().toDomain(gson)
        assertNotNull(result)
        assertEquals(1L, result!!.id)
        assertEquals(10L, result.competitionsID)
        assertEquals(20L, result.signupsID)
        assertEquals(100L, result.points)
    }

    @Test
    fun `toDomain handles empty stationResults list`() {
        val result = validEntity().copy(stationResultsJson = "[]").toDomain(gson)
        assertNotNull(result)
        assertTrue(result!!.results.isEmpty())
    }
}
