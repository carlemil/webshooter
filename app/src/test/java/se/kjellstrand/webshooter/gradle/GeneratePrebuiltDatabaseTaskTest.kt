package se.kjellstrand.webshooter.gradle

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GeneratePrebuiltDatabaseTaskTest {

    private val prebuiltDbFile = File("prebuilt-database.gradle.kts")
    private val buildFile = File("build.gradle.kts")

    // --- Prebuilt database task tests ---

    @Test
    fun `prebuilt database gradle file should exist`() {
        assertTrue(
            "prebuilt-database.gradle.kts must exist",
            prebuiltDbFile.exists()
        )
    }

    @Test
    fun `prebuilt database file should register generatePrebuiltDatabase task`() {
        val content = prebuiltDbFile.readText()
        assertTrue(
            "prebuilt-database.gradle.kts must register a generatePrebuiltDatabase task",
            content.contains("generatePrebuiltDatabase")
        )
    }

    @Test
    fun `generatePrebuiltDatabase task should reference schema location`() {
        val content = prebuiltDbFile.readText()
        assertTrue(
            "task must read the Room schema JSON to create the database",
            content.contains("schemas") && content.contains("AppDatabase")
        )
    }

    @Test
    fun `generatePrebuiltDatabase task should create competition_fetch_status table`() {
        val content = prebuiltDbFile.readText()
        assertTrue(
            "task must create a competition_fetch_status tracking table",
            content.contains("competition_fetch_status")
        )
    }

    @Test
    fun `generatePrebuiltDatabase task should use sqlite-jdbc`() {
        val content = prebuiltDbFile.readText()
        assertTrue(
            "task must use SQLite JDBC to create the database file",
            content.contains("jdbc:sqlite")
        )
    }

    @Test
    fun `generatePrebuiltDatabase task should call results API`() {
        val content = prebuiltDbFile.readText()
        assertTrue(
            "task must call the webshooter results API",
            content.contains("competitions") && content.contains("results")
        )
    }

    // --- Release build wiring tests ---

    @Test
    fun `assembleProdRelease should depend on generatePrebuiltDatabase`() {
        val content = prebuiltDbFile.readText()
        assertTrue(
            "assembleProdRelease must depend on generatePrebuiltDatabase",
            content.contains("assembleProdRelease") && content.contains("dependsOn(generatePrebuiltDatabase)")
        )
    }

    @Test
    fun `bundleProdRelease should depend on generatePrebuiltDatabase`() {
        val content = prebuiltDbFile.readText()
        assertTrue(
            "bundleProdRelease must depend on generatePrebuiltDatabase",
            content.contains("bundleProdRelease") && content.contains("dependsOn(generatePrebuiltDatabase)")
        )
    }

    // --- Guard tests ---

    @Test
    fun `build file should apply prebuilt database gradle file`() {
        val content = buildFile.readText()
        assertTrue(
            "build.gradle.kts must apply prebuilt-database.gradle.kts",
            content.contains("prebuilt-database.gradle.kts")
        )
    }

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
