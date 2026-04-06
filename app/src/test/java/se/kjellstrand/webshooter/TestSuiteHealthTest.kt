package se.kjellstrand.webshooter

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Meta-test that ensures the test suite meets minimum coverage requirements.
 * Guards against regression to near-zero test coverage.
 */
class TestSuiteHealthTest {

    private val testRoot = File("src/test/java/se/kjellstrand/webshooter")

    @Test
    fun `test suite has at least 30 test files`() {
        val testFiles = testRoot.walkTopDown()
            .filter { it.isFile && it.name.endsWith("Test.kt") }
            .toList()
        assertTrue(
            "Expected at least 30 test files but found ${testFiles.size}",
            testFiles.size >= 30
        )
    }

    @Test
    fun `test suite covers data layer`() {
        val dataTests = File(testRoot, "data").walkTopDown()
            .filter { it.isFile && it.name.endsWith("Test.kt") }
            .toList()
        assertTrue(
            "Expected data layer tests but found ${dataTests.size}",
            dataTests.size >= 5
        )
    }

    @Test
    fun `test suite covers UI layer`() {
        val uiTests = File(testRoot, "ui").walkTopDown()
            .filter { it.isFile && it.name.endsWith("Test.kt") }
            .toList()
        assertTrue(
            "Expected UI layer tests but found ${uiTests.size}",
            uiTests.size >= 10
        )
    }

    @Test
    fun `test suite covers security`() {
        val securityTests = File(testRoot, "security").walkTopDown()
            .filter { it.isFile && it.name.endsWith("Test.kt") }
            .toList()
        assertTrue(
            "Expected security tests but found ${securityTests.size}",
            securityTests.size >= 2
        )
    }

    @Test
    fun `test suite covers navigation`() {
        val navTests = File(testRoot, "ui/navigation").walkTopDown()
            .filter { it.isFile && it.name.endsWith("Test.kt") }
            .toList()
        assertTrue(
            "Expected navigation tests but found ${navTests.size}",
            navTests.size >= 2
        )
    }
}
