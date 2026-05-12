package se.kjellstrand.webshooter.ui.mock

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class MockDataFilesTest {

    private val mockDir = "../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/mock"

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `MockClub file should exist`() {
        assertTrue("MockClub.kt should exist", File("$mockDir/MockClub.kt").exists())
    }

    @Test
    fun `MockPatrols file should exist`() {
        assertTrue("MockPatrols.kt should exist", File("$mockDir/MockPatrols.kt").exists())
    }

    @Test
    fun `MockSettings file should exist`() {
        assertTrue("MockSettings.kt should exist", File("$mockDir/MockSettings.kt").exists())
    }

    @Test
    fun `MockSignups file should exist`() {
        assertTrue("MockSignups.kt should exist", File("$mockDir/MockSignups.kt").exists())
    }

    @Test
    fun `MockTeams file should exist`() {
        assertTrue("MockTeams.kt should exist", File("$mockDir/MockTeams.kt").exists())
    }

    @Test
    fun `MockShooterResult file should exist`() {
        assertTrue("MockShooterResult.kt should exist", File("$mockDir/MockShooterResult.kt").exists())
    }

    @Test
    fun `MockMyResults file should exist`() {
        assertTrue("MockMyResults.kt should exist", File("$mockDir/MockMyResults.kt").exists())
    }

    @Test
    fun `MockSignup file should exist`() {
        assertTrue("MockSignup.kt should exist", File("$mockDir/MockSignup.kt").exists())
    }

    @Test
    fun `MockClub should instantiate via reflection with club data having 5+ members`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.mock.MockClub")
        val instance = clazz.getDeclaredConstructor().newInstance()
        val clubData = clazz.getMethod("getClubData").invoke(instance)!!
        val admins = clubData.javaClass.getMethod("getAdmins").invoke(clubData) as List<*>
        assertTrue("MockClub should have 5+ admins", admins.size >= 5)
        val users = clubData.javaClass.getMethod("getUsers").invoke(clubData) as List<*>
        assertTrue("MockClub should have 5+ users", users.size >= 5)
    }

    @Test
    fun `MockTeams should have 5+ teams via reflection`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.mock.MockTeams")
        val instance = clazz.getDeclaredConstructor().newInstance()
        val teams = clazz.getMethod("getTeams").invoke(instance) as List<*>
        assertTrue("MockTeams should have 5+ teams", teams.size >= 5)
    }

    @Test
    fun `MockSignups should have 5+ signups via reflection`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.mock.MockSignups")
        val instance = clazz.getDeclaredConstructor().newInstance()
        val signups = clazz.getMethod("getSignups").invoke(instance) as List<*>
        assertTrue("MockSignups should have 5+ signups", signups.size >= 5)
    }

    @Test
    fun `MockPatrols should have 5+ patrols via reflection`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.mock.MockPatrols")
        val instance = clazz.getDeclaredConstructor().newInstance()
        val patrols = clazz.getMethod("getPatrols").invoke(instance) as List<*>
        assertTrue("MockPatrols should have 5+ patrols", patrols.size >= 5)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `MockCompetitions class should still provide valid data`() {
        val mock = MockCompetitions()
        assertNotNull(mock.competitions)
        assertTrue(mock.competitions.data.isNotEmpty())
    }

    @Test
    fun `MockResults class should still provide valid data`() {
        val mock = MockResults()
        assertTrue(mock.results.isNotEmpty())
    }

    @Test
    fun `existing mock files should exist`() {
        assertTrue("MockCompetitions.kt should exist", File("$mockDir/MockCompetitions.kt").exists())
        assertTrue("MockResults.kt should exist", File("$mockDir/MockResults.kt").exists())
    }
}
