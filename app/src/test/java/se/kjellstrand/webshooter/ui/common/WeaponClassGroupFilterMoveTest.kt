package se.kjellstrand.webshooter.ui.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WeaponClassGroupFilterMoveTest {

    private val newLocation =
        File("src/main/java/se/kjellstrand/webshooter/ui/common/WeaponClassGroupFilter.kt")
    private val oldLocation =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/seriespoints/WeaponClassGroupFilter.kt")
    private val seriesPointsScreen =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/seriespoints/SeriesPointsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `WeaponClassGroupFilter lives in ui common now`() {
        assertTrue("Filter must exist at new ui/common location", newLocation.exists())
    }

    @Test
    fun `new file declares ui common package`() {
        assertTrue(
            "new file must exist before package check",
            newLocation.exists()
        )
        val source = newLocation.readText()
        assertTrue(
            "package line must be se.kjellstrand.webshooter.ui.common",
            source.contains("package se.kjellstrand.webshooter.ui.common")
        )
        assertTrue(
            "must declare fun WeaponClassGroupFilter(",
            source.contains("fun WeaponClassGroupFilter(")
        )
    }

    @Test
    fun `old seriespoints location no longer contains the composable`() {
        assertFalse(
            "Old file must be removed from ui/screens/seriespoints/",
            oldLocation.exists()
        )
    }

    @Test
    fun `SeriesPointsScreen imports WeaponClassGroupFilter from ui common`() {
        val source = seriesPointsScreen.readText()
        assertTrue(
            "SeriesPointsScreen must import the composable from ui.common",
            source.contains("se.kjellstrand.webshooter.ui.common.WeaponClassGroupFilter")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `WeaponClassGroup enum stays in seriespoints state`() {
        val source = File(
            "src/main/java/se/kjellstrand/webshooter/ui/screens/seriespoints/SeriesPointsUiState.kt"
        ).readText()
        assertTrue(
            "WeaponClassGroup enum must remain in SeriesPointsUiState",
            source.contains("enum class WeaponClassGroup")
        )
    }

    @Test
    fun `SeriesPointsScreen still calls WeaponClassGroupFilter`() {
        val source = seriesPointsScreen.readText()
        assertTrue(source.contains("WeaponClassGroupFilter("))
    }
}
