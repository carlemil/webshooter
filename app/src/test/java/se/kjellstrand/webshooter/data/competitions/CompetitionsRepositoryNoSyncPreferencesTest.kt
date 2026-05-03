package se.kjellstrand.webshooter.data.competitions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CompetitionsRepositoryNoSyncPreferencesTest {

    private fun syncPreferencesClass(): Class<*>? = try {
        Class.forName("se.kjellstrand.webshooter.data.competitions.local.SyncPreferences")
    } catch (e: ClassNotFoundException) {
        null
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `SyncPreferences class is removed from the project`() {
        assertNull(
            "SyncPreferences should no longer exist — it's dead weight after the WEEK_MS gate removal",
            syncPreferencesClass()
        )
    }

    @Test
    fun `CompetitionsRepository constructor does not depend on SyncPreferences`() {
        val constructors = CompetitionsRepository::class.java.constructors
        val takesSyncPrefs = constructors.any { c ->
            c.parameterTypes.any { it.name.endsWith("SyncPreferences") }
        }
        assertFalse(
            "CompetitionsRepository must not have a SyncPreferences ctor parameter",
            takesSyncPrefs
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `CompetitionsRepository class still exists`() {
        val cls = try {
            Class.forName("se.kjellstrand.webshooter.data.competitions.CompetitionsRepository")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("CompetitionsRepository should still exist", cls)
    }

    @Test
    fun `CompetitionsRepository still has syncAll method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncAll" }
        assertNotNull("CompetitionsRepository should still have syncAll", method)
    }

    @Test
    fun `CompetitionsRepository still has observeAll method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "observeAll" }
        assertNotNull("CompetitionsRepository should still have observeAll", method)
    }
}
