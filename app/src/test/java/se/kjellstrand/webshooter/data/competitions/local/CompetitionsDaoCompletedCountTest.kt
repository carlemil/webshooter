package se.kjellstrand.webshooter.data.competitions.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompetitionsDaoCompletedCountTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `DAO has getCompletedCount method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getCompletedCount" }
        assertNotNull("CompetitionsDao should have a getCompletedCount method", method)
    }

    @Test
    fun `getCompletedCount returns Int`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getCompletedCount" }
        assertNotNull("CompetitionsDao should have a getCompletedCount method", method)
        assertEquals(
            "getCompletedCount should return Object (boxed Int from suspend fun)",
            Object::class.java,
            method!!.returnType
        )
    }

    @Test
    fun `getCompletedCount takes only continuation parameter`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getCompletedCount" }
        assertNotNull("CompetitionsDao should have a getCompletedCount method", method)
        val params = method!!.parameterTypes
        assertEquals(
            "getCompletedCount should take only the Continuation parameter (suspend fun)",
            1,
            params.size
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `DAO has getCompletedCompetitions method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getCompletedCompetitions" }
        assertNotNull("CompetitionsDao should have a getCompletedCompetitions method", method)
    }

    @Test
    fun `DAO has getAll method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getAll" }
        assertNotNull("CompetitionsDao should have a getAll method", method)
    }

    @Test
    fun `DAO has insertAll method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "insertAll" }
        assertNotNull("CompetitionsDao should have a insertAll method", method)
    }
}
