import XCTest

/// Verifies the host app's `Resources/mocks/` folder reference made it into
/// the built `.app` bundle with the directory structure intact. The iOS
/// `IosMockInterceptor.loadBundleResource` does the equivalent lookup at
/// runtime via `NSBundle.pathForResource:ofType:inDirectory:`, so if these
/// files aren't where the test expects them, mock mode silently returns
/// empty responses (see commit `01749a0`).
final class MockBundleResourceTests: XCTestCase {

    /// The host app's bundle (resolved via TEST_HOST in project.yml).
    private var hostBundle: Bundle {
        // Each unit-test target embeds the host app at a predictable path.
        guard let hostBundleURL = Bundle(for: type(of: self))
            .url(forResource: "iosApp", withExtension: "app",
                 subdirectory: nil) ?? Bundle.allBundles.first(where: {
                $0.bundlePath.hasSuffix("iosApp.app")
            })?.bundleURL,
              let bundle = Bundle(url: hostBundleURL)
        else {
            // Fallback: TEST_HOST setup means main bundle IS the host app.
            return Bundle.main
        }
        return bundle
    }

    func testCompetitionsMockExists() {
        let path = hostBundle.path(
            forResource: "competitions",
            ofType: "txt",
            inDirectory: "mocks"
        )
        XCTAssertNotNil(path, "Resources/mocks/competitions.txt missing from app bundle")
    }

    func testAuthEndpointMocksExist() {
        let requiredAuthMocks = [
            "api_v4_1_9_oauth_token",
            "authenticate_user",
            "clubs_get_user_club",
        ]
        for name in requiredAuthMocks {
            XCTAssertNotNil(
                hostBundle.path(forResource: name, ofType: "txt", inDirectory: "mocks"),
                "Resources/mocks/\(name).txt missing from app bundle"
            )
        }
    }

    func testCompetitionDetailMocksExistForKnownIds() {
        // IosMockInterceptor hard-codes the per-competition routes; mocks for
        // 208 and 305 are referenced by the autologin / mock-mode flows.
        let prefixes = ["results", "signups", "patrols", "teams"]
        let ids = [208, 305]
        for id in ids {
            for prefix in prefixes {
                let name = "\(prefix)_\(id)"
                XCTAssertNotNil(
                    hostBundle.path(forResource: name, ofType: "txt", inDirectory: "mocks"),
                    "Resources/mocks/\(name).txt missing from app bundle"
                )
            }
        }
    }

    func testMyEntriesMockExists() {
        // The /api/v4.1.9/competitions endpoint with usersignup=1 maps to a
        // separate mock file (see IosMockInterceptor.mockBodyFor).
        XCTAssertNotNil(
            hostBundle.path(forResource: "myentries", ofType: "txt", inDirectory: "mocks"),
            "Resources/mocks/myentries.txt missing from app bundle"
        )
    }
}
