package se.kjellstrand.webshooter.di

import org.junit.Test
import org.junit.Assert.*

class ClubStatsModuleTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ClubStatsModule class exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.di.ClubStatsModule")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ClubStatsModule should exist in di package", clazz)
    }

    @Test
    fun `ClubStatsModule has Hilt Module annotation`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.di.ClubStatsModule")
        val hasModule = clazz.annotations.any {
            it.annotationClass.qualifiedName == "dagger.Module"
        }
        assertTrue("ClubStatsModule should be annotated with @Module", hasModule)
    }

    @Test
    fun `ClubStatsModule has providesClubStatsRepository method`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.di.ClubStatsModule")
        val method = clazz.declaredMethods.find { it.name == "providesClubStatsRepository" }
        assertNotNull("ClubStatsModule should have a providesClubStatsRepository method", method)
    }

    @Test
    fun `providesClubStatsRepository returns ClubStatsRepository`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.di.ClubStatsModule")
        val method = clazz.declaredMethods.find { it.name == "providesClubStatsRepository" }
        assertNotNull(method)
        assertEquals(
            "se.kjellstrand.webshooter.data.clubstats.ClubStatsRepository",
            method!!.returnType.name
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartsModule class still exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.di.ChartsModule")
        assertNotNull(clazz)
    }

    @Test
    fun `ClubStatsRepository class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.clubstats.ClubStatsRepository")
        assertNotNull(clazz)
    }
}
