package se.kjellstrand.webshooter.data.mysignups.local

import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.Test

class SignupMappersTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private fun validEntity() = SignupEntryEntity(
        id = 1L,
        groupKey = "group1",
        competitionsId = 10L,
        weaponClassesId = 3L,
        patrolsId = 1L,
        startTime = null,
        endTime = null,
        lane = 1L,
        note = null,
        registrationFee = 100L,
        specialWishes = "",
        startTimeHuman = "10:00",
        endTimeHuman = "12:00",
        competitionJson = """{"id":10,"name":"Test","date":"2026-01-01","status":"active","status_human":"Active","contact_name":"John","contact_city":"Stockholm","results_type":"precision","results_type_human":"Precision"}""",
        weaponClassJson = """{"id":3,"classname":"A","classname_general":"Pistol"}""",
        patrolJson = null,
        resultsPlacementsJson = null
    )

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `toDomain returns null for corrupted competitionJson`() {
        val entity = validEntity().copy(competitionJson = "not json!!!")
        assertNull(entity.toDomain(json))
    }

    @Test
    fun `toDomain returns null for corrupted weaponClassJson`() {
        val entity = validEntity().copy(weaponClassJson = "{{{bad")
        assertNull(entity.toDomain(json))
    }

    @Test
    fun `toDomain returns null for corrupted patrolJson`() {
        val entity = validEntity().copy(patrolJson = "corrupt")
        assertNull(entity.toDomain(json))
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `toDomain maps valid entity correctly`() {
        val result = validEntity().toDomain(json)
        assertNotNull(result)
        assertEquals(1L, result!!.id)
        assertEquals(10L, result.competitionsId)
        assertEquals(3L, result.weaponClassesId)
    }

    @Test
    fun `toDomain handles null patrol and resultsPlacement`() {
        val result = validEntity().toDomain(json)
        assertNotNull(result)
        assertNull(result!!.patrol)
        assertNull(result.resultsPlacements)
    }

    @Test
    fun `toDomain handles null resultsPlacementsJson`() {
        val result = validEntity().copy(resultsPlacementsJson = null).toDomain(json)
        assertNotNull(result)
        assertNull(result!!.resultsPlacements)
    }
}
