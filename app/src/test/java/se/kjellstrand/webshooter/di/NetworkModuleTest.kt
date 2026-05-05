package se.kjellstrand.webshooter.di

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class NetworkModuleTest {

    private val networkModuleFile = File("src/main/java/se/kjellstrand/webshooter/di/NetworkModule.kt")
    private val httpFactoryFile = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/data/HttpClientFactory.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `HTTP logging level is conditional on debug build`() {
        val source = httpFactoryFile.readText()
        val hasUnconditionalHeaders = Regex("""level\s*=\s*LogLevel\.(HEADERS|ALL|BODY|INFO)\s*$""", RegexOption.MULTILINE)
            .containsMatchIn(source)
        assertFalse(
            "Ktor Logging level should not be unconditionally verbose in production",
            hasUnconditionalHeaders
        )
    }

    @Test
    fun `HTTP logging level is gated on a debug flag`() {
        val networkModuleSource = networkModuleFile.readText()
        val httpFactorySource = httpFactoryFile.readText()
        assertTrue(
            "NetworkModule must pass BuildConfig.DEBUG into the shared HTTP configurator",
            networkModuleSource.contains("BuildConfig.DEBUG") &&
                networkModuleSource.contains("configureWebshooterHttpClient")
        )
        assertTrue(
            "HttpClientFactory must gate the Logging plugin on the isDebug flag",
            httpFactorySource.contains("install(Logging)") &&
                httpFactorySource.contains("if (isDebug)")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source files exist`() {
        assertTrue(networkModuleFile.exists())
        assertTrue(httpFactoryFile.exists())
    }

    @Test
    fun `HTTP client is configured with the mock interceptor`() {
        val source = networkModuleFile.readText()
        assertTrue(
            "Mock interceptor must remain wired into the OkHttp engine",
            source.contains("addInterceptor(mockInterceptor)")
        )
    }
}
