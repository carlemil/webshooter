package se.kjellstrand.webshooter.ui.common

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class SharedComposablesTest {

    private val commonDir = File("src/main/java/se/kjellstrand/webshooter/ui/common")
    private val patrolsScreen = File("src/main/java/se/kjellstrand/webshooter/ui/screens/patrols/PatrolsScreen.kt")
    private val teamsScreen = File("src/main/java/se/kjellstrand/webshooter/ui/screens/teams/TeamsScreen.kt")
    private val signupsScreen = File("src/main/java/se/kjellstrand/webshooter/ui/screens/signups/SignupsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `GroupCardHeader composable exists in common`() {
        val commonFiles = commonDir.listFiles()?.map { it.readText() }?.joinToString("\n") ?: ""
        assertTrue(
            "A shared GroupCardHeader composable should exist in ui/common/",
            commonFiles.contains("fun GroupCardHeader(")
        )
    }

    @Test
    fun `PatrolsScreen uses shared GroupCardHeader`() {
        val source = patrolsScreen.readText()
        assertTrue(
            "PatrolsScreen should use shared GroupCardHeader composable",
            source.contains("GroupCardHeader")
        )
    }

    @Test
    fun `TeamsScreen uses shared GroupCardHeader`() {
        val source = teamsScreen.readText()
        assertTrue(
            "TeamsScreen should use shared GroupCardHeader composable",
            source.contains("GroupCardHeader")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `PatrolsScreen exists`() {
        assertTrue(patrolsScreen.exists())
    }

    @Test
    fun `TeamsScreen exists`() {
        assertTrue(teamsScreen.exists())
    }

    @Test
    fun `SignupsScreen exists`() {
        assertTrue(signupsScreen.exists())
    }

    @Test
    fun `PatrolsScreen still renders patrol info`() {
        val source = patrolsScreen.readText()
        assertTrue(source.contains("PatrolHeaderItem") || source.contains("GroupCardHeader"))
    }
}
