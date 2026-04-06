package se.kjellstrand.webshooter.di

import org.junit.Test
import org.junit.Assert.*

class ChartsModuleTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ChartsModule class exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.di.ChartsModule")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ChartsModule should exist in di package", clazz)
    }

    @Test
    fun `ChartsModule has Hilt Module annotation`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.di.ChartsModule")
        val hasModule = clazz.annotations.any {
            it.annotationClass.qualifiedName == "dagger.Module"
        }
        assertTrue("ChartsModule should be annotated with @Module", hasModule)
    }

    @Test
    fun `ChartsModule has providesChartsRepository method`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.di.ChartsModule")
        val method = clazz.declaredMethods.find { it.name == "providesChartsRepository" }
        assertNotNull("ChartsModule should have a providesChartsRepository method", method)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ResultsModule class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.di.ResultsModule")
        assertNotNull(clazz)
    }

    @Test
    fun `SignupsModule class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.di.SignupsModule")
        assertNotNull(clazz)
    }

    @Test
    fun `ChartsRepository class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.charts.ChartsRepository")
        assertNotNull(clazz)
    }
}
