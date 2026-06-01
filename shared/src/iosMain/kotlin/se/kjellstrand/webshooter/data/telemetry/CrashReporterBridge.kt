package se.kjellstrand.webshooter.data.telemetry

/**
 * Thin Swift-side seam. The iOS host implements this interface in Swift
 * with direct calls to the FirebaseCrashlytics Objective-C SDK, then
 * hands the instance to [iosPlatformModule]. Kotlin/Native interfaces
 * surface to Swift as `@objc` protocols, so the Swift impl can adopt it
 * with method names matching one-to-one.
 *
 * Lives in iosMain (not commonMain) because nothing else in the Kotlin
 * tree needs to know about the bridge; the only Kotlin caller is
 * [IosCrashReporter].
 *
 * No Kotlin `Throwable` crosses the boundary — [IosCrashReporter] maps
 * each throwable to (domain, code, userInfo) so the Swift impl can build
 * an `NSError` without touching Kotlin reflection.
 */
interface CrashReporterBridge {
    fun recordError(domain: String, code: Int, userInfo: Map<String, String>)
    fun log(message: String)
    fun setUserId(id: String?)
    fun setCustomKeyString(key: String, value: String)
    fun setCustomKeyBool(key: String, value: Boolean)
    fun setCustomKeyInt(key: String, value: Int)
}
