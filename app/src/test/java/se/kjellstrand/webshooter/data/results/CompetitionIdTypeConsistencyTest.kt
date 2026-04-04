package se.kjellstrand.webshooter.data.results

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.kjellstrand.webshooter.ui.navigation.Screen

@RunWith(RobolectricTestRunner::class)
class CompetitionIdTypeConsistencyTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ResultsRepository get method competitionId parameter is Long type`() {
        val method = ResultsRepository::class.java.methods.find {
            it.name == "get" && it.parameterCount == 1
        }!!
        assertEquals(
            "competitionId parameter should be Long (long)",
            Long::class.javaPrimitiveType,
            method.parameterTypes[0]
        )
    }

    @Test
    fun `ResultsRepository getPreferCached method competitionId parameter is Long type`() {
        val method = ResultsRepository::class.java.methods.find {
            it.name == "getPreferCached" && it.parameterCount == 1
        }!!
        assertEquals(
            "competitionId parameter should be Long (long)",
            Long::class.javaPrimitiveType,
            method.parameterTypes[0]
        )
    }

    @Test
    fun `ResultsRepository getShooterResults competitionId parameter is Long type`() {
        val method = ResultsRepository::class.java.methods.find {
            it.name == "getShooterResults" && it.parameterCount == 2
        }!!
        assertEquals(
            "First parameter (competitionId) should be Long (long)",
            Long::class.javaPrimitiveType,
            method.parameterTypes[0]
        )
    }

    @Test
    fun `CompetitionResults createRoute competitionId parameter is Long type`() {
        val method = Screen.CompetitionResults::class.java.methods.find {
            it.name == "createRoute"
        }!!
        assertEquals(
            "competitionId parameter should be Long (long)",
            Long::class.javaPrimitiveType,
            method.parameterTypes[0]
        )
    }

    @Test
    fun `ShooterResult createRoute competitionId parameter is Long type`() {
        val method = Screen.ShooterResult::class.java.methods.find {
            it.name == "createRoute"
        }!!
        assertEquals(
            "competitionId parameter should be Long (long)",
            Long::class.javaPrimitiveType,
            method.parameterTypes[0]
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `CompetitionResults createRoute produces correct route with normal id`() {
        val route = Screen.CompetitionResults.createRoute(
            42, "FIELD", "Test", "2026-01-01"
        )
        assertTrue(route.contains("42"))
        assertTrue(route.contains("FIELD"))
    }

    @Test
    fun `ShooterResult createRoute produces correct route with normal ids`() {
        val route = Screen.ShooterResult.createRoute(
            99, 55, "PRECISION"
        )
        assertTrue(route.contains("99"))
        assertTrue(route.contains("55"))
        assertTrue(route.contains("PRECISION"))
    }

    @Test
    fun `CompetitionSignup createRoute accepts Long`() {
        val largeId: Long = Int.MAX_VALUE.toLong() + 1L
        val route = Screen.CompetitionSignup.createRoute(largeId)
        assertTrue(route.contains(largeId.toString()))
    }

    @Test
    fun `CompetitionTeams createRoute accepts Long`() {
        val largeId: Long = Int.MAX_VALUE.toLong() + 1L
        val route = Screen.CompetitionTeams.createRoute(largeId)
        assertTrue(route.contains(largeId.toString()))
    }
}
