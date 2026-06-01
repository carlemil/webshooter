import Foundation
import Shared

#if canImport(FirebaseCrashlytics)
import FirebaseCrashlytics
#endif

/// Swift-side implementation of the Kotlin `CrashReporterBridge` protocol
/// (exposed via the Shared framework). The Kotlin `IosCrashReporter`
/// translates `Throwable` into the `(domain, code, userInfo)` tuple this
/// bridge receives; the only thing left to do here is hand off to the
/// FirebaseCrashlytics Obj-C SDK.
///
/// Lives in Swift so we never have to cinterop against the Firebase
/// framework from Kotlin/Native.
@objc final class FirebaseCrashReporterBridge: NSObject, CrashReporterBridge {

    func recordError(domain: String, code: Int32, userInfo: [String: String]) {
        #if canImport(FirebaseCrashlytics)
        let nsError = NSError(
            domain: domain,
            code: Int(code),
            userInfo: userInfo
        )
        Crashlytics.crashlytics().record(error: nsError)
        #endif
    }

    func log(message: String) {
        #if canImport(FirebaseCrashlytics)
        Crashlytics.crashlytics().log(message)
        #endif
    }

    func setUserId(id: String?) {
        #if canImport(FirebaseCrashlytics)
        Crashlytics.crashlytics().setUserID(id ?? "")
        #endif
    }

    func setCustomKeyString(key: String, value: String) {
        #if canImport(FirebaseCrashlytics)
        Crashlytics.crashlytics().setCustomValue(value, forKey: key)
        #endif
    }

    func setCustomKeyBool(key: String, value: Bool) {
        #if canImport(FirebaseCrashlytics)
        Crashlytics.crashlytics().setCustomValue(value, forKey: key)
        #endif
    }

    func setCustomKeyInt(key: String, value: Int32) {
        #if canImport(FirebaseCrashlytics)
        Crashlytics.crashlytics().setCustomValue(Int(value), forKey: key)
        #endif
    }
}
