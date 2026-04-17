package se.kjellstrand.webshooter.ui.common

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UserLegendTest {

    private val userLegendFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/common/UserLegend.kt")
    private val commonDir = File("src/main/java/se/kjellstrand/webshooter/ui/common")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `UserLegend source file exists in ui common`() {
        assertTrue(
            "UserLegend.kt should exist at ui/common/UserLegend.kt",
            userLegendFile.exists()
        )
    }

    @Test
    fun `UserLegendItem data class is declared with label color and shapeIndex`() {
        val source = userLegendFile.readText()
        assertTrue(
            "UserLegendItem data class should exist",
            source.contains("data class UserLegendItem")
        )
        assertTrue("UserLegendItem should have label: String", source.contains("label: String"))
        assertTrue("UserLegendItem should have color: Color", source.contains("color: Color"))
        assertTrue(
            "UserLegendItem should have shapeIndex: Int",
            source.contains("shapeIndex: Int")
        )
    }

    @Test
    fun `UserLegend composable function is declared`() {
        val source = userLegendFile.readText()
        assertTrue(
            "UserLegend composable should exist and accept items + modifier",
            source.contains("fun UserLegend(") &&
                source.contains("items: List<UserLegendItem>") &&
                source.contains("modifier: Modifier")
        )
        assertTrue(
            "UserLegend should be marked @Composable",
            source.contains("@Composable")
        )
    }

    @Test
    fun `UserLegend uses FlowRow with scroll state and draws shape per item`() {
        val source = userLegendFile.readText()
        assertTrue("UserLegend should lay out with FlowRow", source.contains("FlowRow"))
        assertTrue(
            "UserLegend should scroll vertically via rememberScrollState",
            source.contains("verticalScroll") && source.contains("rememberScrollState")
        )
        assertTrue(
            "UserLegend should draw scatter shapes via a DrawScope helper",
            source.contains("drawScatterShape")
        )
    }

    @Test
    fun `UserLegend supports horizontal line shape at index 7`() {
        val source = userLegendFile.readText()
        assertTrue(
            "drawScatterShape must handle shapeIndex 7 as a horizontal line",
            Regex("""7\s*->\s*\{[\s\S]{0,400}drawLine""").containsMatchIn(source)
        )
    }

    @Test
    fun `UserLegend supports sloped line shape at index 8`() {
        val source = userLegendFile.readText()
        assertTrue(
            "drawScatterShape must handle shapeIndex 8 as a sloped line",
            Regex("""8\s*->\s*\{[\s\S]{0,400}drawLine""").containsMatchIn(source)
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ui common directory exists`() {
        assertTrue("ui/common directory should exist", commonDir.exists() && commonDir.isDirectory)
    }

    @Test
    fun `ChartStyles still exposes CHART_SHAPE_RENDERERS and CHART_COLORS`() {
        val chartStyles = File("src/main/java/se/kjellstrand/webshooter/ui/common/ChartStyles.kt")
        assertTrue("ChartStyles.kt should exist", chartStyles.exists())
        val source = chartStyles.readText()
        assertTrue(source.contains("CHART_COLORS"))
        assertTrue(source.contains("CHART_SHAPE_RENDERERS"))
    }
}
