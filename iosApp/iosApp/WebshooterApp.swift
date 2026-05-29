import SwiftUI
import Shared

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
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif
        let config = WebshooterConfig(
            isDebug: isDebug,
            baseUrl: baseUrl,
            versionName: bundleVersion,
            clientSecret: clientSecret
        )
        _ = KoinKt.doInitKoin(platformModule: IosPlatformModuleKt.iosPlatformModule(config: config))
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
