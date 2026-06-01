package se.kjellstrand.webshooter.data.telemetry

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

/**
 * Napier sink that pipes every `Napier.w(...)` and `Napier.e(...)` call
 * into the supplied [CrashReporter]. This is the single highest-leverage
 * change in the telemetry rollout — the ~50 existing
 * `Napier.w("Error", e, TAG)` sites across the data layer all gain
 * non-fatal crash reporting without per-site edits.
 *
 * Level mapping:
 * - VERBOSE / DEBUG / INFO → dropped (noise; debug builds still log them
 *   via DebugAntilog registered alongside).
 * - WARNING → breadcrumb `log(tag: message)` plus, if a throwable was
 *   passed, a non-fatal `recordException`. Lossy-on-purpose: warnings
 *   without a throwable are recoverable events worth a breadcrumb but
 *   not a separate issue in the dashboard.
 * - ERROR / ASSERT → breadcrumb plus non-fatal. If no throwable was
 *   passed, a synthetic `RuntimeException` carries the message so the
 *   issue still has a stack trace pointing at the call site.
 *
 * Register alongside (not instead of) DebugAntilog — the two coexist:
 * DebugAntilog handles human-readable logcat output; CrashlyticsAntilog
 * handles persistence to the dashboard.
 */
class CrashlyticsAntilog(private val reporter: CrashReporter) : Antilog() {

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) {
        when (priority) {
            LogLevel.VERBOSE, LogLevel.DEBUG, LogLevel.INFO -> Unit
            LogLevel.WARNING -> {
                val text = formatBreadcrumb(tag, message)
                if (text.isNotEmpty()) reporter.log(text)
                if (throwable != null) reporter.recordException(throwable, message)
            }
            LogLevel.ERROR, LogLevel.ASSERT -> {
                val text = formatBreadcrumb(tag, message)
                if (text.isNotEmpty()) reporter.log(text)
                reporter.recordException(
                    throwable ?: RuntimeException(text.ifEmpty { "Napier.e with no message" }),
                    message,
                )
            }
        }
    }

    private fun formatBreadcrumb(tag: String?, message: String?): String =
        listOfNotNull(tag, message).filter { it.isNotEmpty() }.joinToString(": ")
}
