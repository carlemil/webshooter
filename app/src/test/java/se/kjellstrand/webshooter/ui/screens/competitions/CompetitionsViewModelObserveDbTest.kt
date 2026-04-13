package se.kjellstrand.webshooter.ui.screens.competitions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import java.io.File

class CompetitionsViewModelObserveDbTest {

    private val viewModelSource: String by lazy {
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/competitions/CompetitionsViewModelImpl.kt").readText()
    }

    private val interfaceSource: String by lazy {
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/competitions/CompetitionsViewModel.kt").readText()
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `CompetitionsViewModel interface no longer declares loadNextPage`() {
        val method = CompetitionsViewModel::class.java.methods.find { it.name == "loadNextPage" }
        assertNull(
            "loadNextPage should be removed from the interface — DB is the source of truth, no pagination",
            method
        )
    }

    @Test
    fun `CompetitionsViewModelImpl no longer has loadNextPage method`() {
        val method = CompetitionsViewModelImpl::class.java.declaredMethods.find {
            it.name == "loadNextPage"
        }
        assertNull(
            "loadNextPage should be removed from the impl",
            method
        )
    }

    @Test
    fun `CompetitionsViewModelImpl no longer has currentPage field`() {
        val field = CompetitionsViewModelImpl::class.java.declaredFields.find {
            it.name == "currentPage"
        }
        assertNull(
            "currentPage should be removed — pagination is gone",
            field
        )
    }

    @Test
    fun `CompetitionsViewModelImpl source observes repository observeAll`() {
        assertTrue(
            "Impl should call competitionsRepository.observeAll() to drive UI state",
            viewModelSource.contains("competitionsRepository.observeAll()")
        )
    }

    @Test
    fun `CompetitionsViewModelImpl source calls syncAll for reload`() {
        assertTrue(
            "Impl should call competitionsRepository.syncAll() (used by reload())",
            viewModelSource.contains("competitionsRepository.syncAll()")
        )
    }

    @Test
    fun `CompetitionsViewModelImpl source no longer calls repository get`() {
        // The old paging path went through competitionsRepository.get(page, pageSize).
        // After the refactor, that call should be gone.
        assertFalse(
            "Impl should no longer call competitionsRepository.get(",
            viewModelSource.contains("competitionsRepository.get(")
        )
    }

    @Test
    fun `interface source no longer mentions loadNextPage`() {
        assertFalse(
            "Interface source should not declare loadNextPage anymore",
            interfaceSource.contains("loadNextPage")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `CompetitionsViewModelImpl still has reload method`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find { it.name == "reload" }
        assertNotNull("reload should still exist", method)
    }

    @Test
    fun `CompetitionsViewModelImpl still has getCompetitionById method`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find { it.name == "getCompetitionById" }
        assertNotNull("getCompetitionById should still exist", method)
    }

    @Test
    fun `CompetitionsViewModelImpl still has setSelectedCompetitionTypeIds method`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find {
            it.name == "setSelectedCompetitionTypeIds"
        }
        assertNotNull("setSelectedCompetitionTypeIds should still exist", method)
    }

    @Test
    fun `CompetitionsViewModelImpl still has setSelectedStatuses method`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find {
            it.name == "setSelectedStatuses"
        }
        assertNotNull("setSelectedStatuses should still exist", method)
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
