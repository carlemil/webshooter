package se.kjellstrand.webshooter.ui.screens.charts

import org.junit.Test
import org.junit.Assert.*

class ChartsViewModelTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ChartsUiState class exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ChartsUiState")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ChartsUiState should exist", clazz)
    }

    @Test
    fun `ChartsUiState has required fields`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ChartsUiState")
        val fieldNames = clazz.declaredFields.map { it.name }
        assertTrue("Should have chartData", fieldNames.contains("chartData"))
        assertTrue("Should have selectedGroup", fieldNames.contains("selectedGroup"))
        assertTrue("Should have availableGroups", fieldNames.contains("availableGroups"))
        assertTrue("Should have isLoading", fieldNames.contains("isLoading"))
        assertTrue("Should have hasError", fieldNames.contains("hasError"))
        assertTrue("Should have showSearchDialog", fieldNames.contains("showSearchDialog"))
        assertTrue("Should have searchQuery", fieldNames.contains("searchQuery"))
    }

    @Test
    fun `ResultsTrendsViewModel interface exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ResultsTrendsViewModel")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ResultsTrendsViewModel interface should exist", clazz)
        assertTrue("ResultsTrendsViewModel should be an interface", clazz!!.isInterface)
    }

    @Test
    fun `ResultsTrendsViewModel has required methods`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ResultsTrendsViewModel")
        val methodNames = clazz.declaredMethods.map { it.name }
        assertTrue("Should have selectTab", methodNames.contains("selectTab"))
        assertTrue("Should have selectWeaponGroup", methodNames.contains("selectWeaponGroup"))
        assertTrue("Should have addShooter", methodNames.contains("addShooter"))
        assertTrue("Should have removeShooter", methodNames.contains("removeShooter"))
        assertTrue("Should have setSearchQuery", methodNames.contains("setSearchQuery"))
        assertTrue("Should have setShowSearchDialog", methodNames.contains("setShowSearchDialog"))
    }

    @Test
    fun `ResultsTrendsViewModelImpl class exists and implements ResultsTrendsViewModel`() {
        val implClass = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ResultsTrendsViewModelImpl")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ResultsTrendsViewModelImpl should exist", implClass)

        val vmInterface = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ResultsTrendsViewModel")
        assertTrue(
            "ResultsTrendsViewModelImpl should implement ResultsTrendsViewModel",
            vmInterface.isAssignableFrom(implClass!!)
        )
    }

    @Test
    fun `ShooterChartInfo class exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ShooterChartInfo")
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
