package se.kjellstrand.webshooter.data.club.local

import org.junit.Assert.*
import org.junit.Test
import se.kjellstrand.webshooter.data.club.remote.ClubData

class ClubMappersTest {

    private fun buildClubData(
        phone: String? = null,
        addressStreet: String? = null,
        addressZipcode: String? = null,
        addressCity: String? = null,
        addressCountry: String? = null,
        bankgiro: String? = null,
        postgiro: String? = null
    ) = ClubData(
        id = 1L,
        clubsNr = "123",
        name = "Test Club",
        email = "test@test.se",
        phone = phone,
        addressStreet = addressStreet,
        addressStreet2 = null,
        addressZipcode = addressZipcode,
        addressCity = addressCity,
        addressCountry = addressCountry,
        bankgiro = bankgiro,
        postgiro = postgiro,
        swish = null,
        logoUrl = null
    )

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `sanitize replaces literal null string with null for phone`() {
        val club = buildClubData(phone = "null")
        val sanitized = club.sanitizeNullStrings()
        assertNull("phone 'null' string should become null", sanitized.phone)
    }

    @Test
    fun `sanitize replaces literal null string with null for address fields`() {
        val club = buildClubData(
            addressStreet = "null",
            addressZipcode = "null",
            addressCity = "null",
            addressCountry = "null"
        )
        val sanitized = club.sanitizeNullStrings()
        assertNull("addressStreet 'null' should become null", sanitized.addressStreet)
        assertNull("addressZipcode 'null' should become null", sanitized.addressZipcode)
        assertNull("addressCity 'null' should become null", sanitized.addressCity)
        assertNull("addressCountry 'null' should become null", sanitized.addressCountry)
    }

    @Test
    fun `sanitize replaces literal null string with null for bankgiro and postgiro`() {
        val club = buildClubData(bankgiro = "null", postgiro = "null")
        val sanitized = club.sanitizeNullStrings()
        assertNull("bankgiro 'null' should become null", sanitized.bankgiro)
        assertNull("postgiro 'null' should become null", sanitized.postgiro)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `sanitize preserves actual values`() {
        val club = buildClubData(
            phone = "0701234567",
            addressStreet = "Main St 1",
            bankgiro = "1234-5678"
        )
        val sanitized = club.sanitizeNullStrings()
        assertEquals("0701234567", sanitized.phone)
        assertEquals("Main St 1", sanitized.addressStreet)
        assertEquals("1234-5678", sanitized.bankgiro)
    }

    @Test
    fun `sanitize preserves actual null values`() {
        val club = buildClubData(phone = null, addressStreet = null, bankgiro = null)
        val sanitized = club.sanitizeNullStrings()
        assertNull(sanitized.phone)
        assertNull(sanitized.addressStreet)
        assertNull(sanitized.bankgiro)
    }

    @Test
    fun `sanitize preserves non-nullable fields unchanged`() {
        val club = buildClubData()
        val sanitized = club.sanitizeNullStrings()
        assertEquals(1L, sanitized.id)
        assertEquals("123", sanitized.clubsNr)
        assertEquals("Test Club", sanitized.name)
        assertEquals("test@test.se", sanitized.email)
    }
}
