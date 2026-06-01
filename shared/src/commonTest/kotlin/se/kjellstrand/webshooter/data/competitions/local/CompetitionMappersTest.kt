package se.kjellstrand.webshooter.data.competitions.local

import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.Test

class CompetitionMappersTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val validClubJson = """{"id":1,"disable_personal_invoices":0,"districts_id":1,"clubs_nr":"001","name":"Test Club","email":"club@test.com","phone":null,"address_street":"Street 1","address_street_2":null,"address_zipcode":"12345","address_city":"Stockholm","address_country":null,"bankgiro":null,"postgiro":null,"swish":"123","logo":null,"user_has_role":null,"address_combined":"Street 1, 12345 Stockholm","address_incomplete":false,"logo_url":"","logo_path":""}"""

    private fun validEntity() = CompetitionEntity(
        id = 1L,
        name = "Test Competition",
        date = "2026-01-01",
        status = "active",
        statusHuman = "Active",
        contactName = "John",
        contactVenue = "Range",
        contactCity = "Stockholm",
        contactEmail = "test@test.com",
        contactTelephone = "123",
        lat = 59.0,
        lng = 18.0,
        googleMaps = null,
        description = "A test",
        website = null,
        resultsType = "PRECISION",
        resultsTypeHuman = "Precision",
        signupsOpeningDate = "2026-01-01",
        signupsClosingDate = "2026-01-01",
        allowSignupsAfterClosingDateHuman = null,
        startTimeHuman = null,
        finalTimeHuman = null,
        signupsCount = 10L,
        patrolsCount = 2L,
        allowTeams = 0L,
        competitionTypeJson = """{"id":1,"name":"Precision"}""",
        weaponGroupsJson = "[]",
        weaponClassesJson = "[]",
        userSignupsJson = "[]",
        clubJson = validClubJson
    )

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `toDomain returns null for corrupted competitionTypeJson`() {
        val entity = validEntity().copy(competitionTypeJson = "not json!!!")
        assertNull(entity.toDomain(json))
    }

    @Test
    fun `toDomain returns null for corrupted clubJson`() {
        val entity = validEntity().copy(clubJson = "{{{bad json")
        assertNull(entity.toDomain(json))
    }

    @Test
    fun `toDomain returns null for corrupted weaponGroupsJson`() {
        val entity = validEntity().copy(weaponGroupsJson = "bad")
        assertNull(entity.toDomain(json))
    }

    @Test
    fun `toDomain returns null for corrupted weaponClassesJson`() {
        val entity = validEntity().copy(weaponClassesJson = "bad")
        assertNull(entity.toDomain(json))
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `toDomain maps valid entity correctly`() {
        val result = validEntity().toDomain(json)
        assertNotNull(result)
        assertEquals(1L, result!!.id)
        assertEquals("Test Competition", result.name)
        assertEquals("2026-01-01", result.date)
    }

    @Test
    fun `toDomain handles empty list json fields`() {
        val result = validEntity().toDomain(json)
        assertNotNull(result)
        assertTrue(result!!.weaponGroups.isEmpty())
        assertTrue(result.weaponClasses.isEmpty())
        assertTrue(result.userSignups.isEmpty())
    }
}
