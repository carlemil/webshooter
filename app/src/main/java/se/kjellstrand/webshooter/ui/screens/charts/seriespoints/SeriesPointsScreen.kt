package se.kjellstrand.webshooter.ui.screens.charts.seriespoints

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.seriespoints.CompetitionSeries
import se.kjellstrand.webshooter.ui.common.AddShooterButton
import se.kjellstrand.webshooter.ui.common.CHART_COLORS
import se.kjellstrand.webshooter.ui.common.CHART_MIN_HEIGHT_FRACTION
import se.kjellstrand.webshooter.ui.common.ChartStateWrapper
import se.kjellstrand.webshooter.ui.common.ShooterPickerDialog
import se.kjellstrand.webshooter.ui.common.ChartLegend
import se.kjellstrand.webshooter.ui.common.UserLegendItem
import se.kjellstrand.webshooter.ui.common.WeaponClassGroupFilter
import se.kjellstrand.webshooter.ui.common.applyBaseChartStyle
import android.graphics.Color as AndroidColor

private val LATEST_COLOR = AndroidColor.rgb(76, 255, 120) // bright accent
private val OLDER_HSV = floatArrayOf(120f, 0.55f, 1f) // green hue, ramped value

private fun colorForAge(indexFromNewest: Int, total: Int): Int {
    if (indexFromNewest == 0) return LATEST_COLOR
    val denom = (total - 1).coerceAtLeast(1)
    val t = indexFromNewest.toFloat() / denom
    val value = 0.75f - 0.55f * t
    return AndroidColor.HSVToColor(floatArrayOf(OLDER_HSV[0], OLDER_HSV[1], value))
}

@Composable
fun SeriesPointsScreen(viewModel: SeriesPointsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.series_points_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
            )
            ChartStateWrapper(
                isLoading = uiState.isLoading && uiState.competitions.isEmpty(),
                hasError = uiState.hasError
            ) {
                SeriesPointsContent(uiState, viewModel)
            }
        }
    }

    if (uiState.showSearchDialog) {
        ShooterPickerDialog(
            title = stringResource(R.string.series_points_select_shooter),
            searchQuery = uiState.searchQuery,
            clubMembers = uiState.clubMembers,
            allParticipants = uiState.allParticipants,
            selectedShooters = emptyMap(),
            onSearchQueryChanged = viewModel::setSearchQuery,
            onPickShooter = viewModel::selectShooter,
            onRemoveShooter = {},
            onDismiss = { viewModel.setShowSearchDialog(false) },
            showSelectedSection = false,
            relevantUserIds = null
        )
    }
}

