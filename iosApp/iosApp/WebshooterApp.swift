import SwiftUI
import Shared

#if canImport(FirebaseCore)
import FirebaseCore
#endif

@main
struct WebshooterApp: App {
    init() {
        let info = Bundle.main.infoDictionary
        let bundleVersion = (info?["CFBundleShortVersionString"] as? String) ?? "0.0.0"
        // BaseUrl + ClientSecret come from the build configuration via Info.plist
        // substitution. The Staging and Prod schemes set WEBSHOOTER_BASE_URL to
        // staging.webshooter.se vs webshooter.se respectively. See project.yml.
        let baseUrl = (info?["WebshooterBaseUrl"] as? String) ?? "https://staging.webshooter.se/"
        let clientSecret = (info?["WebshooterClientSecret"] as? String) ?? ""
        // WebshooterCrashReportingEnabled is "YES" only for the Release-Prod
        // scheme. Anywhere else we bind NoOpCrashReporter via Koin.
        let crashReportingEnabled =
            ((info?["WebshooterCrashReportingEnabled"] as? String)?.uppercased() == "YES")
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif

        // Configure Firebase before Koin so the bridge has a live
        // Crashlytics singleton when IosCrashReporter is first resolved.
        // GoogleService-Info.plist must be present in the app bundle
        // (download it from the Firebase console and add to iosApp/).
        var bridge: CrashReporterBridge? = nil
        if crashReportingEnabled {
            #if canImport(FirebaseCore)
            FirebaseApp.configure()
            bridge = FirebaseCrashReporterBridge()
            #endif
        }

        let config = WebshooterConfig(
            isDebug: isDebug,
            baseUrl: baseUrl,
            versionName: bundleVersion,
            clientSecret: clientSecret,
            crashReportingEnabled: crashReportingEnabled
        )
        _ = KoinKt.doInitKoin(
            platformModule: IosPlatformModuleKt.iosPlatformModule(
                config: config,
                crashReporterBridge: bridge
            )
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
