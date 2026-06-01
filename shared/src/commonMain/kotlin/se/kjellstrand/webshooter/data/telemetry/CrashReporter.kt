package se.kjellstrand.webshooter.data.telemetry

/**
 * Platform-agnostic sink for crashes, non-fatal errors, breadcrumbs, and
 * crash-time context (user ID, custom keys). The Android binding wraps
 * Firebase Crashlytics directly; the iOS binding routes through a Swift
 * bridge so the SDK call sites stay in Swift (sidesteps Kotlin/Native
 * cinterop with Firebase).
 *
 * Non-prod builds bind [NoOpCrashReporter] so test runs and staging don't
 * report.
 *
 * Most call sites don't talk to this directly — `CrashlyticsAntilog`
 * pipes every `Napier.w/Napier.e` through `recordException` + `log`
 * automatically. Direct calls are reserved for the few places where we
 * want to set context (user ID on login, route on navigation).
 */
interface CrashReporter {

    /**
     * Record a non-fatal exception. The throwable surfaces as a separate
     * issue in the Crashlytics dashboard, distinct from real crashes. Use
     * for caught exceptions that we recover from but want to know about.
     */
    fun recordException(throwable: Throwable, message: String? = null)

    /**
     * Append a breadcrumb to the current session. Breadcrumbs are
     * attached to whatever crash or non-fatal happens next; on their own
     * they're not surfaced.
     */
    fun log(breadcrumb: String)

    /**
     * Set the Crashlytics user ID for the current session. Pass `null` on
     * logout. The webshooter.se user ID (a `Long`) is the safe choice —
     * lets support correlate a crash to a user without leaking
     * username/email into breadcrumbs.
     */
    fun setUserId(id: String?)

    /** Custom key for the current session — visible in the issue detail. */
    fun setCustomKey(key: String, value: String)
    fun setCustomKey(key: String, value: Boolean)
    fun setCustomKey(key: String, value: Int)
}
