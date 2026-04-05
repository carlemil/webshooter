package se.kjellstrand.webshooter.data

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthCookieJarTest {

    private lateinit var cookieJar: AuthCookieJar
    private val testUrl = "https://webshooter.se/api/v4.1.9/test".toHttpUrl()

    @Before
    fun setUp() {
        cookieJar = AuthCookieJar()
    }

    private fun buildCookie(
        name: String,
        value: String,
        domain: String = "webshooter.se",
        path: String = "/",
        expiresAt: Long = Long.MAX_VALUE
    ): Cookie {
        return Cookie.Builder()
            .name(name)
            .value(value)
            .domain(domain)
            .path(path)
            .expiresAt(expiresAt)
            .build()
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `loadForRequest excludes expired cookies`() {
        val expiredCookie = buildCookie("session", "abc", expiresAt = 1L)
        cookieJar.saveFromResponse(testUrl, listOf(expiredCookie))

        val loaded = cookieJar.loadForRequest(testUrl)

        assertTrue("Expired cookies should not be returned", loaded.isEmpty())
    }

    @Test
    fun `loadForRequest returns only non-expired cookies when mix present`() {
        val expiredCookie = buildCookie("old", "stale", expiresAt = 1L)
        val validCookie = buildCookie("new", "fresh", expiresAt = Long.MAX_VALUE)
        cookieJar.saveFromResponse(testUrl, listOf(expiredCookie, validCookie))

        val loaded = cookieJar.loadForRequest(testUrl)

        assertEquals(1, loaded.size)
        assertEquals("new", loaded[0].name)
    }

    @Test
    fun `expired cookies are removed from store after loadForRequest`() {
        val expiredCookie = buildCookie("old", "stale", expiresAt = 1L)
        val validCookie = buildCookie("new", "fresh", expiresAt = Long.MAX_VALUE)
        cookieJar.saveFromResponse(testUrl, listOf(expiredCookie, validCookie))

        cookieJar.loadForRequest(testUrl)

        val allCookies = cookieJar.getAllCookies()
        assertFalse("Expired cookie should be cleaned from store", allCookies.contains("old=stale"))
        assertTrue("Valid cookie should remain in store", allCookies.contains("new=fresh"))
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `loadForRequest returns matching non-expired cookies`() {
        val cookie = buildCookie("token", "xyz")
        cookieJar.saveFromResponse(testUrl, listOf(cookie))

        val loaded = cookieJar.loadForRequest(testUrl)

        assertEquals(1, loaded.size)
        assertEquals("token", loaded[0].name)
    }

    @Test
    fun `loadForRequest filters by domain`() {
        val otherDomainCookie = buildCookie("token", "xyz", domain = "other.se")
        cookieJar.saveFromResponse("https://other.se/".toHttpUrl(), listOf(otherDomainCookie))

        val loaded = cookieJar.loadForRequest(testUrl)

        assertTrue("Cookies from other domains should not be returned", loaded.isEmpty())
    }

    @Test
    fun `saveFromResponse overwrites cookie with same key`() {
        val cookie1 = buildCookie("token", "old")
        val cookie2 = buildCookie("token", "new")
        cookieJar.saveFromResponse(testUrl, listOf(cookie1))
        cookieJar.saveFromResponse(testUrl, listOf(cookie2))

        val loaded = cookieJar.loadForRequest(testUrl)

        assertEquals(1, loaded.size)
        assertEquals("new", loaded[0].value)
    }

    @Test
    fun `getAllCookies returns all stored cookies`() {
        val cookie1 = buildCookie("a", "1")
        val cookie2 = buildCookie("b", "2")
        cookieJar.saveFromResponse(testUrl, listOf(cookie1, cookie2))

        val result = cookieJar.getAllCookies()

        assertTrue(result.contains("a=1"))
        assertTrue(result.contains("b=2"))
    }

    @Test
    fun `getSessionCookies returns only session-related cookies with masked values`() {
        val xsrf = buildCookie("XSRF-TOKEN", "xsrftokenvalue123")
        val session = buildCookie("laravel_session", "sessionvalue456")
        val other = buildCookie("other", "oval")
        cookieJar.saveFromResponse(testUrl, listOf(xsrf, session, other))

        val result = cookieJar.getSessionCookies()

        assertTrue(result.contains("XSRF-TOKEN=xsrf****"))
        assertTrue(result.contains("laravel_session=sess****"))
        assertFalse(result.contains("other"))
    }
}
