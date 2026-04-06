package se.kjellstrand.webshooter.ui.screens.charts

import org.junit.Test
import org.junit.Assert.*
import java.lang.reflect.Modifier

class ChartsScreenTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ChartsScreen composable function exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsScreenKt")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ChartsScreenKt should exist", clazz)
        val methods = clazz!!.declaredMethods.map { it.name }
        assertTrue("Should have ChartsScreen composable", methods.any { it == "ChartsScreen" })
    }

    @Test
    fun `ChartLineChart composable function exists for chart rendering`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsScreenKt")
        val methods = clazz.declaredMethods.map { it.name }
        assertTrue("Should have ChartLineChart composable", methods.any { it == "ChartLineChart" })
    }

    @Test
    fun `AddShooterDialog composable function exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsScreenKt")
        val methods = clazz.declaredMethods.map { it.name }
        assertTrue("Should have AddShooterDialog composable", methods.any { it == "AddShooterDialog" })
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartsViewModel interface exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsViewModel")
        assertTrue(clazz.isInterface)
    }

    @Test
    fun `ChartsUiState class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsUiState")
        assertNotNull(clazz)
    }

    @Test
    fun `WeaponClassBadge composable exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.common.WeaponClassBadgesKt")
        assertNotNull(clazz)
    }

    @Test
    fun `MPAndroidChart LineChart class is available`() {
        val clazz = Class.forName("com.github.mikephil.charting.charts.LineChart")
        assertNotNull(clazz)
    }
}
