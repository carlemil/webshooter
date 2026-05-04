package se.kjellstrand.webshooter.di

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class NetworkModuleTest {

    private val sourceFile = File("src/main/java/se/kjellstrand/webshooter/di/NetworkModule.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `HTTP logging level is conditional on debug build`() {
        val source = sourceFile.readText()
        val hasUnconditionalHeaders = Regex("""level\s*=\s*LogLevel\.(HEADERS|ALL|BODY|INFO)\s*$""", RegexOption.MULTILINE)
            .containsMatchIn(source)
        assertFalse(
            "Ktor Logging level should not be unconditionally verbose in production",
            hasUnconditionalHeaders
        )
    }

    @Test
    fun `HTTP logging references BuildConfig DEBUG`() {
        val source = sourceFile.readText()
        assertTrue(
            "HTTP logging should use BuildConfig.DEBUG to determine log level",
            source.contains("BuildConfig.DEBUG") && source.contains("Logging")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source file exists`() {
        assertTrue(sourceFile.exists())
    }

    @Test
    fun `HTTP client is configured with the mock interceptor`() {
        val source = sourceFile.readText()
        assertTrue(
            "Mock interceptor must remain wired into the OkHttp engine",
            source.contains("addInterceptor(mockInterceptor)")
        )
    }
}
