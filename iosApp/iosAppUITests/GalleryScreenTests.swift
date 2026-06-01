import XCTest

/// Smoke-tests every `DebugGallery` screen (see DebugGallery.kt) by launching
/// the app with `SCREEN=<name>` and asserting the scene rendered something
/// non-trivial (more than just an empty container). Button-level interaction
/// coverage lives in MockLoginFlowTests.swift, which drives the real
/// `AppNavHost` via the `mockuser`/`mockpassword` credentials.
final class GalleryScreenTests: XCTestCase {

    override func setUp() {
        continueAfterFailure = false
    }

    private func launch(screen: String) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["SCREEN"] = screen
        app.launch()
        return app
    }

    /// Asserts the gallery screen rendered something meaningful. Compose
    /// Multiplatform on iOS publishes most Compose `Text` nodes as `Other`
    /// elements (not `StaticText`), so we count those too.
    private func assertScreenRenders(_ app: XCUIApplication, minNodes: Int = 5) {
        let anyElement = app.descendants(matching: .any).firstMatch
        XCTAssertTrue(anyElement.waitForExistence(timeout: 15))
        let nodeCount = app.buttons.count
            + app.staticTexts.count
            + app.otherElements.count
        XCTAssertGreaterThanOrEqual(
            nodeCount,
            minNodes,
            "Screen rendered too few elements (\(nodeCount)) — likely empty/crashed"
        )
    }

    private func assertLabel(_ app: XCUIApplication, _ label: String, timeout: TimeInterval = 15) {
        let predicate = NSPredicate(format: "label == %@ OR label CONTAINS[c] %@", label, label)
        let exists = NSPredicate { _, _ in
            app.buttons.matching(predicate).firstMatch.exists
                || app.staticTexts.matching(predicate).firstMatch.exists
                || app.otherElements.matching(predicate).firstMatch.exists
                || app.textFields.matching(predicate).firstMatch.exists
                || app.secureTextFields.matching(predicate).firstMatch.exists
        }
        let exp = XCTNSPredicateExpectation(predicate: exists, object: nil)
        XCTAssertEqual(
            XCTWaiter().wait(for: [exp], timeout: timeout),
            .completed,
            "Timed out waiting for label '\(label)'"
        )
    }

    // MARK: gallery screens

    func testLoginScreenRenders() {
        let app = launch(screen: "login")
        // Login screen exposes Buttons for OutlinedTextFields + the login submit
        // + the password visibility toggle.
        assertLabel(app, "Inloggning")
        assertLabel(app, "Visa/dölj lösenord")
    }

    func testCompetitionsScreenRenders() {
        let app = launch(screen: "competitions")
        assertScreenRenders(app)
    }

    func testMyEntriesScreenRenders() {
        let app = launch(screen: "myentries")
        assertScreenRenders(app)
    }

    func testChartsScreenRenders() {
        let app = launch(screen: "charts")
        assertScreenRenders(app)
    }

    func testClubStatsScreenRenders() {
        let app = launch(screen: "clubstats")
        assertScreenRenders(app)
    }

    func testSeriesPointsScreenRenders() {
        let app = launch(screen: "seriespoints")
        assertScreenRenders(app)
    }

    func testClubScreenRenders() {
        let app = launch(screen: "club")
        // ClubScreen's three tab labels are stable copy.
        assertLabel(app, "Information")
        assertLabel(app, "Administratörer")
        assertLabel(app, "Medlemmar")
    }

    func testSettingsScreenRenders() {
        let app = launch(screen: "settings")
        // Tabs: "Profil" + "Lösenord".
        assertLabel(app, "Profil")
        assertLabel(app, "Lösenord")
    }

    func testLicensesScreenRenders() {
        let app = launch(screen: "licenses")
        assertLabel(app, "Apache License 2.0")
    }

    func testResultsScreenRenders() {
        let app = launch(screen: "results")
        // ResultsScreen renders the competition name in the top bar.
        assertLabel(app, "Vintercup")
    }

    func testShooterResultScreenRenders() {
        let app = launch(screen: "shooter")
        assertScreenRenders(app)
    }

    func testSignupsListScreenRenders() {
        let app = launch(screen: "signupslist")
        assertScreenRenders(app)
    }

    func testPatrolsScreenRenders() {
        let app = launch(screen: "patrols")
        assertScreenRenders(app)
    }

    func testTeamsScreenRenders() {
        let app = launch(screen: "teams")
        assertScreenRenders(app)
    }

    func testSignupScreenRenders() {
        let app = launch(screen: "signup")
        // Signup screen has the weapon-class dropdown label.
        assertLabel(app, "Klass")
    }

}
