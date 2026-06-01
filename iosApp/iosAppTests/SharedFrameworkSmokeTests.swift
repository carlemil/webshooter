import XCTest
import Shared

/// Smoke-tests the link to the Kotlin/Native `Shared.framework` from the
/// Swift unit-test target. If these fail, every other Swift test in this
/// target is meaningless — the framework isn't reachable.
final class SharedFrameworkSmokeTests: XCTestCase {

    func testSharedFrameworkIsLinked() {
        // MockModeManager is a Kotlin `object` (singleton) — it bridges to
        // Swift as a class with a `shared` accessor.
        let manager = MockModeManager.shared
        let original = manager.isMockMode
        manager.isMockMode = true
        XCTAssertTrue(MockModeManager.shared.isMockMode)
        manager.isMockMode = false
        XCTAssertFalse(MockModeManager.shared.isMockMode)
        // Restore.
        manager.isMockMode = original
    }

    func testWebshooterConfigConstructs() {
        // WebshooterConfig is the DI parameter the iOS host hands to Koin.
        // If its initialiser signature drifts, this fails to compile.
        let config = WebshooterConfig(
            isDebug: true,
            baseUrl: "https://example.test/",
            versionName: "0.0.0",
            clientSecret: "test"
        )
        XCTAssertEqual(config.baseUrl, "https://example.test/")
        XCTAssertEqual(config.versionName, "0.0.0")
        XCTAssertTrue(config.isDebug)
    }
}