@Composable
private fun SeriesPointsContent(
    uiState: SeriesPointsUiState,
    viewModel: SeriesPointsViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = uiState.selectedUserName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp)
        )

        if (uiState.availableYears.isNotEmpty()) {
            val tabYears: List<Int> = listOf(0) + uiState.availableYears
            val selectedIndex = tabYears.indexOf(uiState.selectedYear).coerceAtLeast(0)
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 0.dp
            ) {
                tabYears.forEach { year ->
                    Tab(
                        selected = year == uiState.selectedYear,
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

        val competitions = uiState.filteredCompetitions
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp

        if (competitions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.charts_no_data),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            SeriesPointsChart(
                competitions = competitions,
                seriesAverage = uiState.seriesAverage,
                seriesTrend = uiState.seriesTrend,
                modifier = Modifier
                    .height(screenHeight * CHART_MIN_HEIGHT_FRACTION)
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            val legendItems = buildList {
                if (uiState.seriesAverage != null) {
                    add(
                        UserLegendItem(
                            label = stringResource(R.string.charts_legend_average),
                            color = Color(CHART_COLORS[1]),
                            shapeIndex = 7
                        )
                    )
                }
                if (uiState.seriesTrend != null) {
                    add(
                        UserLegendItem(
                            label = stringResource(R.string.charts_legend_trend),
                            color = Color(CHART_COLORS[7]),
                            shapeIndex = 8
                        )
                    )
                }
                val ordered = competitions.asReversed()
                val total = ordered.size
                ordered.forEachIndexed { indexFromNewest, comp ->
                    add(
                        UserLegendItem(
                            label = "${comp.date} ${comp.competitionName}",
                            color = Color(colorForAge(indexFromNewest, total)),
                            shapeIndex = 7
                        )
                    )
                }
            }
            if (legendItems.isNotEmpty()) {
                ChartLegend(
                    items = legendItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        AddShooterButton(
            text = stringResource(R.string.series_points_change_shooter),
            onClick = { viewModel.setShowSearchDialog(true) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private class SeriesPointsMarkerView(
    context: Context,
    private val labels: Map<Pair<Int, Int>, String>
) : MarkerView(context, R.layout.marker_view) {

    private val textView: TextView = findViewById(R.id.marker_text)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val dsIndex = highlight?.dataSetIndex ?: -1
        val xIndex = (e?.x?.toInt() ?: 0) - 1
        textView.text = labels[dsIndex to xIndex] ?: ""
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}

private val TREND_COLOR_ARGB: Int = CHART_COLORS[7]
private val AVERAGE_COLOR_ARGB: Int = CHART_COLORS[1]

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun SeriesPointsChart(
    competitions: List<CompetitionSeries>,
    seriesAverage: Float?,
    seriesTrend: SeriesPointsUiState.TrendLine?,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                applyBaseChartStyle(onSurfaceColor)
                legend.isEnabled = false
            }
        },
        update = { chart ->
            // Defensive: ensure the built-in legend stays off across
            // recompositions and any stale custom-entry state is cleared,
            // otherwise LegendRenderer can crash with IOOB when entries and
            // calculated label sizes get out of sync.
            chart.legend.resetCustom()
            chart.legend.isEnabled = false

            val signature = buildString {
                competitions.forEach { comp ->
                    append(comp.competitionId).append(':')
                    comp.seriesPoints.forEach { append(it).append(',') }
                    append(';')
                }
                append('@').append(seriesAverage ?: "")
                append('~')
                if (seriesTrend != null) {
                    append(seriesTrend.fromX).append(',').append(seriesTrend.fromY).append(';')
                        .append(seriesTrend.toX).append(',').append(seriesTrend.toY)
                }
            }
            if (chart.tag == signature) return@AndroidView
            chart.tag = signature

            // Competitions are sorted by date ascending, so the last entry is
            // the newest. Iterate from newest→oldest so the newest renders on top.
            val ordered = competitions.reversed()
            val total = ordered.size
            val builtDataSets = mutableListOf<Pair<LineDataSet, List<String>>>()

            ordered.forEachIndexed { indexFromNewest, comp ->
                if (comp.seriesPoints.isEmpty()) return@forEachIndexed
                val entries = comp.seriesPoints.mapIndexed { i, points ->
                    Entry((i + 1).toFloat(), points.toFloat())
                }
                val perEntryLabels = comp.seriesPoints.map { points -> "$points p" }
                val isLatest = indexFromNewest == 0
                val lineColor = colorForAge(indexFromNewest, total)
                val label = "${comp.date} ${comp.competitionName}"
                val dataSet = LineDataSet(entries, label).apply {
                    color = lineColor
                    lineWidth = if (isLatest) 3.5f else 1.5f
                    setDrawCircles(isLatest)
                    if (isLatest) {
                        setCircleColor(lineColor)
                        circleRadius = 4f
                        setDrawCircleHole(false)
                    }
                    setDrawValues(false)
                    mode = LineDataSet.Mode.LINEAR
                }
                builtDataSets.add(dataSet to perEntryLabels)
            }

            // Reverse so the newest dataset is drawn LAST (on top).
            builtDataSets.reverse()

            val labelMap = mutableMapOf<Pair<Int, Int>, String>()
            builtDataSets.forEachIndexed { dsIndex, (_, perEntryLabels) ->
                perEntryLabels.forEachIndexed { xIndex, text ->
                    labelMap[dsIndex to xIndex] = text
                }
            }
            val dataSets = builtDataSets.map { it.first }.toMutableList()

            if (seriesTrend != null) {
                val trendEntries = listOf(
                    Entry(seriesTrend.fromX, seriesTrend.fromY),
                    Entry(seriesTrend.toX, seriesTrend.toY)
                )
                val trendDataSet = LineDataSet(trendEntries, "Trend").apply {
                    color = TREND_COLOR_ARGB
                    lineWidth = 2f
                    setDrawCircles(false)
                    setDrawValues(false)
                    isHighlightEnabled = false
                    mode = LineDataSet.Mode.LINEAR
                }
                dataSets.add(trendDataSet)
            }

            if (dataSets.isNotEmpty()) {
                chart.data = LineData(dataSets.toList())
                val maxSeries = competitions.maxOf { it.seriesPoints.size }
                chart.xAxis.labelCount = minOf(maxSeries, 10)
            } else {
                chart.data = null
            }

            chart.axisLeft.removeAllLimitLines()
            if (seriesAverage != null) {
                val avgLine = LimitLine(seriesAverage).apply {
                    lineColor = AVERAGE_COLOR_ARGB
                    lineWidth = 2f
                }
                chart.axisLeft.addLimitLine(avgLine)
            }

            chart.marker = SeriesPointsMarkerView(chart.context, labelMap)

            chart.invalidate()
        }
    )
}
