package se.kjellstrand.webshooter

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository

class ShooterApplicationStartupSyncTest {

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
