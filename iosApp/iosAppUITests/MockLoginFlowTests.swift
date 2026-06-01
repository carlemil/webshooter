import XCTest

/// Drives the real `AppNavHost` via the mockuser/mockpassword credentials
/// (handled by `IosMockInterceptor`). Covers the login form interactions,
/// drawer navigation entries, and per-competition detail buttons that the
/// `DebugGallery` smoke tests can't reach.
final class MockLoginFlowTests: XCTestCase {

    override func setUp() {
        continueAfterFailure = false
    }

    // MARK: helpers

    private func waitForAnyLabel(_ app: XCUIApplication, _ label: String, timeout: TimeInterval = 20) {
        let predicate = NSPredicate(format: "label == %@ OR label CONTAINS[c] %@", label, label)
        let exists = NSPredicate { _, _ in
            app.staticTexts.matching(predicate).firstMatch.exists
                || app.buttons.matching(predicate).firstMatch.exists
                || app.otherElements.matching(predicate).firstMatch.exists
        }
        let exp = XCTNSPredicateExpectation(predicate: exists, object: nil)
        XCTAssertEqual(
            XCTWaiter().wait(for: [exp], timeout: timeout),
            .completed,
            "Timed out waiting for '\(label)'"
        )
    }

    /// Tap the first node with the exact label. Falls back to a coordinate
    /// tap when the element exists but isn't hittable (Compose Multiplatform
    /// iOS often nests interactive elements behind transparent Other layers
    /// that XCUITest considers non-hit-testable).
    private func tapFirst(_ app: XCUIApplication, _ label: String) {
        let predicate = NSPredicate(format: "label == %@", label)
        let button = app.buttons.matching(predicate).firstMatch
        if button.waitForExistence(timeout: 10) {
            if button.isHittable {
                button.tap()
            } else {
                button.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
            }
            return
        }
        let text = app.staticTexts.matching(predicate).firstMatch
        if text.exists {
            if text.isHittable {
                text.tap()
            } else {
                text.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
            }
            return
        }
        let other = app.otherElements.matching(predicate).firstMatch
        if other.exists {
            other.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
            return
        }
        XCTFail("Could not find tappable '\(label)'")
    }

