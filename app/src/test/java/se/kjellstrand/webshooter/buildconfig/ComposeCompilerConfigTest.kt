package se.kjellstrand.webshooter.buildconfig

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ComposeCompilerConfigTest {

    private val buildGradle = File("build.gradle.kts")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `build gradle does not contain stale composeOptions block`() {
        val content = buildGradle.readText()
        assertFalse(
            "composeOptions block with kotlinCompilerExtensionVersion is stale when using the Compose compiler Gradle plugin",
            content.contains("composeOptions")
        )
    }

    @Test
    fun `build gradle does not hardcode kotlinCompilerExtensionVersion`() {
        val content = buildGradle.readText()
        assertFalse(
            "kotlinCompilerExtensionVersion should not be hardcoded — the Compose compiler plugin handles this",
            content.contains("kotlinCompilerExtensionVersion")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `build gradle exists`() {
        assertTrue(buildGradle.exists())
    }

    @Test
    fun `build gradle uses compose compiler plugin`() {
        val content = buildGradle.readText()
        assertTrue(
            "Should use the Compose compiler Gradle plugin",
            content.contains("compose.compiler") || content.contains("compose-compiler") || content.contains("compose_compiler")
        )
    }

    @Test
    fun `compose is enabled in build features`() {
        val content = buildGradle.readText()
        assertTrue(content.contains("compose = true"))
    }
}
