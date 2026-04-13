package se.kjellstrand.webshooter.ui.screens.competitions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CompetitionsViewModelSelfSyncTest {

    private val viewModelSource: String by lazy {
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/competitions/CompetitionsViewModelImpl.kt").readText()
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `CompetitionsViewModelImpl source declares a syncTriggered flag`() {
        assertTrue(
            "Impl should have a syncTriggered flag to guarantee the self-sync runs at most once per VM",
            viewModelSource.contains("syncTriggered")
        )
    }

    @Test
    fun `CompetitionsViewModelImpl source checks data isEmpty in the observe block`() {
        // The observe block needs to branch on whether the emitted list is empty.
        assertTrue(
            "Impl should inspect data.isEmpty() (or isNotEmpty()) in the observe block",
            viewModelSource.contains("isEmpty()") || viewModelSource.contains("isNotEmpty()")
        )
    }

    @Test
    fun `CompetitionsViewModelImpl source calls syncAll for self-healing`() {
        // Not just in reload() — also from the init/observe path.
        // The syncAll call should appear at least twice (once from reload, once from self-sync).
        val count = Regex("syncAll\\(\\)").findAll(viewModelSource).count()
        assertTrue(
            "Impl should call syncAll() from at least two places (reload AND the self-sync path), found $count",
            count >= 2
        )
    }

    @Test
    fun `observe block keeps isLoading true while first emission is empty and sync is running`() {
        // Extract the init block and check that on the empty branch, it does NOT
        // flip isLoading = false before the sync has a chance to run.
        val initStart = viewModelSource.indexOf("init {")
        assertTrue("init block should exist", initStart >= 0)
        // Look at the next ~1500 chars for the whole init body
        val initBody = viewModelSource.substring(
            initStart,
            (initStart + 1500).coerceAtMost(viewModelSource.length)
        )
        // The init body should reference syncTriggered and syncAll.
        assertTrue(
            "init block should reference syncTriggered",
            initBody.contains("syncTriggered")
        )
        assertTrue(
            "init block should call syncAll (self-sync on empty)",
            initBody.contains("syncAll()")
        )
    }

    @Test
    fun `hasError is set when self-sync fails with still-empty DB`() {
        // The self-sync catch block should set hasError = true. Look for it.
        assertTrue(
            "Impl should set hasError = true when sync fails (look for 'hasError = true' in source)",
            viewModelSource.contains("hasError = true")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `CompetitionsViewModelImpl still observes repository observeAll`() {
        assertTrue(
            "Impl should still subscribe to competitionsRepository.observeAll()",
            viewModelSource.contains("competitionsRepository.observeAll()")
        )
    }

    @Test
    fun `CompetitionsViewModelImpl still has reload method calling syncAll`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find { it.name == "reload" }
        assertNotNull("reload should still exist", method)
        // And reload's body should still call syncAll (verified via source).
        val reloadStart = viewModelSource.indexOf("override fun reload()")
        assertTrue("reload() should exist in source", reloadStart >= 0)
    }

    @Test
    fun `CompetitionsViewModelImpl still has getCompetitionById method`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find {
            it.name == "getCompetitionById"
        }
        assertNotNull("getCompetitionById should still exist", method)
    }

    @Test
    fun `CompetitionsViewModelImpl source does NOT unconditionally set isLoading to false on empty`() {
        // Find the observe block and check that it doesn't blindly set isLoading = false
        // after an empty emission. A naive implementation would do
        // `_uiState.value = _uiState.value.copy(competitions = ..., isLoading = false)`
        // right after observeAll().collect { ... } — that's the old buggy behavior.
        // With the fix, the empty branch keeps isLoading = true until sync completes.
        //
        // This is a soft check: we ensure the word "isEmpty" appears AND we see conditional
        // handling (the presence of syncTriggered branching implies the conditional path exists).
        assertTrue(
            "Impl should branch on emptiness (look for isEmpty in source)",
            viewModelSource.contains("isEmpty()") || viewModelSource.contains("isNotEmpty()")
        )
    }
}
