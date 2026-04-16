package se.kjellstrand.webshooter.ui.screens.clubstats

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ClubStatsScreenTest {

    private val sourceFile = File("src/main/java/se/kjellstrand/webshooter/ui/screens/clubstats/ClubStatsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ClubStatsScreen file exists`() {
        assertTrue(
            "ClubStatsScreen.kt must exist",
            sourceFile.exists()
        )
    }

    @Test
    fun `ClubStatsScreen has composable function`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsScreen must have @Composable annotation",
            source.contains("@Composable")
        )
        assertTrue(
            "ClubStatsScreen must have ClubStatsScreen function",
            source.contains("fun ClubStatsScreen(")
        )
    }

    @Test
    fun `ClubStatsScreen takes ClubStatsViewModel parameter`() {
        val source = sourceFile.readText()
        val pattern = Regex("""fun\s+ClubStatsScreen\s*\([\s\S]*ClubStatsViewModel""")
        assertTrue(
            "ClubStatsScreen must accept ClubStatsViewModel parameter",
            pattern.containsMatchIn(source)
        )
    }

    @Test
    fun `ClubStatsScreen uses collectAsState`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsScreen must use collectAsState",
            source.contains("collectAsState()")
        )
    }

    @Test
    fun `ClubStatsScreen uses ScatterChart via AndroidView`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsScreen must use AndroidView",
            source.contains("AndroidView")
        )
        assertTrue(
            "ClubStatsScreen must use ScatterChart",
            source.contains("ScatterChart")
        )
    }

    @Test
    fun `ClubStatsScreen uses ScatterDataSet`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsScreen must use ScatterDataSet",
            source.contains("ScatterDataSet")
        )
    }

    @Test
    fun `ClubStatsScreen shows loading indicator`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsScreen must show CircularProgressIndicator when loading",
            source.contains("CircularProgressIndicator")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartsScreen still exists`() {
        assertTrue(
            File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/ChartsScreen.kt").exists()
        )
    }

    @Test
    fun `ClubStatsUiState still exists`() {
        assertTrue(
            File("src/main/java/se/kjellstrand/webshooter/ui/screens/clubstats/ClubStatsUiState.kt").exists()
        )
    }

    @Test
    fun `ClubStatsViewModel still exists`() {
        assertTrue(
            File("src/main/java/se/kjellstrand/webshooter/ui/screens/clubstats/ClubStatsViewModel.kt").exists()
        )
    }
}