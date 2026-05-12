package se.kjellstrand.webshooter.ui.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AddShooterButtonTest {

    private val buttonFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/common/AddShooterButton.kt")
    private val chartsScreen =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsScreen.kt")
    private val seriesPointsScreen =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/seriespoints/SeriesPointsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `AddShooterButton source file exists in ui common`() {
        assertTrue(
            "AddShooterButton.kt must exist at ui/common/AddShooterButton.kt",
            buttonFile.exists()
        )
    }

    @Test
    fun `AddShooterButton composable signature is declared`() {
        val source = buttonFile.readText()
        assertTrue(
            "AddShooterButton should be marked @Composable",
            source.contains("@Composable")
        )
        assertTrue(
            "AddShooterButton must accept text, onClick and modifier",
            source.contains("fun AddShooterButton(") &&
                source.contains("text: String") &&
                source.contains("onClick: () -> Unit") &&
                source.contains("modifier: Modifier")
        )
        assertTrue(
            "AddShooterButton should use OutlinedButton with PersonAdd icon",
            source.contains("OutlinedButton(") &&
                source.contains("Icons.Default.PersonAdd")
        )
    }

    @Test
    fun `ChartsScreen uses AddShooterButton instead of inline OutlinedButton`() {
        val source = chartsScreen.readText()
        assertTrue(
            "ChartsScreen must call AddShooterButton(",
            source.contains("AddShooterButton(")
        )
        assertFalse(
            "ChartsScreen must not construct Icons.Default.PersonAdd directly any more",
            source.contains("Icons.Default.PersonAdd")
        )
    }

    @Test
    fun `SeriesPointsScreen uses AddShooterButton instead of inline OutlinedButton`() {
        val source = seriesPointsScreen.readText()
        assertTrue(
            "SeriesPointsScreen must call AddShooterButton(",
            source.contains("AddShooterButton(")
        )
        assertFalse(
            "SeriesPointsScreen must not construct Icons.Default.PersonAdd directly any more",
            source.contains("Icons.Default.PersonAdd")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartsScreen still references charts_add_shooter string`() {
        val source = chartsScreen.readText()
        assertTrue(source.contains("Res.string.charts_add_shooter"))
    }

    @Test
    fun `SeriesPointsScreen still references series_points_change_shooter string`() {
        val source = seriesPointsScreen.readText()
        assertTrue(source.contains("Res.string.series_points_change_shooter"))
    }

    @Test
    fun `ui common directory exists`() {
        assertTrue(File("src/main/java/se/kjellstrand/webshooter/ui/common").isDirectory)
    }
}
