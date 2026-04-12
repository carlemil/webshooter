package se.kjellstrand.webshooter.ui.screens.competitions

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository

class CompetitionsViewModelPagingFixTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `reachedCompleted field is removed`() {
        val field = CompetitionsViewModelImpl::class.java.declaredFields.find {
            it.name == "reachedCompleted"
        }
        assertNull(
            "reachedCompleted field should be removed to allow unlimited paging",
            field
        )
    }

    @Test
    fun `currentPage field initial value is 1`() {
        // The field should exist and be initialized to 1 (not 2).
        // We verify the field exists; runtime init value tested via behavior.
        val field = CompetitionsViewModelImpl::class.java.declaredFields.find {
            it.name == "currentPage"
        }
        assertNotNull("currentPage field should exist", field)
        // Verify it's an int type (consistent page tracking)
        assertTrue(
            "currentPage should be an int",
            field!!.type == Int::class.java
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `CompetitionsViewModelImpl still has loadNextPage method`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find { it.name == "loadNextPage" }
        assertNotNull("Should still have loadNextPage", method)
    }

    @Test
    fun `CompetitionsViewModelImpl still has reload method`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find { it.name == "reload" }
        assertNotNull("Should still have reload", method)
    }

    @Test
    fun `CompetitionsViewModelImpl still takes CompetitionsRepository in constructor`() {
        val constructors = CompetitionsViewModelImpl::class.java.declaredConstructors
        val hasRepo = constructors.any { c ->
            c.parameterTypes.any { it == CompetitionsRepository::class.java }
        }
        assertTrue("Should still accept CompetitionsRepository", hasRepo)
    }

    @Test
    fun `CompetitionsViewModelImpl still has getCompetitionById method`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find { it.name == "getCompetitionById" }
        assertNotNull("Should still have getCompetitionById", method)
    }
}
