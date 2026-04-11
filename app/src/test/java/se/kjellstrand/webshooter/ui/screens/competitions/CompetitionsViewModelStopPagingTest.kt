package se.kjellstrand.webshooter.ui.screens.competitions

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository

class CompetitionsViewModelStopPagingTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `CompetitionsViewModelImpl has reachedCompleted field`() {
        val field = CompetitionsViewModelImpl::class.java.declaredFields.find {
            it.name == "reachedCompleted"
        }
        assertNotNull(
            "CompetitionsViewModelImpl should have a reachedCompleted field",
            field
        )
    }

    @Test
    fun `reachedCompleted field is boolean type`() {
        val field = CompetitionsViewModelImpl::class.java.declaredFields.find {
            it.name == "reachedCompleted"
        }
        assertNotNull("reachedCompleted should exist", field)
        assertTrue(
            "reachedCompleted should be a boolean",
            field!!.type == Boolean::class.java || field.type == java.lang.Boolean::class.java
        )
    }

    @Test
    fun `CompetitionsViewModelImpl has appendLocalCompleted method`() {
        val method = CompetitionsViewModelImpl::class.java.declaredMethods.find {
            it.name == "appendLocalCompleted"
        }
        assertNotNull(
            "CompetitionsViewModelImpl should have an appendLocalCompleted method",
            method
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
}
