package se.kjellstrand.webshooter.data.competitions.local

import kotlinx.coroutines.flow.Flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompetitionsDaoObserveAndDiffTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `DAO has observeAll method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "observeAll" }
        assertNotNull("CompetitionsDao should have an observeAll method", method)
    }

    @Test
    fun `observeAll returns Flow and is not a suspend fun`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "observeAll" }
        assertNotNull("observeAll should exist", method)
        assertFalse(
            "observeAll should NOT be a suspend fun (Flow methods return reactively)",
            method!!.parameterTypes.any { it.name.contains("Continuation") }
        )
        assertTrue(
            "observeAll should return Flow, was: ${method.returnType.name}",
            Flow::class.java.isAssignableFrom(method.returnType)
        )
    }

    @Test
    fun `DAO has getNonCompletedIds method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getNonCompletedIds" }
        assertNotNull("CompetitionsDao should have a getNonCompletedIds method", method)
    }

    @Test
    fun `getNonCompletedIds is a suspend fun`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getNonCompletedIds" }
        assertNotNull("getNonCompletedIds should exist", method)
        assertTrue(
            "getNonCompletedIds should be a suspend fun (has Continuation parameter)",
            method!!.parameterTypes.any { it.name.contains("Continuation") }
        )
    }

    @Test
    fun `DAO has deleteById method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "deleteById" }
        assertNotNull("CompetitionsDao should have a deleteById method", method)
    }

    @Test
    fun `deleteById is a suspend fun taking a Long id`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "deleteById" }
        assertNotNull("deleteById should exist", method)
        assertTrue(
            "deleteById should be a suspend fun (has Continuation parameter)",
            method!!.parameterTypes.any { it.name.contains("Continuation") }
        )
        // suspend fun deleteById(id: Long) → JVM signature: deleteById(long, Continuation)
        // Excluding the Continuation, exactly 1 parameter of type long
        val nonContinuationTypes = method.parameterTypes
            .filter { !it.name.contains("Continuation") }
        assertEquals(
            "deleteById should take exactly one non-continuation parameter",
            1,
            nonContinuationTypes.size
        )
        assertEquals(
            "deleteById parameter should be primitive long",
            java.lang.Long.TYPE,
            nonContinuationTypes[0]
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `DAO still has getAll method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getAll" }
        assertNotNull("CompetitionsDao should still have getAll", method)
    }

    @Test
    fun `DAO still has getCompletedCount method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getCompletedCount" }
        assertNotNull("CompetitionsDao should still have getCompletedCount", method)
    }

    @Test
    fun `DAO still has insertAll method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "insertAll" }
        assertNotNull("CompetitionsDao should still have insertAll", method)
    }

    @Test
    fun `DAO still has replaceAll method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "replaceAll" }
        assertNotNull("CompetitionsDao should still have replaceAll", method)
    }
}
