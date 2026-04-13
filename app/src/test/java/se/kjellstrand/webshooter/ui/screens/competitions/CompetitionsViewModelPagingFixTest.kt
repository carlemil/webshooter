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

    // --- Guard tests (should PASS before and after fix) ---

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
