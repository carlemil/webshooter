package se.kjellstrand.webshooter

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Source-level guards for [ShooterApplication]. After the Koin migration the
 * dependencies are no longer Hilt-injected `lateinit var` fields; instead
 * they are resolved inline via `KoinPlatform.getKoin().get<...>()`. The
 * checks below confirm Koin is bootstrapped at the right time and the
 * startup-sync side-effect is preserved.
 */
class ShooterApplicationStartupSyncTest {

    private val applicationSource: String by lazy {
        File("src/main/java/se/kjellstrand/webshooter/ShooterApplication.kt").readText()
    }

    @Test
    fun `ShooterApplication initializes Koin before super onCreate`() {
        // initKoin must run BEFORE super.onCreate() — anything else breaks
        // the contract that subsequent VM construction can resolve dependencies.
        val initKoinIdx = applicationSource.indexOf("initKoin(")
        val superOnCreateIdx = applicationSource.indexOf("super.onCreate(")
        assertTrue("initKoin(...) must appear in onCreate", initKoinIdx >= 0)
        assertTrue("super.onCreate() must appear in onCreate", superOnCreateIdx >= 0)
        assertTrue(
            "initKoin(...) must be called before super.onCreate()",
            initKoinIdx < superOnCreateIdx
        )
    }

    @Test
    fun `ShooterApplication onCreate calls syncAll on the repository`() {
        assertTrue(
            "ShooterApplication source should call competitionsRepository.syncAll()",
            applicationSource.contains("competitionsRepository.syncAll()")
        )
    }

    @Test
    fun `ShooterApplication onCreate no longer calls syncCompleted directly`() {
        assertFalse(
            "ShooterApplication should not call syncCompleted() — that's now an internal step of syncAll()",
            applicationSource.contains("competitionsRepository.syncCompleted()")
        )
    }

    @Test
    fun `ShooterApplication onCreate no longer references the cache TTL`() {
        assertFalse(
            "Startup should sync unconditionally — TTL constant must be gone",
            applicationSource.contains("COMPETITIONS_CACHE_TTL_MS")
        )
        assertFalse(
            "Startup should not gate sync on the last-sync timestamp",
            applicationSource.contains("getCompetitionsLastSync")
        )
        assertFalse(
            "Startup should not stamp the last-sync timestamp",
            applicationSource.contains("setCompetitionsLastSync")
        )
    }

    @Test
    fun `ShooterApplication uses the Koin ApplicationCoroutineScope qualifier`() {
        assertTrue(
            "ShooterApplication source should resolve the scope via ApplicationCoroutineScopeQualifier",
            applicationSource.contains("ApplicationCoroutineScopeQualifier")
        )
    }

    @Test
    fun `ShooterApplication no longer constructs its own CoroutineScope`() {
        assertFalse(
            "ShooterApplication should not construct CoroutineScope(SupervisorJob() + ...) — it should resolve it from Koin",
            applicationSource.contains("CoroutineScope(SupervisorJob()")
        )
    }

    @Test
    fun `ShooterApplication no longer uses Hilt`() {
        val annotation = ShooterApplication::class.java.annotations.find {
            it.annotationClass.simpleName == "HiltAndroidApp"
        }
        assertTrue(
            "ShooterApplication should NOT carry @HiltAndroidApp anymore (Koin replaced it)",
            annotation == null
        )
        assertFalse(
            "ShooterApplication source should not contain dagger.hilt imports",
            applicationSource.contains("dagger.hilt")
        )
        assertFalse(
            "ShooterApplication source should not contain @Inject annotations",
            applicationSource.contains("@Inject")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ShooterApplication extends Application`() {
        assertTrue(
            "ShooterApplication should extend Application",
            android.app.Application::class.java.isAssignableFrom(ShooterApplication::class.java)
        )
    }

    @Test
    fun `ShooterApplication has onCreate method`() {
        val method = ShooterApplication::class.java.methods.find { it.name == "onCreate" }
        assertNotNull("ShooterApplication should have onCreate", method)
    }
}
