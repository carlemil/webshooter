package se.kjellstrand.webshooter.ui.screens.clubstats

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ClubStatsScreenTest {

    private val sourceFile = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/clubstats/ClubStatsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ClubStatsScreen file exists`() {
        assertTrue(
            "ClubStatsScreen.kt must exist",
            sourceFile.exists()
        )
    }

    @Test
    fun `ClubStatsScreen has composable function`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsScreen must have @Composable annotation",
            source.contains("@Composable")
        )
        assertTrue(
            "ClubStatsScreen must have ClubStatsScreen function",
            source.contains("fun ClubStatsScreen(")
        )
    }

    @Test
    fun `ClubStatsScreen takes ClubStatsViewModel parameter`() {
        val source = sourceFile.readText()
        val pattern = Regex("""fun\s+ClubStatsScreen\s*\([\s\S]*ClubStatsViewModel""")
        assertTrue(
            "ClubStatsScreen must accept ClubStatsViewModel parameter",
            pattern.containsMatchIn(source)
        )
    }

    @Test
    fun `ClubStatsScreen uses collectAsState`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsScreen must use collectAsState",
            source.contains("collectAsState()")
        )
    }

    @Test
    fun `ClubStatsScreen renders the scatter via Compose Canvas (no MPAndroidChart)`() {
        val source = sourceFile.readText()
        // After the chart-library migration the screen is pure Compose Canvas
        // using the shared ChartShape/drawScatterShape helpers.
        assertTrue(
            "ClubStatsScreen must render via Compose Canvas",
            source.contains("Canvas(")
        )
        assertTrue(
            "ClubStatsScreen must use the shared ChartShape helpers",
            source.contains("drawScatterShape(")
        )
        assertFalse(
            "ClubStatsScreen must NOT use AndroidView (chart is now pure Compose)",
            source.contains("AndroidView")
        )
        assertFalse(
            "ClubStatsScreen must NOT import MPAndroidChart classes",
            source.contains("com.github.mikephil")
        )
    }

    @Test
    fun `ClubStatsScreen shows loading state via ChartStateWrapper`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsScreen must use ChartStateWrapper to render loading/error/empty states",
            source.contains("ChartStateWrapper(")
        )
        assertTrue(
            "ChartStateWrapper must be wired to uiState.isLoading",
            source.contains("isLoading = uiState.isLoading")
        )
    }

    @Test
    fun `ClubStatsScreen uses shared ChartLegend from ui common`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsScreen must import ChartLegend from ui.common",
            source.contains("se.kjellstrand.webshooter.ui.common.ChartLegend")
        )
        assertTrue(
            "ClubStatsScreen must import UserLegendItem from ui.common",
            source.contains("se.kjellstrand.webshooter.ui.common.UserLegendItem")
        )
        assertTrue(
            "ClubStatsScreen must call ChartLegend(",
            source.contains("ChartLegend(")
        )
        assertTrue(
            "ClubStatsScreen must construct UserLegendItem entries",
            source.contains("UserLegendItem(")
        )
    }

    @Test
    fun `ClubStatsScreen no longer defines local ShooterLegend composable`() {
        val source = sourceFile.readText()
        assertFalse(
            "Local fun ShooterLegend(...) must be removed",
            source.contains("fun ShooterLegend(")
        )
    }

    @Test
    fun `ClubStatsScreen no longer defines local drawScatterShape helper`() {
        val source = sourceFile.readText()
        assertFalse(
            "Local DrawScope.drawScatterShape must be removed",
            source.contains("DrawScope.drawScatterShape")
        )
    }

    @Test
    fun `ClubStatsScreen renders WeaponClassGroupFilter wired to ViewModel`() {
        val source = sourceFile.readText()
        assertTrue(
            "ClubStatsScreen must import WeaponClassGroupFilter from ui.common",
            source.contains("se.kjellstrand.webshooter.ui.common.WeaponClassGroupFilter")
        )
        assertTrue(
            "ClubStatsScreen must call WeaponClassGroupFilter(",
            source.contains("WeaponClassGroupFilter(")
        )
        assertTrue(
            "WeaponClassGroupFilter must be wired to uiState.availableGroups",
            source.contains("availableGroups = uiState.availableGroups")
        )
        assertTrue(
            "WeaponClassGroupFilter must be wired to uiState.selectedGroup",
            source.contains("selectedGroup = uiState.selectedGroup")
        )
        assertTrue(
            "WeaponClassGroupFilter onSelectGroup must call viewModel::selectWeaponGroup",
            source.contains("viewModel::selectWeaponGroup")
        )
    }

    @Test
    fun `ShooterMarkerView shows averagePoints and competitionCount`() {
        val source = sourceFile.readText()
        assertTrue(
            "Marker must reference stats.averagePoints",
            source.contains("averagePoints")
        )
        assertTrue(
            "Marker must reference stats.competitionCount",
            source.contains("competitionCount")
        )
        assertTrue(
            "Marker text must include the Swedish 'tävlingar' label",
            source.contains("tävlingar")
        )
        assertTrue(
            "Marker text must include the 'p' (points) unit next to averagePoints",
            Regex("""averagePoints[^\n]*\bp\b""").containsMatchIn(source) ||
                Regex("""\bp\b[^\n]*averagePoints""").containsMatchIn(source) ||
                source.contains("p · ")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ResultsTrendsScreen still exists`() {
        assertTrue(
            File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsScreen.kt").exists()
        )
    }

    @Test
    fun `ClubStatsUiState still exists`() {
        assertTrue(
            File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/clubstats/ClubStatsUiState.kt").exists()
        )
    }

    @Test
    fun `ClubStatsViewModel still exists`() {
        assertTrue(
            File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/clubstats/ClubStatsViewModel.kt").exists()
        )
    }
}