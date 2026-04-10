package se.kjellstrand.webshooter.gradle

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GeneratePrebuiltDatabaseTaskTest {

    private val buildFile = File("build.gradle.kts")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `build file should register generatePrebuiltDatabase task`() {
        val content = buildFile.readText()
        assertTrue(
            "build.gradle.kts must register a generatePrebuiltDatabase task",
            content.contains("generatePrebuiltDatabase")
        )
    }

    @Test
    fun `generatePrebuiltDatabase task should reference schema location`() {
        val content = buildFile.readText()
        assertTrue(
            "task must read the Room schema JSON to create the database",
            content.contains("schemas") && content.contains("AppDatabase")
        )
    }

    @Test
    fun `generatePrebuiltDatabase task should create competition_fetch_status table`() {
        val content = buildFile.readText()
        assertTrue(
            "task must create a competition_fetch_status tracking table",
            content.contains("competition_fetch_status")
        )
    }

    @Test
    fun `generatePrebuiltDatabase task should use sqlite-jdbc`() {
        val content = buildFile.readText()
        assertTrue(
            "task must use SQLite JDBC to create the database file",
            content.contains("jdbc:sqlite")
        )
    }

    @Test
    fun `generatePrebuiltDatabase task should call results API`() {
        val content = buildFile.readText()
        assertTrue(
            "task must call the webshooter results API",
            content.contains("competitions") && content.contains("results")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `build file should have ksp room schema location configured`() {
        val content = buildFile.readText()
        assertTrue(
            "KSP room.schemaLocation must be configured",
            content.contains("room.schemaLocation")
        )
    }

    @Test
    fun `build file should have generateDbVersion task`() {
        val content = buildFile.readText()
        assertTrue(
            "generateDbVersion task must exist for DB version generation",
            content.contains("generateDbVersion")
        )
    }
}
