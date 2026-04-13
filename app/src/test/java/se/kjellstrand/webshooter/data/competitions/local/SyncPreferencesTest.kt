package se.kjellstrand.webshooter.data.competitions.local

import dagger.hilt.android.qualifiers.ApplicationContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

class SyncPreferencesTest {

    private val className = "se.kjellstrand.webshooter.data.competitions.local.SyncPreferences"

    private fun loadClass(): Class<*>? = try {
        Class.forName(className)
    } catch (e: ClassNotFoundException) {
        null
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `SyncPreferences class exists in local package`() {
        assertNotNull("SyncPreferences should exist at $className", loadClass())
    }

    @Test
    fun `SyncPreferences is annotated with Singleton`() {
        val cls = loadClass()
        assertNotNull("SyncPreferences should exist", cls)
        assertNotNull(
            "SyncPreferences should be annotated with @Singleton",
            cls!!.getAnnotation(Singleton::class.java)
        )
    }

    @Test
    fun `SyncPreferences has Inject constructor taking a Context`() {
        val cls = loadClass()
        assertNotNull("SyncPreferences should exist", cls)
        val constructor = cls!!.declaredConstructors.firstOrNull {
            it.isAnnotationPresent(Inject::class.java)
        }
        assertNotNull("SyncPreferences should have an @Inject constructor", constructor)
        assertEquals(
            "Constructor should take exactly one parameter (Context)",
            1,
            constructor!!.parameterTypes.size
        )
        assertEquals(
            "Constructor parameter should be android.content.Context",
            "android.content.Context",
            constructor.parameterTypes[0].name
        )
        // Note: @ApplicationContext (Hilt qualifier) has BINARY retention,
        // so it isn't visible to runtime reflection. Hilt DI itself enforces
        // it at compile time — we don't re-verify it here.
    }

    @Test
    fun `SyncPreferences has getLastCompletedFullSyncMs returning long`() {
        val cls = loadClass()
        assertNotNull("SyncPreferences should exist", cls)
        val method = cls!!.declaredMethods.firstOrNull { it.name == "getLastCompletedFullSyncMs" }
        assertNotNull("SyncPreferences should have getLastCompletedFullSyncMs", method)
        assertEquals(
            "getLastCompletedFullSyncMs should return primitive long",
            java.lang.Long.TYPE,
            method!!.returnType
        )
        assertEquals(
            "getLastCompletedFullSyncMs should take no parameters",
            0,
            method.parameterTypes.size
        )
    }

    @Test
    fun `SyncPreferences has setLastCompletedFullSyncMs taking long`() {
        val cls = loadClass()
        assertNotNull("SyncPreferences should exist", cls)
        val method = cls!!.declaredMethods.firstOrNull { it.name == "setLastCompletedFullSyncMs" }
        assertNotNull("SyncPreferences should have setLastCompletedFullSyncMs", method)
        assertEquals(
            "setLastCompletedFullSyncMs should take exactly one parameter",
            1,
            method!!.parameterTypes.size
        )
        assertEquals(
            "setLastCompletedFullSyncMs parameter should be primitive long",
            java.lang.Long.TYPE,
            method.parameterTypes[0]
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `Singleton annotation is available on classpath`() {
        // Sanity check: confirm the annotation we're testing for actually exists.
        assertNotNull(Singleton::class.java)
    }

    @Test
    fun `ApplicationContext qualifier is available on classpath`() {
        // Sanity check: confirm the Hilt qualifier we're testing for actually exists.
        assertNotNull(ApplicationContext::class.java)
    }

    @Test
    fun `Inject annotation is available on classpath`() {
        // Sanity check.
        assertNotNull(Inject::class.java)
    }
}
