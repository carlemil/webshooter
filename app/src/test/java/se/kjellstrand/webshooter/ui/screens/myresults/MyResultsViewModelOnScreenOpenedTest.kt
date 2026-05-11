package se.kjellstrand.webshooter.ui.screens.myresults

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MyResultsViewModelOnScreenOpenedTest {

    private val viewModelSource: String by lazy {
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/myresults/MyResultsViewModelImpl.kt").readText()
    }

    private val interfaceSource: String by lazy {
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/myresults/MyResultsViewModel.kt").readText()
    }

    private val screenSource: String by lazy {
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/myresults/MyResultsScreen.kt").readText()
    }

    private val mockSource: String by lazy {
        File("src/main/java/se/kjellstrand/webshooter/ui/mock/MyResultsViewModelMock.kt").readText()
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `MyResultsViewModel interface declares onScreenOpened`() {
        val method = MyResultsViewModel::class.java.methods.find { it.name == "onScreenOpened" }
        assertNotNull(
            "onScreenOpened() should be declared on the MyResultsViewModel interface",
            method
        )
    }

    @Test
    fun `MyResultsViewModelImpl implements onScreenOpened`() {
        val method = MyResultsViewModelImpl::class.java.methods.find { it.name == "onScreenOpened" }
        assertNotNull(
            "MyResultsViewModelImpl should implement onScreenOpened()",
            method
        )
    }

    @Test
    fun `MyResultsViewModelImpl source declares hasSyncedOnce flag`() {
        assertTrue(
            "Impl should track first-open state via a hasSyncedOnce flag",
            viewModelSource.contains("hasSyncedOnce")
        )
    }

    @Test
    fun `onScreenOpened body calls syncAll once`() {
        val idx = viewModelSource.indexOf("override fun onScreenOpened()")
        assertTrue("onScreenOpened() should be overridden in the Impl", idx >= 0)
        val body = viewModelSource.substring(idx, (idx + 800).coerceAtMost(viewModelSource.length))
        assertTrue(
            "onScreenOpened body should branch on hasSyncedOnce",
            body.contains("hasSyncedOnce")
        )
        assertTrue(
            "onScreenOpened body should call competitionsRepository.syncAll()",
            body.contains("competitionsRepository.syncAll()")
        )
    }

    @Test
    fun `MyResultsViewModelImpl injects CompetitionsRepository`() {
        assertTrue(
            "Impl should inject CompetitionsRepository to call syncAll() from onScreenOpened",
            viewModelSource.contains("competitionsRepository: CompetitionsRepository")
        )
    }

    @Test
    fun `MyResultsScreen composable calls onScreenOpened via LaunchedEffect`() {
        assertTrue(
            "MyResultsScreen should invoke viewModel.onScreenOpened() (wired in a LaunchedEffect)",
            screenSource.contains("onScreenOpened()")
        )
        assertTrue(
            "MyResultsScreen should use LaunchedEffect to trigger onScreenOpened on each screen entry",
            screenSource.contains("LaunchedEffect")
        )
    }

    @Test
    fun `MyResultsViewModelMock implements onScreenOpened`() {
        val method = se.kjellstrand.webshooter.ui.mock.MyResultsViewModelMock::class.java.methods
            .find { it.name == "onScreenOpened" }
        assertNotNull("MyResultsViewModelMock must implement onScreenOpened()", method)
        assertTrue(
            "Mock onScreenOpened should be a no-op override",
            mockSource.contains("override fun onScreenOpened()")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `MyResultsViewModelImpl still has reload method`() {
        val method = MyResultsViewModelImpl::class.java.methods.find { it.name == "reload" }
        assertNotNull("reload() must remain", method)
    }

    @Test
    fun `MyResultsViewModel interface still declares reload`() {
        assertTrue(
            "Interface should still declare reload()",
            interfaceSource.contains("fun reload()")
        )
    }

    @Test
    fun `MyResultsViewModelImpl source still calls fetchResultStats`() {
        assertTrue(
            "Impl should re-pull stats after a successful sync",
            viewModelSource.contains("fetchResultStats(")
        )
    }

    @Test
    fun `onScreenOpened body does not unconditionally re-run on every call`() {
        val idx = viewModelSource.indexOf("override fun onScreenOpened()")
        assertTrue("onScreenOpened() should be overridden in the Impl", idx >= 0)
        val body = viewModelSource.substring(idx, (idx + 800).coerceAtMost(viewModelSource.length))
        // First-open-only: there must be an early-return guarded by the flag.
        assertTrue(
            "onScreenOpened should return early when hasSyncedOnce is true",
            body.contains("if (hasSyncedOnce)") || body.contains("hasSyncedOnce -> return")
        )
        assertFalse(
            "onScreenOpened must not call reload() unconditionally; that would re-run on every entry",
            Regex("override fun onScreenOpened\\(\\)\\s*\\{[^}]*reload\\(\\)").containsMatchIn(body)
        )
    }
}
