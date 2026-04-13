package se.kjellstrand.webshooter.data.competitions

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CompetitionsRepositoryRemovedPagingTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `repository no longer has get method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "get" }
        assertNull(
            "get(page, pageSize) should be removed — observeAll() + syncAll() is the canonical path now",
            method
        )
    }

    @Test
    fun `repository no longer has getLocalAll method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "getLocalAll" }
        assertNull(
            "getLocalAll() should be removed — observeAll() replaces it",
            method
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `repository still has observeAll method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "observeAll" }
        assertNotNull("observeAll should still exist (the canonical read path)", method)
    }

    @Test
    fun `repository still has syncAll method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncAll" }
        assertNotNull("syncAll should still exist (the canonical write path)", method)
    }

    @Test
    fun `repository still has syncCompleted method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncCompleted" }
        assertNotNull("syncCompleted should still exist (called internally by syncAll)", method)
    }

    @Test
    fun `repository still has syncNonCompleted method`() {
        val method = CompetitionsRepository::class.java.methods.find { it.name == "syncNonCompleted" }
        assertNotNull("syncNonCompleted should still exist (called internally by syncAll)", method)
    }
}
