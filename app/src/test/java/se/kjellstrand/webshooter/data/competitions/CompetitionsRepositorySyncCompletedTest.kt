package se.kjellstrand.webshooter.data.competitions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompetitionsRepositorySyncCompletedTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `repository has syncCompleted method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncCompleted" }
        assertNotNull("CompetitionsRepository should have a syncCompleted method", method)
    }

    @Test
    fun `syncCompleted is a suspend fun`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncCompleted" }
        assertNotNull("syncCompleted should exist", method)
        // Suspend functions are compiled with a Continuation parameter
        assertTrue(
            "syncCompleted should be a suspend fun (has Continuation parameter)",
            method!!.parameterTypes.any { it.name.contains("Continuation") }
        )
    }

    @Test
    fun `prefetchCompleted method is removed`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "prefetchCompleted" }
        assertNull("prefetchCompleted should be removed", method)
    }

    @Test
    fun `getLocalCompleted method is removed`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "getLocalCompleted" }
        assertNull("getLocalCompleted should be removed", method)
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
}
