package se.kjellstrand.webshooter.ui.screens.charts.clubstats

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.clubstats.ShooterStats
import se.kjellstrand.webshooter.ui.common.CHART_COLORS
import se.kjellstrand.webshooter.ui.common.CHART_MIN_HEIGHT_FRACTION
import se.kjellstrand.webshooter.ui.common.CHART_SHAPE_RENDERERS
import se.kjellstrand.webshooter.ui.common.ChartStateWrapper
import se.kjellstrand.webshooter.ui.common.ChartLegend
import se.kjellstrand.webshooter.ui.common.UserLegendItem
import se.kjellstrand.webshooter.ui.common.WeaponClassGroupFilter
import se.kjellstrand.webshooter.ui.common.applyBaseChartStyle

@Composable
fun ClubStatsScreen(viewModel: ClubStatsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh()
        }
    }

    var highlightedLegendId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.year, uiState.selectedGroup) {
        highlightedLegendId = null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.web_shooter_club_stats),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
        )
        Text(
            text = stringResource(R.string.club_stats_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
        )

        if (uiState.availableYears.isNotEmpty()) {
            val tabYears: List<Int> = listOf(0) + uiState.availableYears
            val selectedIndex = tabYears.indexOf(uiState.year).coerceAtLeast(0)
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 0.dp
            ) {
                tabYears.forEach { year ->
                    Tab(
                        selected = year == uiState.year,
                        onClick = { viewModel.selectYear(year) },
                        text = {
                            Text(
                                if (year == 0) stringResource(R.string.club_stats_all_years)
                                else year.toString()
                            )
                        }
                    )
                }
            }
        }

        WeaponClassGroupFilter(
            availableGroups = uiState.availableGroups,
            selectedGroup = uiState.selectedGroup,
            onSelectGroup = viewModel::selectWeaponGroup,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        ChartStateWrapper(
            isLoading = uiState.isLoading && uiState.shooterStats.isEmpty(),
            hasError = uiState.hasError,
            isEmpty = uiState.shooterStats.isEmpty()
        ) {
            ClubStatsScatterChart(
                shooterStats = uiState.shooterStats,
                highlightedId = highlightedLegendId,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = screenHeight * CHART_MIN_HEIGHT_FRACTION)
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            val legendItems = uiState.shooterStats.mapIndexed { index, stats ->
                UserLegendItem(
                    label = stats.fullname,
                    color = Color(CHART_COLORS[index % CHART_COLORS.size]),
                    shapeIndex = index % CHART_SHAPE_RENDERERS.size,
                    id = "shooter:${stats.userId}"
                )
            }
            ChartLegend(
                items = legendItems,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = screenHeight * (1f - CHART_MIN_HEIGHT_FRACTION))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                highlightedId = highlightedLegendId,
                onItemClick = { id ->
                    highlightedLegendId = if (highlightedLegendId == id) null else id
                }
            )
        }
    }
}

private class ShooterMarkerView(
    context: Context,
    private val shooterStats: List<ShooterStats>
) : MarkerView(context, R.layout.marker_view) {

    private val textView: TextView = findViewById(R.id.marker_text)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val index = e?.data as? Int
        if (index != null && index in shooterStats.indices) {
            val stats = shooterStats[index]
            val avg = "%.1f".format(stats.averagePoints)
            textView.text = "${stats.fullname}\n$avg p · ${stats.competitionCount} tävlingar"
        } else {
            textView.text = ""
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}

private const val CS_DIMMED_ALPHA = 64
private fun Int.csDimmed(): Int = (this and 0x00FFFFFF) or (CS_DIMMED_ALPHA shl 24)

@SuppressLint("ClickableViewAccessibility")
@Composable
fun ClubStatsScatterChart(
    shooterStats: List<ShooterStats>,
    modifier: Modifier = Modifier,
    highlightedId: String? = null
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val scatterShapeSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { 24.dp.toPx() }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            ScatterChart(context).apply {
                applyBaseChartStyle(onSurfaceColor)
                axisLeft.granularity = 1f
                legend.isEnabled = false
            }
        },
        update = { chart ->
            val signature = shooterStats.joinToString(",") {
                "${it.userId}:${it.averagePoints}:${it.competitionCount}"
            } + "!" + (highlightedId ?: "")
            if (chart.tag == signature) {
                return@AndroidView
            }
            chart.tag = signature

            val scatterShapeSizeDp = scatterShapeSizePx
            val dataSets = shooterStats.mapIndexed { index, stats ->
                val entry = Entry(
                    stats.competitionCount.toFloat(),
                    stats.averagePoints.toFloat()
                ).apply {
                    data = index
                }
                val baseColor = CHART_COLORS[index % CHART_COLORS.size]
                val shooterId = "shooter:${stats.userId}"
                val effectiveColor = if (highlightedId != null && highlightedId != shooterId)
                    baseColor.csDimmed() else baseColor
                ScatterDataSet(listOf(entry), stats.fullname).apply {
                    color = effectiveColor
                    shapeRenderer = CHART_SHAPE_RENDERERS[index % CHART_SHAPE_RENDERERS.size]
                    scatterShapeSize = scatterShapeSizeDp
                    setDrawValues(false)
                }
            }

            if (dataSets.isNotEmpty()) {
                chart.data = ScatterData(dataSets.toList())
            } else {
                chart.data = null
            }

            chart.xAxis.axisMinimum = 0f
            chart.xAxis.textColor = onSurfaceColor
            chart.axisLeft.textColor = onSurfaceColor

            chart.marker = ShooterMarkerView(chart.context, shooterStats)

            chart.invalidate()
        }
    )
}
