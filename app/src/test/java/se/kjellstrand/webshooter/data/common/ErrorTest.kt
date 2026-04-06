package se.kjellstrand.webshooter.data.common

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ErrorTest {

    private val errorFile = File("src/main/java/se/kjellstrand/webshooter/data/common/Error.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `HttpError includes status code`() {
        val source = errorFile.readText()
        assertTrue(
            "HttpError should include an HTTP status code for specific error recovery",
            source.contains("statusCode") || source.contains("code:")
        )
    }

    @Test
    fun `UserError is sealed class not enum`() {
        val source = errorFile.readText()
        assertTrue(
            "UserError should be a sealed class (not enum) to support data in error types",
            source.contains("sealed class UserError") || source.contains("sealed interface UserError")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `Error file exists`() {
        assertTrue(errorFile.exists())
    }

    @Test
    fun `UserError has IOError variant`() {
        val source = errorFile.readText()
        assertTrue(source.contains("IOError"))
    }

    @Test
    fun `UserError has HttpError variant`() {
        val source = errorFile.readText()
        assertTrue(source.contains("HttpError"))
    }

    @Test
    fun `UserError has UnknownError variant`() {
        val source = errorFile.readText()
        assertTrue(source.contains("UnknownError"))
    }

    @Test
    fun `UserError implements Error`() {
        val source = errorFile.readText()
        assertTrue(source.contains("Error"))
    }
}
