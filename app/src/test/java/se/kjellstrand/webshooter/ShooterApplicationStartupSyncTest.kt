package se.kjellstrand.webshooter

import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import java.io.File

class ShooterApplicationStartupSyncTest {

    private val applicationSource: String by lazy {
        File("src/main/java/se/kjellstrand/webshooter/ShooterApplication.kt").readText()
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ShooterApplication has competitionsRepository field`() {
        val field = ShooterApplication::class.java.declaredFields.find {
            it.name == "competitionsRepository"
        }
        assertNotNull(
            "ShooterApplication should have a competitionsRepository field for injection",
            field
        )
    }

    @Test
    fun `competitionsRepository field is of correct type`() {
        val field = ShooterApplication::class.java.declaredFields.find {
            it.name == "competitionsRepository"
        }
        assertNotNull("competitionsRepository field should exist", field)
        assertTrue(
            "competitionsRepository should be of type CompetitionsRepository",
            CompetitionsRepository::class.java.isAssignableFrom(field!!.type)
        )
    }

    @Test
    fun `ShooterApplication has authTokenManager field`() {
        val field = ShooterApplication::class.java.declaredFields.find {
            it.name == "authTokenManager"
        }
        assertNotNull(
            "ShooterApplication should have an authTokenManager field for injection",
            field
        )
    }

    @Test
    fun `authTokenManager field is of correct type`() {
        val field = ShooterApplication::class.java.declaredFields.find {
            it.name == "authTokenManager"
        }
        assertNotNull("authTokenManager field should exist", field)
        assertTrue(
            "authTokenManager should be of type AuthTokenManager",
            AuthTokenManager::class.java.isAssignableFrom(field!!.type)
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
    fun `ShooterApplication has applicationScope field of CoroutineScope type`() {
        val field = ShooterApplication::class.java.declaredFields.find {
            it.name == "applicationScope"
        }
        assertNotNull(
            "ShooterApplication should still have an applicationScope field",
            field
        )
        assertTrue(
            "applicationScope should be of type CoroutineScope, was: ${field!!.type.name}",
            CoroutineScope::class.java.isAssignableFrom(field.type)
        )
    }

    @Test
    fun `ShooterApplication no longer constructs its own CoroutineScope`() {
        // Before this fix, the source contained `CoroutineScope(SupervisorJob() + Dispatchers.IO)`.
        // After, it uses the Hilt-injected @ApplicationScope and that literal disappears.
        assertFalse(
            "ShooterApplication should not construct CoroutineScope(SupervisorJob() + ...) — it should inject @ApplicationScope instead",
            applicationSource.contains("CoroutineScope(SupervisorJob()")
        )
    }

    @Test
    fun `ShooterApplication source imports ApplicationScope qualifier`() {
        assertTrue(
            "ShooterApplication source should import se.kjellstrand.webshooter.di.ApplicationScope",
            applicationSource.contains("import se.kjellstrand.webshooter.di.ApplicationScope")
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

    @Test
    fun `ShooterApplication has HiltAndroidApp annotation`() {
        val annotation = ShooterApplication::class.java.annotations.find {
            it.annotationClass.simpleName == "HiltAndroidApp"
        }
        assertNotNull("ShooterApplication should have @HiltAndroidApp annotation", annotation)
    }
}
