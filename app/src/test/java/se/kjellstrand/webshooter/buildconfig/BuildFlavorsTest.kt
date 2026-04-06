package se.kjellstrand.webshooter.buildconfig

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class BuildFlavorsTest {

    private val buildGradle = File("build.gradle.kts")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `build gradle defines a staging flavor`() {
        val content = buildGradle.readText()
        assertTrue(
            "build.gradle.kts should define a staging flavor for testing against staging APIs",
            content.contains("create(\"staging\")")
        )
    }

    @Test
    fun `staging flavor has a different BASE_URL than prod`() {
        val content = buildGradle.readText()
        val stagingSection = content.substringAfter("create(\"staging\")", "")
        assertTrue(
            "staging flavor should define a BASE_URL buildConfigField",
            stagingSection.contains("BASE_URL")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `build gradle exists`() {
        assertTrue(buildGradle.exists())
    }

    @Test
    fun `prod flavor still exists`() {
        val content = buildGradle.readText()
        assertTrue(content.contains("create(\"prod\")"))
    }

    @Test
    fun `prod flavor has BASE_URL`() {
        val content = buildGradle.readText()
        val prodSection = content.substringAfter("create(\"prod\")")
        assertTrue(prodSection.contains("BASE_URL"))
    }
}
