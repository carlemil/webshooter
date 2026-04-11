package se.kjellstrand.webshooter.data.competitions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.flow.Flow

class CompetitionsRepositoryPrefetchTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `repository has prefetchCompleted method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "prefetchCompleted" }
        assertNotNull("CompetitionsRepository should have a prefetchCompleted method", method)
    }

    @Test
    fun `prefetchCompleted returns Flow`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "prefetchCompleted" }
        assertNotNull("prefetchCompleted should exist", method)
        assertTrue(
            "prefetchCompleted should return a Flow",
            Flow::class.java.isAssignableFrom(method!!.returnType)
        )
    }

    @Test
    fun `prefetchCompleted accepts pageSize parameter`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "prefetchCompleted" }
        assertNotNull("prefetchCompleted should exist", method)
        assertTrue(
            "prefetchCompleted should accept an int parameter for pageSize",
            method!!.parameterTypes.any { it == Int::class.java }
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `repository still has get method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "get" }
        assertNotNull("CompetitionsRepository should still have the get method", method)
    }

    @Test
    fun `repository still has getLocalAll method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "getLocalAll" }
        assertNotNull("CompetitionsRepository should still have getLocalAll", method)
    }

    @Test
    fun `repository still has getLocalCompleted method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "getLocalCompleted" }
        assertNotNull("CompetitionsRepository should still have getLocalCompleted", method)
    }
}
