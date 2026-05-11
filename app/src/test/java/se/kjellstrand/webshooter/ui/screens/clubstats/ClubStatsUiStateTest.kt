package se.kjellstrand.webshooter.ui.screens.clubstats

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ClubStatsUiStateTest {

    private val sourceFile = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/clubstats/ClubStatsUiState.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ClubStatsUiState file exists`() {
        assertTrue(
            "ClubStatsUiState.kt must exist",
            sourceFile.exists()
        )
    }

    @Test
    fun `ClubStatsUiState has shooterStats field`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsUiState must have shooterStats field",
            source.contains("shooterStats")
        )
    }

    @Test
    fun `ClubStatsUiState has isLoading field`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsUiState must have isLoading field",
            source.contains("isLoading")
        )
    }

    @Test
    fun `ClubStatsUiState has hasError field`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsUiState must have hasError field",
            source.contains("hasError")
        )
    }

    @Test
    fun `ClubStatsUiState references ShooterStats type`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsUiState must reference ShooterStats",
            source.contains("ShooterStats")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ResultsTrendsUiState still exists`() {
        assertTrue(
            File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsUiState.kt").exists()
        )
    }

    @Test
    fun `ShooterStats data class exists in clubstats package`() {
        assertTrue(
            File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/data/clubstats/ShooterStats.kt").exists()
        )
    }
}
