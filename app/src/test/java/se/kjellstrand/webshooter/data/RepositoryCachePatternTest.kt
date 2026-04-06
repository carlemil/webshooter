package se.kjellstrand.webshooter.data

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class RepositoryCachePatternTest {

    private val patrolsRepo = File("src/main/java/se/kjellstrand/webshooter/data/competitionpatrols/CompetitionPatrolsRepository.kt")
    private val teamsRepo = File("src/main/java/se/kjellstrand/webshooter/data/competitionteams/CompetitionTeamsRepository.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `CompetitionPatrolsRepository uses cachedResourceFlow`() {
        val source = patrolsRepo.readText()
        assertTrue(
            "CompetitionPatrolsRepository should use cachedResourceFlow instead of duplicating cache logic",
            source.contains("cachedResourceFlow")
        )
    }

    @Test
    fun `CompetitionPatrolsRepository does not duplicate try-catch error handling`() {
        val source = patrolsRepo.readText()
        val ioCatchCount = Regex("catch.*IOException").findAll(source).count()
        assertFalse(
            "CompetitionPatrolsRepository should not have manual IOException catch blocks (handled by cachedResourceFlow)",
            ioCatchCount > 0
        )
    }

    @Test
    fun `CompetitionTeamsRepository uses cachedResourceFlow`() {
        val source = teamsRepo.readText()
        assertTrue(
            "CompetitionTeamsRepository should use cachedResourceFlow instead of duplicating cache logic",
            source.contains("cachedResourceFlow")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `CompetitionPatrolsRepository exists`() {
        assertTrue(patrolsRepo.exists())
    }

    @Test
    fun `CompetitionTeamsRepository exists`() {
        assertTrue(teamsRepo.exists())
    }

    @Test
    fun `repositories return Flow of Resource`() {
        val patrolsSource = patrolsRepo.readText()
        val teamsSource = teamsRepo.readText()
        assertTrue(patrolsSource.contains("Flow<Resource<"))
        assertTrue(teamsSource.contains("Flow<Resource<"))
    }
}
