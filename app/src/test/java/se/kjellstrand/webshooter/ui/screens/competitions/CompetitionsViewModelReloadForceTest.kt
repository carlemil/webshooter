package se.kjellstrand.webshooter.ui.screens.competitions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CompetitionsViewModelReloadForceTest {

    private val viewModelSource: String by lazy {
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/competitions/CompetitionsViewModelImpl.kt").readText()
    }

    private fun reloadBody(): String {
        val reloadStart = viewModelSource.indexOf("override fun reload()")
        assertTrue("reload() should exist in source", reloadStart >= 0)
        val afterReload = reloadStart + "override fun reload()".length
        val nextOverride = viewModelSource.indexOf("override fun", afterReload)
        return if (nextOverride >= 0)
            viewModelSource.substring(reloadStart, nextOverride)
        else
            viewModelSource.substring(reloadStart)
    }

    private fun initBody(): String {
        val initStart = viewModelSource.indexOf("init {")
        assertTrue("init block should exist", initStart >= 0)
        val nextOverride = viewModelSource.indexOf("override fun", initStart)
        return if (nextOverride >= 0)
            viewModelSource.substring(initStart, nextOverride)
        else
            viewModelSource.substring(initStart)
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `reload body calls syncAll with force true`() {
        assertTrue(
            "reload() body should call syncAll(force = true) so manual reload bypasses the incremental optimization",
            reloadBody().contains("syncAll(force = true)")
        )
    }

    @Test
    fun `reload body does not call syncAll with empty parens`() {
        assertFalse(
            "reload() body should no longer contain 'syncAll()' — it should use syncAll(force = true)",
            reloadBody().contains("syncAll()")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `init block still calls syncAll without force`() {
        assertTrue(
            "init block should still call syncAll() (the incremental default) for cheap self-sync on empty DB",
            initBody().contains("syncAll()")
        )
    }

    @Test
    fun `init block does not call syncAll with force true`() {
        assertFalse(
            "init block should NOT call syncAll(force = true) — only the manual reload should force a full sync",
            initBody().contains("syncAll(force = true)")
        )
    }

    @Test
    fun `reload method still exists on CompetitionsViewModelImpl`() {
        val method = CompetitionsViewModelImpl::class.java.methods.find { it.name == "reload" }
        assertNotNull("reload should still exist", method)
    }

    @Test
    fun `init block still references syncTriggered flag`() {
        assertTrue(
            "init block should still use the syncTriggered flag to avoid repeated self-syncs",
            initBody().contains("syncTriggered")
        )
    }
}
