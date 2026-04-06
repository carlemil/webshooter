package se.kjellstrand.webshooter.ui.screens.charts

import org.junit.Test
import org.junit.Assert.*

class ChartsViewModelTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ChartsUiState class exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsUiState")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ChartsUiState should exist", clazz)
    }

    @Test
    fun `ChartsUiState has required fields`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsUiState")
        val fieldNames = clazz.declaredFields.map { it.name }
        assertTrue("Should have chartData", fieldNames.contains("chartData"))
        assertTrue("Should have selectedWeaponClasses", fieldNames.contains("selectedWeaponClasses"))
        assertTrue("Should have isLoading", fieldNames.contains("isLoading"))
        assertTrue("Should have hasError", fieldNames.contains("hasError"))
        assertTrue("Should have showSearchDialog", fieldNames.contains("showSearchDialog"))
        assertTrue("Should have searchQuery", fieldNames.contains("searchQuery"))
    }

    @Test
    fun `ChartsViewModel interface exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsViewModel")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ChartsViewModel interface should exist", clazz)
        assertTrue("ChartsViewModel should be an interface", clazz!!.isInterface)
    }

    @Test
    fun `ChartsViewModel has required methods`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsViewModel")
        val methodNames = clazz.declaredMethods.map { it.name }
        assertTrue("Should have selectTab", methodNames.contains("selectTab"))
        assertTrue("Should have toggleWeaponClass", methodNames.contains("toggleWeaponClass"))
        assertTrue("Should have addShooter", methodNames.contains("addShooter"))
        assertTrue("Should have removeShooter", methodNames.contains("removeShooter"))
        assertTrue("Should have setSearchQuery", methodNames.contains("setSearchQuery"))
        assertTrue("Should have setShowSearchDialog", methodNames.contains("setShowSearchDialog"))
    }

    @Test
    fun `ChartsViewModelImpl class exists and implements ChartsViewModel`() {
        val implClass = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsViewModelImpl")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ChartsViewModelImpl should exist", implClass)

        val vmInterface = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsViewModel")
        assertTrue(
            "ChartsViewModelImpl should implement ChartsViewModel",
            vmInterface.isAssignableFrom(implClass!!)
        )
    }

    @Test
    fun `ShooterChartInfo class exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ShooterChartInfo")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ShooterChartInfo should exist", clazz)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartsRepository class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.charts.ChartsRepository")
        assertNotNull(clazz)
    }

    @Test
    fun `ChartDataPoint class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.charts.ChartDataPoint")
        assertNotNull(clazz)
    }

    @Test
    fun `ResultsType enum exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.competitions.remote.ResultsType")
        assertTrue(clazz.isEnum)
    }

    @Test
    fun `ClubMember class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.club.remote.ClubMember")
        assertNotNull(clazz)
    }
}
