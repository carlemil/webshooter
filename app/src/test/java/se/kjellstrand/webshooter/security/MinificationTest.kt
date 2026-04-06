package se.kjellstrand.webshooter.security

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class MinificationTest {

    private val buildGradle = File("build.gradle.kts")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `release build has minification enabled`() {
        val content = buildGradle.readText()
        val releaseBlock = content.substringAfter("release {").substringBefore("}")
        assertTrue(
            "Release build should have isMinifyEnabled = true",
            releaseBlock.contains("isMinifyEnabled = true")
        )
    }

    @Test
    fun `release build does not have minification disabled`() {
        val content = buildGradle.readText()
        val releaseBlock = content.substringAfter("release {").substringBefore("}")
        assertFalse(
            "Release build should not have isMinifyEnabled = false",
            releaseBlock.contains("isMinifyEnabled = false")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `build gradle exists`() {
        assertTrue(buildGradle.exists())
    }

    @Test
    fun `release build type is defined`() {
        val content = buildGradle.readText()
        assertTrue(content.contains("release {"))
    }

    @Test
    fun `release build references proguard files`() {
        val content = buildGradle.readText()
        assertTrue(content.contains("proguardFiles"))
    }
}
