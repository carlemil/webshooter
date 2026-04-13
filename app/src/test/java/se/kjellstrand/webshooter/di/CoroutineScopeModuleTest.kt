package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.inject.Qualifier
import javax.inject.Singleton

class CoroutineScopeModuleTest {

    private fun loadClass(fqName: String): Class<*>? = try {
        Class.forName(fqName)
    } catch (e: ClassNotFoundException) {
        null
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ApplicationScope qualifier class exists`() {
        val cls = loadClass("se.kjellstrand.webshooter.di.ApplicationScope")
        assertNotNull("ApplicationScope qualifier should exist in the di package", cls)
    }

    @Test
    fun `ApplicationScope is annotated with Qualifier`() {
        val cls = loadClass("se.kjellstrand.webshooter.di.ApplicationScope")
        assertNotNull("ApplicationScope should exist", cls)
        assertNotNull(
            "ApplicationScope should be annotated with @Qualifier",
            cls!!.getAnnotation(Qualifier::class.java)
        )
    }

    @Test
    fun `ApplicationScope is an annotation class`() {
        val cls = loadClass("se.kjellstrand.webshooter.di.ApplicationScope")
        assertNotNull("ApplicationScope should exist", cls)
        assertTrue(
            "ApplicationScope should itself be an annotation type",
            cls!!.isAnnotation
        )
    }

    @Test
    fun `CoroutineScopeModule class exists`() {
        val cls = loadClass("se.kjellstrand.webshooter.di.CoroutineScopeModule")
        assertNotNull("CoroutineScopeModule should exist in the di package", cls)
    }

    @Test
    fun `CoroutineScopeModule is annotated with Module`() {
        val cls = loadClass("se.kjellstrand.webshooter.di.CoroutineScopeModule")
        assertNotNull("CoroutineScopeModule should exist", cls)
        assertNotNull(
            "CoroutineScopeModule should be annotated with @Module",
            cls!!.getAnnotation(Module::class.java)
        )
    }

    // Note: we do not reflectively verify @InstallIn. It has CLASS retention
    // (not RUNTIME) in Hilt, so Java reflection returns null even when it is
    // present. Hilt's KSP annotation processor fails compilation if @InstallIn
    // is missing or references a non-existent component, so a successful
    // kspProdReleaseKotlin task already proves @InstallIn is wired correctly.

    @Test
    fun `CoroutineScopeModule has a Provides method returning CoroutineScope`() {
        val cls = loadClass("se.kjellstrand.webshooter.di.CoroutineScopeModule")
        assertNotNull("CoroutineScopeModule should exist", cls)
        val provides = cls!!.declaredMethods.firstOrNull { m ->
            m.isAnnotationPresent(Provides::class.java) &&
                CoroutineScope::class.java.isAssignableFrom(m.returnType)
        }
        assertNotNull(
            "CoroutineScopeModule should have a @Provides method returning CoroutineScope",
            provides
        )
    }

    @Test
    fun `Provides method is annotated with Singleton`() {
        val cls = loadClass("se.kjellstrand.webshooter.di.CoroutineScopeModule")
        assertNotNull("CoroutineScopeModule should exist", cls)

        val provides = cls!!.declaredMethods.firstOrNull { m ->
            m.isAnnotationPresent(Provides::class.java) &&
                CoroutineScope::class.java.isAssignableFrom(m.returnType)
        }
        assertNotNull("Provides method should exist", provides)
        assertTrue(
            "Provides method should be annotated with @Singleton",
            provides!!.isAnnotationPresent(Singleton::class.java)
        )
        // Note: @ApplicationScope has BINARY retention (standard for Hilt qualifiers),
        // so it isn't visible to runtime reflection. Hilt's KSP processor enforces that
        // the qualifier is wired up at compile time — if it's missing, Hilt will fail
        // compilation of any consumer that tries to inject via @ApplicationScope.
    }

    @Test
    fun `provided CoroutineScope can be constructed and is active`() {
        val cls = loadClass("se.kjellstrand.webshooter.di.CoroutineScopeModule")
        assertNotNull("CoroutineScopeModule should exist", cls)
        val provides = cls!!.declaredMethods.firstOrNull { m ->
            m.isAnnotationPresent(Provides::class.java) &&
                CoroutineScope::class.java.isAssignableFrom(m.returnType)
        }
        assertNotNull("Provides method should exist", provides)

        // Obtain an instance of the module (class or object) and invoke the provider
        val instance = try {
            cls.getDeclaredField("INSTANCE").get(null)  // object declaration
        } catch (e: NoSuchFieldException) {
            cls.getDeclaredConstructor().apply { isAccessible = true }.newInstance()  // class declaration
        }
        val scope = provides!!.invoke(instance) as CoroutineScope
        assertNotNull("Provider should return a non-null CoroutineScope", scope)
        // A freshly constructed scope should be active.
        assertTrue(
            "Provided scope should be active right after creation",
            scope.coroutineContext[kotlinx.coroutines.Job]?.isActive == true
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `Qualifier annotation class is available on classpath`() {
        assertNotNull(Qualifier::class.java)
    }

    @Test
    fun `Singleton annotation class is available on classpath`() {
        assertNotNull(Singleton::class.java)
    }

    @Test
    fun `SingletonComponent class is available on classpath`() {
        // Sanity check that the Hilt component we reference actually exists.
        assertEquals("SingletonComponent", SingletonComponent::class.java.simpleName)
    }
}
