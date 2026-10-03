package se.kjellstrand.webshooter.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.TimeSource

/**
 * Client-side throttle matching the webshooter.se API limit of 60 requests
 * per minute per user.
 *
 * The backend does not answer an exceeded limit with 429: it returns a plain
 * `500 {"message": "Ett fel inträffade. Vänligen försök igen."}` without any
 * `X-RateLimit-*` headers, which is indistinguishable from a real failure for
 * the caller. So the only safe option is to never exceed the limit.
 *
 * This is a sliding-window log: at most [maxPerWindow] requests are let
 * through in any [windowMillis] span, which can never exceed a fixed-window
 * limit of the same size on the server. [acquire] suspends until a slot is
 * free. Background work passes a `reserve` so it leaves headroom for
 * requests the user is waiting on.
 */
class RequestRateLimiter(
    private val maxPerWindow: Int = DEFAULT_MAX_PER_WINDOW,
    private val windowMillis: Long = 60_000L,
    private val now: () -> Long = monotonicClock(),
) {
    private val mutex = Mutex()
    private val stamps = ArrayDeque<Long>()

    /** Suspends until a request may be sent, then records it. */
    suspend fun acquire(reserve: Int = 0) {
        val cap = (maxPerWindow - reserve).coerceAtLeast(1)
        while (true) {
            val waitMillis = mutex.withLock {
                val t = now()
                prune(t)
                if (stamps.size < cap) {
                    stamps.addLast(t)
                    return
                }
                // Free again once enough of the oldest entries have aged out.
                stamps[stamps.size - cap] + windowMillis - t
            }
            delay(waitMillis.coerceAtLeast(MIN_WAIT_MILLIS))
        }
    }

    /**
     * Reconcile with the server's own count (`X-RateLimit-Limit` /
     * `X-RateLimit-Remaining`). The limit is per user, so the website or a
     * second device can spend the budget too; when the server has counted
     * more requests than we have, pad our log to match.
     */
    suspend fun onServerCount(limit: Int, remaining: Int) {
        val serverUsed = (limit - remaining).coerceIn(0, limit)
        mutex.withLock {
            val t = now()
            prune(t)
            repeat(serverUsed - stamps.size) { stamps.addLast(t) }
        }
    }

    private fun prune(t: Long) {
        while (stamps.isNotEmpty() && t - stamps.first() >= windowMillis) stamps.removeFirst()
    }

    companion object {
        /** Server limit is 60/min; stay a little under it. */
        const val DEFAULT_MAX_PER_WINDOW = 55

        /** Slots background sync leaves free for interactive requests. */
        const val BACKGROUND_RESERVE = 15

        private const val MIN_WAIT_MILLIS = 50L

        private fun monotonicClock(): () -> Long {
            val start = TimeSource.Monotonic.markNow()
            return { start.elapsedNow().inWholeMilliseconds }
        }
    }
}
