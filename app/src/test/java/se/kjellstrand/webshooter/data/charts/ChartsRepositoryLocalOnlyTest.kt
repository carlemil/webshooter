package se.kjellstrand.webshooter.data.charts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource

class ChartsRepositoryLocalOnlyTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `syncNewCompletedCompetitions method is removed`() {
        val method = ChartsRepository::class.java.declaredMethods.find {
            it.name == "syncNewCompletedCompetitions"
        }
        assertNull(
            "syncNewCompletedCompetitions should be removed - sync is done at app startup",
            method
        )
    }

    @Test
    fun `ChartsRepository constructor does not accept CompetitionsRemoteDataSource`() {
        val constructors = ChartsRepository::class.java.declaredConstructors
        val hasRemoteDataSource = constructors.any { c ->
            c.parameterTypes.any { it == CompetitionsRemoteDataSource::class.java }
        }
        assertFalse(
            "ChartsRepository should not depend on CompetitionsRemoteDataSource anymore",
            hasRemoteDataSource
        )
    }

    @Test
    fun `ChartsRepository has no competitionsRemoteDataSource field`() {
        val field = ChartsRepository::class.java.declaredFields.find {
            it.name == "competitionsRemoteDataSource"
        }
        assertNull(
            "ChartsRepository should not have a competitionsRemoteDataSource field",
            field
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartsRepository still has getChartData method`() {
        val method = ChartsRepository::class.java.methods.find { it.name == "getChartData" }
        assertNotNull("ChartsRepository should still have getChartData", method)
    }

    @Test
    fun `ChartsRepository still has getShooterChartData method`() {
        val method = ChartsRepository::class.java.methods.find { it.name == "getShooterChartData" }
        assertNotNull("ChartsRepository should still have getShooterChartData", method)
    }

    @Test
    fun `ChartsRepository constructor accepts CompetitionsDao`() {
        val constructors = ChartsRepository::class.java.declaredConstructors
        val hasDao = constructors.any { c ->
            c.parameterTypes.any { it.name.endsWith("CompetitionsDao") }
        }
        assertTrue("ChartsRepository should still accept CompetitionsDao", hasDao)
    }
}
