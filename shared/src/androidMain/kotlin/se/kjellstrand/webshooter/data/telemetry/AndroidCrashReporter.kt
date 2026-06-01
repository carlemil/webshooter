package se.kjellstrand.webshooter.data.telemetry

import com.google.firebase.crashlytics.FirebaseCrashlytics
import se.kjellstrand.webshooter.data.MockModeManager

/**
 * Android binding for [CrashReporter] — thin wrapper over
 * `FirebaseCrashlytics.getInstance()`. The Firebase singleton is
 * initialised by `FirebaseApp.initializeApp(...)` in
 * `ShooterApplication.onCreate`, which runs before Koin resolves this
 * binding.
 *
 * Every method early-returns when `MockModeManager.isMockMode` is true,
 * so reviewer-flow noise stays off the dashboard even if the user logs
 * in with `mockuser` on a prod build.
 */
class AndroidCrashReporter : CrashReporter {

    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    override fun recordException(throwable: Throwable, message: String?) {
        if (MockModeManager.isMockMode) return
        if (message != null) crashlytics.log(message)
        crashlytics.recordException(throwable)
    }

    override fun log(breadcrumb: String) {
        if (MockModeManager.isMockMode) return
        crashlytics.log(breadcrumb)
    }

    override fun setUserId(id: String?) {
        if (MockModeManager.isMockMode) return
        crashlytics.setUserId(id ?: "")
    }

    override fun setCustomKey(key: String, value: String) {
        if (MockModeManager.isMockMode) return
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        if (MockModeManager.isMockMode) return
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        if (MockModeManager.isMockMode) return
        crashlytics.setCustomKey(key, value)
    }
}
