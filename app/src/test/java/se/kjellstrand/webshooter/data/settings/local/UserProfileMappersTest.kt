package se.kjellstrand.webshooter.data.settings.local

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class UserProfileMappersTest {

    private val gson = Gson()

    private fun validEntity() = UserProfileEntity(
        userId = 1L,
        name = "Test",
        lastname = "User",
        email = "test@test.com",
        shootingCardNumber = null,
        noShootingCardNumber = null,
        birthday = null,
        gender = null,
        phone = null,
        mobile = null,
        gradeField = null,
        gradeTrackshooting = null,
        apiToken = null,
        fullname = "Test User",
        clubsId = 1L,
        status = "active",
        clubsJson = "[]"
    )

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `toDomain returns null for corrupted clubsJson`() {
        val entity = validEntity().copy(clubsJson = "not valid json!!!")
        assertNull(entity.toDomain(gson))
    }

    @Test
    fun `toDomain returns null for malformed clubsJson object instead of array`() {
        val entity = validEntity().copy(clubsJson = """{"id":1}""")
        assertNull(entity.toDomain(gson))
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `toDomain maps valid entity correctly`() {
        val result = validEntity().toDomain(gson)
        assertNotNull(result)
        assertEquals(1L, result!!.userId)
        assertEquals("Test", result.name)
        assertEquals("User", result.lastname)
        assertEquals("test@test.com", result.email)
    }

    @Test
    fun `toDomain handles empty clubs list`() {
        val result = validEntity().toDomain(gson)
        assertNotNull(result)
        assertTrue(result!!.clubs.isEmpty())
    }
}
