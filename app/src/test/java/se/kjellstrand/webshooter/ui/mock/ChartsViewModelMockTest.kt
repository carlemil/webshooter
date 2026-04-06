package se.kjellstrand.webshooter.ui.mock

import org.junit.Test
import org.junit.Assert.*

class ChartsViewModelMockTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ChartsViewModelMock class exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.mock.ChartsViewModelMock")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ChartsViewModelMock should exist in ui.mock package", clazz)
    }

    @Test
    fun `ChartsViewModelMock implements ChartsViewModel`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.mock.ChartsViewModelMock")
        val vmInterface = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsViewModel")
        assertTrue(
            "ChartsViewModelMock should implement ChartsViewModel",
            vmInterface.isAssignableFrom(clazz)
        )
    }

    @Test
    fun `MockCharts data class exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.mock.MockCharts")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("MockCharts should exist for mock chart data", clazz)
    }

    @Test
    fun `ChartsScreen has preview composable functions`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsScreenKt")
        val methods = clazz.declaredMethods.map { it.name }
        assertTrue("Should have a preview function", methods.any { it.contains("Preview") })
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ClubViewModelMock class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.mock.ClubViewModelMock")
        assertNotNull(clazz)
    }

    @Test
    fun `ChartsViewModel interface exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsViewModel")
        assertTrue(clazz.isInterface)
    }

    @Test
    fun `ChartsScreen composable exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsScreenKt")
        val methods = clazz.declaredMethods.map { it.name }
        assertTrue(methods.any { it == "ChartsScreen" })
    }
}
