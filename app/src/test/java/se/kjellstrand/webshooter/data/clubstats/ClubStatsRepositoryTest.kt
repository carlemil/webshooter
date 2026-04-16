package se.kjellstrand.webshooter.data.clubstats

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ClubStatsRepositoryTest {

    private val baseDir = "src/main/java/se/kjellstrand/webshooter/data/clubstats"

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ShooterStats data class file exists`() {
        assertTrue(
            "ShooterStats.kt must exist",
            File("$baseDir/ShooterStats.kt").exists()
        )
    }

    @Test
    fun `ShooterStats has required fields`() {
        val source = File("$baseDir/ShooterStats.kt").readText()
        listOf("userId", "fullname", "averagePoints", "competitionCount").forEach { field ->
            assertTrue("ShooterStats must have field $field", source.contains(field))
        }
    }

    @Test
    fun `ClubStatsData data class file exists`() {
        assertTrue(
            "ClubStatsData.kt must exist",
            File("$baseDir/ClubStatsData.kt").exists()
        )
    }

    @Test
    fun `ClubStatsData contains shooterStats list`() {
        val source = File("$baseDir/ClubStatsData.kt").readText()
        assertTrue(
            "ClubStatsData must contain shooterStats field",
            source.contains("shooterStats")
        )
        assertTrue(
            "ClubStatsData must reference ShooterStats type",
            source.contains("ShooterStats")
        )
    }

    @Test
    fun `ClubStatsRepository file exists`() {
        assertTrue(
            "ClubStatsRepository.kt must exist",
            File("$baseDir/ClubStatsRepository.kt").exists()
        )
    }

    @Test
    fun `ClubStatsRepository has getClubStats method`() {
        val source = File("$baseDir/ClubStatsRepository.kt").readText()
        assertTrue(
            "ClubStatsRepository must have getClubStats method",
            source.contains("fun getClubStats(")
        )
    }

    @Test
    fun `ClubStatsRepository depends on ClubRepository and ResultsDao`() {
        val source = File("$baseDir/ClubStatsRepository.kt").readText()
        assertTrue(
            "ClubStatsRepository must depend on ClubRepository",
            source.contains("ClubRepository")
        )
        assertTrue(
            "ClubStatsRepository must depend on ResultsDao",
            source.contains("ResultsDao")
        )
    }

    @Test
    fun `ClubStatsRepository returns Flow of Resource`() {
        val source = File("$baseDir/ClubStatsRepository.kt").readText()
        assertTrue(
            "getClubStats must return Flow<Resource<ClubStatsData, UserError>>",
            source.contains("Flow<Resource<ClubStatsData, UserError>>")
        )
    }

    @Test
    fun `ClubStatsRepository calls getUserClub`() {
        val source = File("$baseDir/ClubStatsRepository.kt").readText()
        assertTrue(
            "ClubStatsRepository must call getUserClub()",
            source.contains("getUserClub()")
        )
    }

    @Test
    fun `ClubStatsRepository calls getClubStats on ResultsDao`() {
        val source = File("$baseDir/ClubStatsRepository.kt").readText()
        assertTrue(
            "ClubStatsRepository must call getClubStats on ResultsDao",
            Regex("""resultsDao\.getClubStats|dao\.getClubStats""").containsMatchIn(source)
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ClubStatsRow still exists in results local package`() {
        assertTrue(
            "ClubStatsRow.kt must still exist",
            File("src/main/java/se/kjellstrand/webshooter/data/results/local/ClubStatsRow.kt").exists()
        )
    }

    @Test
    fun `ResultsDao still has getClubStats method`() {
        val source = File("src/main/java/se/kjellstrand/webshooter/data/results/local/ResultsDao.kt").readText()
        assertTrue(
            "ResultsDao must still have getClubStats",
            source.contains("fun getClubStats(")
        )
    }

    @Test
    fun `ClubRepository still exists`() {
        assertTrue(
            "ClubRepository.kt must still exist",
            File("src/main/java/se/kjellstrand/webshooter/data/club/ClubRepository.kt").exists()
        )
    }
}
