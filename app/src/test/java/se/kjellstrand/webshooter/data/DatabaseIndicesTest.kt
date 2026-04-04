package se.kjellstrand.webshooter.data

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DatabaseIndicesTest {

    private val teamEntityFile = File("src/main/java/se/kjellstrand/webshooter/data/competitionteams/local/TeamEntity.kt")
    private val patrolEntityFile = File("src/main/java/se/kjellstrand/webshooter/data/competitionpatrols/local/PatrolEntity.kt")
    private val signupEntityFile = File("src/main/java/se/kjellstrand/webshooter/data/competitionsignups/local/CompetitionSignupEntity.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `TeamEntity has index on competitionId`() {
        val source = teamEntityFile.readText()
        assertTrue(
            "TeamEntity should have @Entity indices for competitionId",
            source.contains("Index") && source.contains("competitionId")
                    && source.contains("indices")
        )
    }

    @Test
    fun `PatrolEntity has index on competitionId`() {
        val source = patrolEntityFile.readText()
        assertTrue(
            "PatrolEntity should have @Entity indices for competitionId",
            source.contains("Index") && source.contains("competitionId")
                    && source.contains("indices")
        )
    }

    @Test
    fun `CompetitionSignupEntity has index on competitionId`() {
        val source = signupEntityFile.readText()
        assertTrue(
            "CompetitionSignupEntity should have @Entity indices for competitionId",
            source.contains("Index") && source.contains("competitionId")
                    && source.contains("indices")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `TeamEntity file exists`() {
        assertTrue(teamEntityFile.exists())
    }

    @Test
    fun `PatrolEntity file exists`() {
        assertTrue(patrolEntityFile.exists())
    }

    @Test
    fun `CompetitionSignupEntity file exists`() {
        assertTrue(signupEntityFile.exists())
    }
}