    /// Launches with `AUTOLOGIN=mock` so `MainViewController` seeds the auth
    /// manager with a fake mock-mode token before the splash screen runs —
    /// SplashScreen then routes straight to the landing scaffold. Bypasses
    /// the Compose login form, which doesn't accept synthetic XCUITest
    /// keystrokes (Compose Multiplatform on iOS bridges OutlinedTextField as
    /// a button, not a UITextField).
    private func launchAutoLoggedIn() -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["AUTOLOGIN"] = "mock"
        app.launch()
        // The top bar's "Meny" hamburger button is the landing-screen
        // identifying element.
        XCTAssertTrue(
            app.buttons["Meny"].waitForExistence(timeout: 30),
            "Landing scaffold did not render — autologin failed?"
        )
        return app
    }

    // MARK: tests

    func testAutoLogin_landsOnLanding() {
        _ = launchAutoLoggedIn()
    }


    func testDrawer_opensAndShowsAllSections() {
        let app = launchAutoLoggedIn()

        // Open drawer via the menu hamburger. The IconButton's accessibility
        // label comes from its contentDescription "Meny".
        tapFirst(app, "Meny")

        // Drawer title and section headers.
        waitForAnyLabel(app, "Meny")
        waitForAnyLabel(app, "Statistik")
        waitForAnyLabel(app, "Förening")
        waitForAnyLabel(app, "Inställningar")
        waitForAnyLabel(app, "Övrigt")

        // Drawer items.
        waitForAnyLabel(app, "Mina resultat")
        waitForAnyLabel(app, "Resultattrender")
        waitForAnyLabel(app, "Serieresultat")
        waitForAnyLabel(app, "Föreningsstatistik")
        waitForAnyLabel(app, "Licenser")
        waitForAnyLabel(app, "Skicka förslag")
    }

    func testDrawer_navigatesToMyEntries() {
        let app = launchAutoLoggedIn()
        tapFirst(app, "Meny")
        tapFirst(app, "Mina resultat")
        waitForAnyLabel(app, "Sammanfattning")
    }

    func testDrawer_navigatesToLicenses() {
        let app = launchAutoLoggedIn()
        tapFirst(app, "Meny")
        tapFirst(app, "Licenser")
        waitForAnyLabel(app, "Apache License 2.0")
    }

    func testDrawer_navigatesToSettings_andOpensEditMode() {
        let app = launchAutoLoggedIn()
        tapFirst(app, "Meny")
        tapFirst(app, "Inställningar")
        waitForAnyLabel(app, "Personlig information")

        // Switch to password tab.
        tapFirst(app, "Lösenord")
        waitForAnyLabel(app, "Ändra lösenord")
        waitForAnyLabel(app, "Uppdatera lösenord")

        // Back to profile tab.
        tapFirst(app, "Profil")
        waitForAnyLabel(app, "Personlig information")

        // Edit button → cancel. Compose's OutlinedTextField exposes prefilled
        // values (not labels) to accessibility, so we look for the Avbryt
        // cancel button as the proof that edit mode is up.
        tapFirst(app, "Redigera profil")
        waitForAnyLabel(app, "Avbryt")
        tapFirst(app, "Avbryt")
        waitForAnyLabel(app, "Personlig information")
    }

    func testDrawer_navigatesToClub_andSwitchesTabs() {
        let app = launchAutoLoggedIn()
        tapFirst(app, "Meny")
        tapFirst(app, "Förening")
        // Tabs.
        waitForAnyLabel(app, "Information")
        tapFirst(app, "Administratörer")
        tapFirst(app, "Medlemmar")
        tapFirst(app, "Information")
    }

    func testCompetition_participantsButton_navigates() {
        let app = launchAutoLoggedIn()
        // "Deltagare" appears as a button on every competition card.
        tapFirst(app, "Deltagare")
        // Back arrow renders on the destination.
        // Back arrow on detail screens uses the default ArrowBack
        // contentDescription "Back".
        waitForAnyLabel(app, "Back")
        tapFirst(app, "Back")
        waitForAnyLabel(app, "Tävlingar")
    }

    func testCompetition_patrolsButton_navigates() {
        let app = launchAutoLoggedIn()
        tapFirst(app, "Patruller")
        // Back arrow on detail screens uses the default ArrowBack
        // contentDescription "Back".
        waitForAnyLabel(app, "Back")
        tapFirst(app, "Back")
        waitForAnyLabel(app, "Tävlingar")
    }

    func testCompetition_expandCard_revealsContactInfo() {
        let app = launchAutoLoggedIn()
        // Expand button contentDescription is "Expandera".
        tapFirst(app, "Expandera")
        waitForAnyLabel(app, "Kontaktinformation")
    }

    func testSettings_logoutDialog_cancels() {
        let app = launchAutoLoggedIn()
        tapFirst(app, "Meny")
        tapFirst(app, "Inställningar")
        waitForAnyLabel(app, "Personlig information")
        tapFirst(app, "Logga ut")
        waitForAnyLabel(app, "säker")
        tapFirst(app, "Nej")
        waitForAnyLabel(app, "Personlig information")
    }

    // MARK: parity with Android DrawerNavigationTest

    func testDrawer_navigatesToCharts() {
        let app = launchAutoLoggedIn()
        tapFirst(app, "Meny")
        tapFirst(app, "Resultattrender")
        // ChartsScreen subtitle.
        waitForAnyLabel(app, "Snittpoäng")
    }

    func testDrawer_navigatesToSeriesPoints() {
        let app = launchAutoLoggedIn()
        tapFirst(app, "Meny")
        tapFirst(app, "Serieresultat")
        // SeriesPointsScreen subtitle.
        waitForAnyLabel(app, "Poäng per serie")
    }

    func testDrawer_navigatesToClubStats() {
        let app = launchAutoLoggedIn()
        tapFirst(app, "Meny")
        tapFirst(app, "Föreningsstatistik")
        // ClubStatsScreen subtitle.
        waitForAnyLabel(app, "Snittpoäng")
    }

    // MARK: parity with Android CompetitionDetailsNavigationTest

    func testCompetition_signupButton_navigatesToSignupScreen() {
        let app = launchAutoLoggedIn()
        tapFirst(app, "Anmälan")
        // SignupScreen back button uses Swedish "Tillbaka" (vs the default
        // "Back" on other detail screens — see SignupScreen.kt).
        waitForAnyLabel(app, "Tillbaka")
        tapFirst(app, "Tillbaka")
        waitForAnyLabel(app, "Meny")
    }

    func testCompetitionsList_filterFab_opensStatusBottomSheet() {
        let app = launchAutoLoggedIn()
        // The filter FAB's contentDescription is "Open Filters".
        tapFirst(app, "Open Filters")
        // The bottom sheet's title is "Status".
        waitForAnyLabel(app, "Status")
        tapFirst(app, "Klar")
    }

    // MARK: parity with Android LoginAndSettingsTest

    func testLoginScreen_passwordVisibilityToggle_clickable() {
        // Use SCREEN=login (DebugGallery) so the login screen renders regardless
        // of any auth state persisted by earlier tests in the same simulator
        // Keychain — XCUIApplication.launch() doesn't wipe Keychain entries.
        let app = XCUIApplication()
        app.launchEnvironment["SCREEN"] = "login"
        app.launch()
        let eye = app.buttons["Visa/dölj lösenord"]
        XCTAssertTrue(eye.waitForExistence(timeout: 15))
        eye.tap()
        eye.tap()
    }
}
