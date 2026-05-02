package se.kjellstrand.webshooter.data.results

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import se.kjellstrand.webshooter.ui.navigation.Screen

@RunWith(AndroidJUnit4::class)
class CompetitionIdTypeConsistencyTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun ResultsRepository_get_method_competitionId_parameter_is_Long_type() {
        val method = ResultsRepository::class.java.methods.find {
            it.name == "get" && it.parameterCount == 2
        }!!
        assertEquals(
            "competitionId parameter should be Long (long)",
            Long::class.javaPrimitiveType,
            method.parameterTypes[0]
        )
    }

    @Test
    fun ResultsRepository_getShooterResults_competitionId_parameter_is_Long_type() {
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
    fun CompetitionResults_createRoute_competitionId_parameter_is_Long_type() {
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
    fun ShooterResult_createRoute_competitionId_parameter_is_Long_type() {
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
    fun CompetitionResults_createRoute_produces_correct_route_with_normal_id() {
        val route = Screen.CompetitionResults.createRoute(
            42, "FIELD", "Test", "2026-01-01"
        )
        assertTrue(route.contains("42"))
        assertTrue(route.contains("FIELD"))
    }

    @Test
    fun ShooterResult_createRoute_produces_correct_route_with_normal_ids() {
        val route = Screen.ShooterResult.createRoute(
            99, 55, "PRECISION"
        )
        assertTrue(route.contains("99"))
        assertTrue(route.contains("55"))
        assertTrue(route.contains("PRECISION"))
    }

    @Test
    fun CompetitionSignup_createRoute_accepts_Long() {
        val largeId: Long = Int.MAX_VALUE.toLong() + 1L
        val route = Screen.CompetitionSignup.createRoute(largeId)
        assertTrue(route.contains(largeId.toString()))
    }

    @Test
    fun CompetitionTeams_createRoute_accepts_Long() {
        val largeId: Long = Int.MAX_VALUE.toLong() + 1L
        val route = Screen.CompetitionTeams.createRoute(largeId)
        assertTrue(route.contains(largeId.toString()))
    }
}
