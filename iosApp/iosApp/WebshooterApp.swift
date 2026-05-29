import SwiftUI
import Shared

@main
struct WebshooterApp: App {
    init() {
        let bundleVersion = (Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String) ?? "0.0.0"
        let config = WebshooterConfig(
            isDebug: true,
            baseUrl: "https://staging.webshooter.se/",
            versionName: bundleVersion,
            // Same default secret as the Android build's hardcoded fallback;
            // see app/build.gradle.kts productFlavors block. Replace with
            // an xcconfig/Info.plist source in Phase 7.
            clientSecret: "REMOVED-CLIENT-SECRET"
        )
        _ = KoinKt.doInitKoin(platformModule: IosPlatformModuleKt.iosPlatformModule(config: config))
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
