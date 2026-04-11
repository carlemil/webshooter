package se.kjellstrand.webshooter.data.competitions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.flow.Flow

class CompetitionsRepositoryLocalReadTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `repository has getLocalAll method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "getLocalAll" }
        assertNotNull("CompetitionsRepository should have a getLocalAll method", method)
    }

    @Test
    fun `getLocalAll returns Flow`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "getLocalAll" }
        assertNotNull("getLocalAll should exist", method)
        assertTrue(
            "getLocalAll should return a Flow",
            Flow::class.java.isAssignableFrom(method!!.returnType)
        )
    }

    @Test
    fun `getLocalAll takes no parameters`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "getLocalAll" }
        assertNotNull("getLocalAll should exist", method)
        assertEquals("getLocalAll should take no parameters", 0, method!!.parameterTypes.size)
    }

    @Test
    fun `repository has getLocalCompleted method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "getLocalCompleted" }
        assertNotNull("CompetitionsRepository should have a getLocalCompleted method", method)
    }

    @Test
    fun `getLocalCompleted returns Flow`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "getLocalCompleted" }
        assertNotNull("getLocalCompleted should exist", method)
        assertTrue(
            "getLocalCompleted should return a Flow",
            Flow::class.java.isAssignableFrom(method!!.returnType)
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `repository has existing get method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "get" }
        assertNotNull("CompetitionsRepository should have a get method", method)
    }

    @Test
    fun `get method takes page and pageSize parameters`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "get" }
        assertNotNull(method)
        assertEquals("get should take 2 parameters", 2, method!!.parameterTypes.size)
    }

    @Test
    fun `DAO has getCompletedCompetitions method`() {
        val method = se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao::class.java
            .methods.find { it.name == "getCompletedCompetitions" }
        assertNotNull("CompetitionsDao should have getCompletedCompetitions", method)
    }
}
