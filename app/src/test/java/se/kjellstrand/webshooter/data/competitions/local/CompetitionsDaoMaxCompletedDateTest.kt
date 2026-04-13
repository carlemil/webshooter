package se.kjellstrand.webshooter.data.competitions.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CompetitionsDaoMaxCompletedDateTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `DAO has getMaxCompletedDate method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getMaxCompletedDate" }
        assertNotNull("CompetitionsDao should have a getMaxCompletedDate method", method)
    }

    @Test
    fun `getMaxCompletedDate returns Object (nullable String from suspend fun)`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getMaxCompletedDate" }
        assertNotNull("CompetitionsDao should have a getMaxCompletedDate method", method)
        assertEquals(
            "getMaxCompletedDate should return Object (boxed nullable String from suspend fun)",
            Object::class.java,
            method!!.returnType
        )
    }

    @Test
    fun `getMaxCompletedDate takes only continuation parameter`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getMaxCompletedDate" }
        assertNotNull("CompetitionsDao should have a getMaxCompletedDate method", method)
        val params = method!!.parameterTypes
        assertEquals(
            "getMaxCompletedDate should take only the Continuation parameter (suspend fun)",
            1,
            params.size
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `DAO has getCompletedCount method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getCompletedCount" }
        assertNotNull("CompetitionsDao should have a getCompletedCount method", method)
    }

    @Test
    fun `DAO has getCompletedCompetitions method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getCompletedCompetitions" }
        assertNotNull("CompetitionsDao should have a getCompletedCompetitions method", method)
    }

    @Test
    fun `DAO has insertAll method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "insertAll" }
        assertNotNull("CompetitionsDao should have an insertAll method", method)
    }
}
