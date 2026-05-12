package se.kjellstrand.webshooter.ui.screens.competitions

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CompetitionsViewModelOnScreenOpenedTest {

    private val viewModelSource: String by lazy {
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/competitions/CompetitionsViewModelImpl.kt").readText()
    }

    private val interfaceSource: String by lazy {
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/competitions/CompetitionsViewModel.kt").readText()
    }

    private val screenSource: String by lazy {
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/competitions/CompetitionsScreen.kt").readText()
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `CompetitionsViewModel interface declares onScreenOpened`() {
        val method = CompetitionsViewModel::class.java.methods.find { it.name == "onScreenOpened" }
        assertNotNull(
            "onScreenOpened() should be declared on the CompetitionsViewModel interface",
            method
        )
    }

    @Test
    fun `CompetitionsViewModelImpl implements onScreenOpened`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find { it.name == "onScreenOpened" }
        assertNotNull(
            "CompetitionsViewModelImpl should implement onScreenOpened()",
            method
        )
    }

    @Test
    fun `CompetitionsViewModelImpl source declares hasBeenOpenedOnce flag`() {
        assertTrue(
            "Impl should track first-open state via a hasBeenOpenedOnce flag",
            viewModelSource.contains("hasBeenOpenedOnce")
        )
    }

    @Test
    fun `onScreenOpened body references reload`() {
        val idx = viewModelSource.indexOf("override fun onScreenOpened()")
        assertTrue("onScreenOpened() should be overridden in the Impl", idx >= 0)
        val body = viewModelSource.substring(idx, (idx + 600).coerceAtMost(viewModelSource.length))
        assertTrue(
            "onScreenOpened body should call reload() on subsequent opens",
            body.contains("reload()")
        )
        assertTrue(
            "onScreenOpened body should branch on hasBeenOpenedOnce",
            body.contains("hasBeenOpenedOnce")
        )
    }

    @Test
    fun `CompetitionsScreen composable calls onScreenOpened via LaunchedEffect`() {
        assertTrue(
            "CompetitionsScreen should invoke viewModel.onScreenOpened() (wired in a LaunchedEffect)",
            screenSource.contains("onScreenOpened()")
        )
        assertTrue(
            "CompetitionsScreen should use LaunchedEffect to trigger onScreenOpened on each screen entry",
            screenSource.contains("LaunchedEffect")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `CompetitionsViewModelImpl still has reload method`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find { it.name == "reload" }
        assertNotNull("reload() must remain — onScreenOpened delegates to it", method)
    }

    @Test
    fun `CompetitionsViewModel interface still declares reload`() {
        assertTrue(
            "Interface should still declare reload()",
            interfaceSource.contains("fun reload()")
        )
    }

    @Test
    fun `CompetitionsViewModelImpl source still observes repository observeAll`() {
        assertTrue(
            "Impl should still subscribe to competitionsRepository.observeAll()",
            viewModelSource.contains("competitionsRepository.observeAll()")
        )
    }

    @Test
    fun `CompetitionsViewModelImpl source still has init-block self sync`() {
        assertTrue(
            "The init-block self-sync path (syncTriggered) must not regress",
            viewModelSource.contains("syncTriggered")
        )
    }
}
