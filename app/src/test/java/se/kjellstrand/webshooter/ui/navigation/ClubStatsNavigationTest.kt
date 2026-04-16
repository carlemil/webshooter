package se.kjellstrand.webshooter.ui.navigation

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ClubStatsNavigationTest {

    private val screenFile = File("src/main/java/se/kjellstrand/webshooter/ui/navigation/Screen.kt")
    private val webShooterScreenFile = File("src/main/java/se/kjellstrand/webshooter/ui/landingscreen/WebShooterScreen.kt")
    private val stringsFile = File("src/main/res/values/strings.xml")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `Screen kt contains ClubStats object`() {
        val source = screenFile.readText()
        assertTrue(
            "Screen.kt must contain object ClubStats",
            source.contains("object ClubStats")
        )
    }

    @Test
    fun `ClubStats route is club_stats`() {
        val source = screenFile.readText()
        assertTrue(
            "ClubStats route must be 'club_stats'",
            source.contains("\"club_stats\"")
        )
    }

    @Test
    fun `WebShooterScreen has ClubStats navigation item`() {
        val source = webShooterScreenFile.readText()
        assertTrue(
            "WebShooterScreen must reference Screen.ClubStats.route in navigation items",
            source.contains("Screen.ClubStats.route")
        )
    }

    @Test
    fun `WebShooterScreen has ClubStats composable route`() {
        val source = webShooterScreenFile.readText()
        val pattern = Regex("""composable\s*\(\s*Screen\.ClubStats\.route\s*\)""")
        assertTrue(
            "WebShooterScreen must have composable(Screen.ClubStats.route) block",
            pattern.containsMatchIn(source)
        )
    }

    @Test
    fun `WebShooterScreen renders ClubStatsScreen in NavHost`() {
        val source = webShooterScreenFile.readText()
        assertTrue(
            "WebShooterScreen must render ClubStatsScreen",
            source.contains("ClubStatsScreen(")
        )
    }

    @Test
    fun `strings xml has club stats drawer label`() {
        val source = stringsFile.readText()
        assertTrue(
            "strings.xml must contain web_shooter_club_stats string resource",
            source.contains("web_shooter_club_stats")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `Screen kt still contains Charts object`() {
        val source = screenFile.readText()
        assertTrue(source.contains("object Charts"))
    }

    @Test
    fun `Screen kt still contains SeriesPoints object`() {
        val source = screenFile.readText()
        assertTrue(source.contains("object SeriesPoints"))
    }

    @Test
    fun `WebShooterScreen still has Charts composable`() {
        val source = webShooterScreenFile.readText()
        assertTrue(source.contains("Screen.Charts.route"))
    }

    @Test
    fun `WebShooterScreen still has SeriesPoints composable`() {
        val source = webShooterScreenFile.readText()
        assertTrue(source.contains("Screen.SeriesPoints.route"))
    }
}
