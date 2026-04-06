package se.kjellstrand.webshooter.data.charts

import org.junit.Test
import org.junit.Assert.*

class ChartsRepositoryTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ChartDataPoint class exists and holds correct data`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.charts.ChartDataPoint")
        assertNotNull(clazz)

        // Verify it has the expected fields via constructor parameters
        val constructor = clazz.constructors.first()
        val paramNames = constructor.parameters.map { it.name }
        assertTrue("Should have competitionId field", clazz.declaredFields.any { it.name == "competitionId" })
        assertTrue("Should have date field", clazz.declaredFields.any { it.name == "date" })
        assertTrue("Should have averageSerieScore field", clazz.declaredFields.any { it.name == "averageSerieScore" })
        assertTrue("Should have weaponClass field", clazz.declaredFields.any { it.name == "weaponClass" })
        assertTrue("Should have resultsType field", clazz.declaredFields.any { it.name == "resultsType" })
    }

    @Test
    fun `ChartData class exists and contains dataPoints list`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.charts.ChartData")
        assertNotNull(clazz)
        assertTrue("Should have dataPoints field", clazz.declaredFields.any { it.name == "dataPoints" })
    }

    @Test
    fun `ChartsRepository class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.charts.ChartsRepository")
        assertNotNull(clazz)
    }

    @Test
    fun `computeAverageStationScore computes correct average from station results`() {
        // Test the static/companion computation helper
        val repoClass = Class.forName("se.kjellstrand.webshooter.data.charts.ChartsRepository")
        val companionField = repoClass.getDeclaredField("Companion")
        companionField.isAccessible = true
        val companion = companionField.get(null)

        val method = companion.javaClass.getDeclaredMethod(
            "computeAverageStationScore",
            List::class.java
        )
        method.isAccessible = true

        // Simulate station points: [10, 20, 30] → average = 20.0
        val stationPoints = listOf(10L, 20L, 30L)
        val result = method.invoke(companion, stationPoints) as Double
        assertEquals(20.0, result, 0.001)
    }

    @Test
    fun `computeAverageStationScore returns 0 for empty list`() {
        val repoClass = Class.forName("se.kjellstrand.webshooter.data.charts.ChartsRepository")
        val companionField = repoClass.getDeclaredField("Companion")
        companionField.isAccessible = true
        val companion = companionField.get(null)

        val method = companion.javaClass.getDeclaredMethod(
            "computeAverageStationScore",
            List::class.java
        )
        method.isAccessible = true

        val result = method.invoke(companion, emptyList<Long>()) as Double
        assertEquals(0.0, result, 0.001)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ResultsResponse class exists in results package`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.results.remote.ResultsResponse")
        assertNotNull(clazz)
    }

    @Test
    fun `SignupsRepository class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.mysignups.SignupsRepository")
        assertNotNull(clazz)
    }

    @Test
    fun `ResultsRepository class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.results.ResultsRepository")
        assertNotNull(clazz)
    }

    @Test
    fun `Resource sealed interface exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.data.common.Resource")
        assertNotNull(clazz)
    }
}
