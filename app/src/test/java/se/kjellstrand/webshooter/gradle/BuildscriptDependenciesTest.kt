package se.kjellstrand.webshooter.gradle

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BuildscriptDependenciesTest {

    private val buildFile = File("../build.gradle.kts")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `project buildscript should include sqlite-jdbc dependency`() {
        val content = buildFile.readText()
        assertTrue(
            "buildscript must include sqlite-jdbc for prebuilt database generation",
            content.contains("sqlite-jdbc")
        )
    }

    @Test
    fun `project buildscript should include okhttp dependency`() {
        val content = buildFile.readText()
        assertTrue(
            "buildscript must include okhttp for API calls in prebuilt database task",
            content.contains("okhttp") && content.contains("buildscript")
        )
    }

    @Test
    fun `project buildscript should include gson dependency`() {
        val content = buildFile.readText()
        assertTrue(
            "buildscript must include gson for JSON parsing in prebuilt database task",
            content.contains("gson") && content.contains("dependencies")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `project buildscript should have repositories block`() {
        val content = buildFile.readText()
        assertTrue(
            "buildscript must have repositories for dependency resolution",
            content.contains("repositories")
        )
    }

    @Test
    fun `project buildscript should include mavenCentral`() {
        val content = buildFile.readText()
        assertTrue(
            "buildscript must include mavenCentral repository",
            content.contains("mavenCentral()")
        )
    }
}
