package se.kjellstrand.webshooter.data.telemetry

/**
 * Bound on non-prod builds (debug, staging) and in mock-mode reviewer flows.
 * All methods are no-ops, so any call site can use [CrashReporter] without
 * a null check.
 */
object NoOpCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable, message: String?) = Unit
    override fun log(breadcrumb: String) = Unit
    override fun setUserId(id: String?) = Unit
    override fun setCustomKey(key: String, value: String) = Unit
    override fun setCustomKey(key: String, value: Boolean) = Unit
    override fun setCustomKey(key: String, value: Int) = Unit
}
