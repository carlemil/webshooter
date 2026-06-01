package se.kjellstrand.webshooter.data.telemetry

import se.kjellstrand.webshooter.data.MockModeManager

/**
 * iOS binding for [CrashReporter]. Translates Kotlin-native concepts
 * ([Throwable]) into the Swift-friendly tuple `(domain, code, userInfo)`
 * and delegates to a [CrashReporterBridge] implemented in Swift, which
 * does the actual Crashlytics call.
 *
 * Every method early-returns when [MockModeManager.isMockMode] is true.
 */
class IosCrashReporter(private val bridge: CrashReporterBridge) : CrashReporter {

    override fun recordException(throwable: Throwable, message: String?) {
        if (MockModeManager.isMockMode) return
        val domain = throwable::class.qualifiedName
            ?: throwable::class.simpleName
            ?: "KotlinThrowable"
        bridge.recordError(
            domain = domain,
            code = throwable.hashCode(),
            userInfo = buildMap {
                put("message", message ?: throwable.message ?: "")
                put("stacktrace", throwable.stackTraceToString())
            },
        )
    }

    override fun log(breadcrumb: String) {
        if (MockModeManager.isMockMode) return
        bridge.log(breadcrumb)
    }

    override fun setUserId(id: String?) {
        if (MockModeManager.isMockMode) return
        bridge.setUserId(id)
    }

    override fun setCustomKey(key: String, value: String) {
        if (MockModeManager.isMockMode) return
        bridge.setCustomKeyString(key, value)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        if (MockModeManager.isMockMode) return
        bridge.setCustomKeyBool(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        if (MockModeManager.isMockMode) return
        bridge.setCustomKeyInt(key, value)
    }
}
