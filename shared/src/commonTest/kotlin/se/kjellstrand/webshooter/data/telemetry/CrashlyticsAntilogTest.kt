package se.kjellstrand.webshooter.data.telemetry

import io.github.aakira.napier.Napier
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Behavior tests for the level-mapping inside [CrashlyticsAntilog]. These
 * are the only contract the production CrashReporter implementations
 * depend on; the Crashlytics SDK call sites themselves are exercised on
 * device.
 *
 * Drives the antilog through Napier's public API (its `performLog`
 * override is `protected` in the upstream class).
 */
class CrashlyticsAntilogTest {

    private class RecordingCrashReporter : CrashReporter {
        data class Recorded(val throwable: Throwable, val message: String?)

        val recorded = mutableListOf<Recorded>()
        val breadcrumbs = mutableListOf<String>()

        override fun recordException(throwable: Throwable, message: String?) {
            recorded += Recorded(throwable, message)
        }

        override fun log(breadcrumb: String) {
            breadcrumbs += breadcrumb
        }

        override fun setUserId(id: String?) = Unit
        override fun setCustomKey(key: String, value: String) = Unit
        override fun setCustomKey(key: String, value: Boolean) = Unit
        override fun setCustomKey(key: String, value: Int) = Unit
    }

    private lateinit var sink: RecordingCrashReporter
    private lateinit var antilog: CrashlyticsAntilog

    @BeforeTest
    fun installAntilog() {
        sink = RecordingCrashReporter()
        antilog = CrashlyticsAntilog(sink)
        Napier.base(antilog)
    }

    @AfterTest
    fun removeAntilog() {
        Napier.takeLogarithm(antilog)
    }

    @Test
    fun verbose_debug_info_levels_are_dropped() {
        Napier.v("verbose msg", tag = "x")
        Napier.d("debug msg", tag = "x")
        Napier.i("info msg", tag = "x")
        assertTrue(sink.recorded.isEmpty(), "no exceptions should be recorded")
        assertTrue(sink.breadcrumbs.isEmpty(), "no breadcrumbs should be left")
    }

    @Test
    fun warning_with_throwable_records_non_fatal_and_breadcrumb() {
        val throwable = RuntimeException("bang")
        Napier.w("syncAll failed", throwable, "Repo")

        assertEquals(1, sink.recorded.size)
        assertEquals(throwable, sink.recorded.single().throwable)
        assertEquals("syncAll failed", sink.recorded.single().message)
        assertEquals(listOf("Repo: syncAll failed"), sink.breadcrumbs)
    }

    @Test
    fun warning_without_throwable_only_logs_breadcrumb() {
        Napier.w("deep link ignored", tag = "AppNavHost")

        assertTrue(sink.recorded.isEmpty(), "non-throwable warnings stay as breadcrumbs only")
        assertEquals(listOf("AppNavHost: deep link ignored"), sink.breadcrumbs)
    }

    @Test
    fun error_with_throwable_records_non_fatal() {
        val throwable = IllegalStateException("boom")
        Napier.e("couldn't pop", throwable, "Nav")

        assertEquals(1, sink.recorded.size)
        assertEquals(throwable, sink.recorded.single().throwable)
        assertEquals(listOf("Nav: couldn't pop"), sink.breadcrumbs)
    }

    @Test
    fun error_without_throwable_synthesizes_runtime_exception() {
        Napier.e("lone error", tag = "X")

        assertEquals(1, sink.recorded.size)
        assertTrue(sink.recorded.single().throwable is RuntimeException)
        assertEquals("X: lone error", sink.recorded.single().throwable.message)
    }

    @Test
    fun error_with_no_message_still_records() {
        Napier.e("")

        assertEquals(1, sink.recorded.size)
        assertNull(sink.recorded.single().message?.takeIf { it.isNotEmpty() })
    }
}
