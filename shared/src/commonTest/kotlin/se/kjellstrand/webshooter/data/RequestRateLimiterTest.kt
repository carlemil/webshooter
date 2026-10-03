package se.kjellstrand.webshooter.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RequestRateLimiterTest {

    @Test
    fun `requests within the budget are not delayed`() = runTest {
        val limiter = RequestRateLimiter(maxPerWindow = 3, windowMillis = 1_000, now = { currentTime })

        repeat(3) { limiter.acquire() }

        assertEquals(0, currentTime)
    }

    @Test
    fun `request over the budget waits for the oldest one to age out`() = runTest {
        val limiter = RequestRateLimiter(maxPerWindow = 3, windowMillis = 1_000, now = { currentTime })

        repeat(4) { limiter.acquire() }

        assertEquals(1_000, currentTime)
    }

    @Test
    fun `never lets more than the budget through in any window`() = runTest {
        val limiter = RequestRateLimiter(maxPerWindow = 5, windowMillis = 1_000, now = { currentTime })
        val sentAt = mutableListOf<Long>()

        repeat(23) {
            limiter.acquire()
            sentAt += currentTime
        }

        sentAt.forEach { start ->
            val inWindow = sentAt.count { it >= start && it < start + 1_000 }
            assertTrue(inWindow <= 5, "$inWindow requests in the window starting at $start")
        }
    }

    @Test
    fun `background requests leave the reserve free for interactive ones`() = runTest {
        val limiter = RequestRateLimiter(maxPerWindow = 3, windowMillis = 1_000, now = { currentTime })

        limiter.acquire(reserve = 2)
        assertEquals(0, currentTime)

        // Interactive requests can still use the two reserved slots.
        limiter.acquire()
        limiter.acquire()
        assertEquals(0, currentTime)

        limiter.acquire(reserve = 2)
        assertEquals(1_000, currentTime, "Second background request must wait for the window")
    }

    @Test
    fun `server count from another client shrinks the local budget`() = runTest {
        val limiter = RequestRateLimiter(maxPerWindow = 3, windowMillis = 1_000, now = { currentTime })

        limiter.acquire()
        // Server says 3 of 60 are spent although we only sent one.
        limiter.onServerCount(limit = 60, remaining = 57)
        limiter.acquire()

        assertEquals(1_000, currentTime)
    }

    @Test
    fun `server count lower than the local one is ignored`() = runTest {
        val limiter = RequestRateLimiter(maxPerWindow = 3, windowMillis = 1_000, now = { currentTime })

        repeat(3) { limiter.acquire() }
        limiter.onServerCount(limit = 60, remaining = 60)
        limiter.acquire()

        assertEquals(1_000, currentTime)
    }
}
