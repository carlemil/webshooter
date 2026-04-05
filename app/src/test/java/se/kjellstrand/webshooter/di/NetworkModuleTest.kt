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
        val hasUnconditionalBasic = Regex("""level\s*=\s*HttpLoggingInterceptor\.Level\.BASIC""")
            .containsMatchIn(source)
        assertFalse(
            "HttpLoggingInterceptor level should not be unconditionally BASIC in production",
            hasUnconditionalBasic
        )
    }

    @Test
    fun `HTTP logging references BuildConfig DEBUG`() {
        val source = sourceFile.readText()
        assertTrue(
            "HTTP logging should use BuildConfig.DEBUG to determine log level",
            source.contains("BuildConfig.DEBUG") && source.contains("HttpLoggingInterceptor")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source file exists`() {
        assertTrue(sourceFile.exists())
    }

    @Test
    fun `OkHttpClient is configured with interceptors`() {
        val source = sourceFile.readText()
        assertTrue(source.contains("addInterceptor"))
    }
}
