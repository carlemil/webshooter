package se.kjellstrand.webshooter.data.club.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
        addressZipcode = addressZipcode,
        addressCity = addressCity,
        addressCountry = addressCountry,
        bankgiro = bankgiro,
        postgiro = postgiro,
        swish = null
    )

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `sanitize replaces literal null string with null for phone`() {
        val club = buildClubData(phone = "null")
        val sanitized = club.sanitizeNullStrings()
        assertNull(sanitized.phone, "phone 'null' string should become null")
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
        assertNull(sanitized.addressStreet, "addressStreet 'null' should become null")
        assertNull(sanitized.addressZipcode, "addressZipcode 'null' should become null")
        assertNull(sanitized.addressCity, "addressCity 'null' should become null")
        assertNull(sanitized.addressCountry, "addressCountry 'null' should become null")
    }

    @Test
    fun `sanitize replaces literal null string with null for bankgiro and postgiro`() {
        val club = buildClubData(bankgiro = "null", postgiro = "null")
        val sanitized = club.sanitizeNullStrings()
        assertNull(sanitized.bankgiro, "bankgiro 'null' should become null")
        assertNull(sanitized.postgiro, "postgiro 'null' should become null")
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
