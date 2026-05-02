package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.charts.ChartDataPoint
import androidx.compose.ui.graphics.Color
import se.kjellstrand.webshooter.ui.common.AddShooterButton
import se.kjellstrand.webshooter.ui.common.CHART_COLORS
import se.kjellstrand.webshooter.ui.common.CHART_MIN_HEIGHT_FRACTION
import se.kjellstrand.webshooter.ui.common.CHART_SHAPE_RENDERERS
import se.kjellstrand.webshooter.ui.common.ChartStateWrapper
import se.kjellstrand.webshooter.ui.common.ShooterPickerDialog
import se.kjellstrand.webshooter.ui.common.ChartLegend
import se.kjellstrand.webshooter.ui.common.UserLegendItem
import se.kjellstrand.webshooter.ui.common.WeaponClassGroupFilter
import se.kjellstrand.webshooter.ui.common.applyBaseChartStyle
import se.kjellstrand.webshooter.ui.mock.ChartsViewModelMock
import se.kjellstrand.webshooter.ui.mock.MockCharts
import kotlin.collections.get

@Composable
fun ChartsScreen(viewModel: ResultsTrendsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh()
        }
    }

    Scaffold { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.charts_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
            )
            ChartStateWrapper(
                isLoading = uiState.isLoading && uiState.chartData.isEmpty(),
                hasError = uiState.hasError
            ) {
                ChartsContent(uiState, viewModel)
            }
        }
    }

    if (uiState.showSearchDialog) {
        ShooterPickerDialog(
            title = stringResource(R.string.charts_add_shooter),
            searchQuery = uiState.searchQuery,
            clubMembers = uiState.clubMembers,
            allParticipants = uiState.allParticipants,
            selectedShooters = uiState.comparedShooters.mapValues { it.value.name },
            onSearchQueryChanged = viewModel::setSearchQuery,
            onPickShooter = viewModel::addShooter,
            onRemoveShooter = viewModel::removeShooter,
            onDismiss = { viewModel.setShowSearchDialog(false) },
            relevantUserIds = uiState.relevantUserIds
        )
    }
}

@Composable
private fun ChartsContent(uiState: ChartsUiState, viewModel: ResultsTrendsViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.availableResultsTypes.isNotEmpty()) {
            val selectedIndex = uiState.availableResultsTypes.indexOf(uiState.selectedResultsType)
                .coerceAtLeast(0)

            TabRow(
                selectedTabIndex = selectedIndex
            ) {
                uiState.availableResultsTypes.forEach { type ->
                    Tab(
                        selected = type == uiState.selectedResultsType,
                        onClick = { viewModel.selectTab(type) },
                        text = { Text(trendsTabDisplayName(type)) }
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

        val chartData = uiState.filteredChartData
        val comparedShooters = uiState.filteredComparedShooters
        val hasAnyData = chartData.isNotEmpty() ||
                comparedShooters.values.any { it.chartData.isNotEmpty() }
        val isHitsBased = uiState.selectedResultsType == "field" ||
                uiState.selectedResultsType == "pointfield"

        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        // Render the chart frame as soon as metadata (tabs) exists, even if no
        // datapoints have streamed in yet — they pop in progressively.
        if (uiState.availableResultsTypes.isNotEmpty()) {
            ChartScatterChart(
                myData = chartData,
                comparedShooters = comparedShooters,
                myAverage = uiState.myAverage,
                myTrend = uiState.myTrend,
                isHitsBased = isHitsBased,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = screenHeight * CHART_MIN_HEIGHT_FRACTION)
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            val legendItems = buildList {
                if (chartData.isNotEmpty()) {
                    add(
                        UserLegendItem(
                            label = stringResource(R.string.charts_my_results),
                            color = Color(CHART_COLORS[0]),
                            shapeIndex = 0
                        )
                    )
                }
                comparedShooters.entries.forEachIndexed { index, (_, info) ->
                    if (info.chartData.isNotEmpty()) {
                        val colorIndex = (index + 1) % CHART_COLORS.size
                        val shapeIndex = (index + 1) % CHART_SHAPE_RENDERERS.size
                        add(
                            UserLegendItem(
                                label = info.name,
                                color = Color(CHART_COLORS[colorIndex]),
                                shapeIndex = shapeIndex
                            )
                        )
                    }
                }
                if (uiState.myAverage != null) {
                    add(
                        UserLegendItem(
                            label = stringResource(R.string.charts_legend_average),
                            color = Color(AVERAGE_COLOR_ARGB),
                            shapeIndex = 7
                        )
                    )
                }
                if (uiState.myTrend != null && !isHitsBased) {
                    add(
                        UserLegendItem(
                            label = stringResource(R.string.charts_legend_trend),
                            color = Color(TREND_COLOR_ARGB),
                            shapeIndex = 8
                        )
                    )
                }
            }
            if (legendItems.isNotEmpty()) {
                ChartLegend(
                    items = legendItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = screenHeight * (1f - CHART_MIN_HEIGHT_FRACTION))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else if (!uiState.isLoading && !hasAnyData) {
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
        }

        AddShooterButton(
            text = stringResource(R.string.charts_add_shooter),
            onClick = { viewModel.setShowSearchDialog(true) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private class ChartsMarkerView(
    context: Context,
    private val labels: Map<Int, String>
) : MarkerView(context, R.layout.marker_view) {

    private val textView: TextView = findViewById(R.id.marker_text)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        textView.text = labels[e?.data as? Int] ?: ""
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}

private val AVERAGE_COLOR_ARGB: Int = android.graphics.Color.rgb(127, 255, 0)
private val TREND_COLOR_ARGB: Int = android.graphics.Color.rgb(0, 255, 64)

@SuppressLint("ClickableViewAccessibility")
@Composable
fun ChartScatterChart(
    myData: List<ChartDataPoint>,
    comparedShooters: Map<Long, ShooterChartInfo>,
    myAverage: Float?,
    myTrend: ChartsUiState.TrendLine?,
    isHitsBased: Boolean,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val myResultsLabel = stringResource(R.string.charts_my_results)
    val scatterShapeSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { 24.dp.toPx() }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            CombinedChart(context).apply {
                applyBaseChartStyle(onSurfaceColor)
                xAxis.labelRotationAngle = -45f
                legend.isEnabled = false
                setDrawOrder(
                    arrayOf(
                        CombinedChart.DrawOrder.LINE,
                        CombinedChart.DrawOrder.SCATTER
                    )
                )
            }
        },
        update = { chart ->
            // Collect all dates for X axis
            val allDataPoints = myData + comparedShooters.values.flatMap { it.chartData }
            val sortedDates = allDataPoints.map { it.date }.distinct().sorted()
            val dateIndexMap = sortedDates.withIndex().associate { (i, d) -> d to i.toFloat() }

            // Build a signature of the inputs so we can skip rebuilding the
            // ScatterData (which resets the user's zoom/pan) when nothing changed.
            val signature = buildString {
                append(myData.size).append('|')
                myData.forEach {
                    append(it.date).append(':').append(it.averageSerieScore).append(',')
                }
                append('#')
                comparedShooters.forEach { (id, info) ->
                    append(id).append('=').append(info.name).append(':')
                    info.chartData.forEach {
                        append(it.date).append(':').append(it.averageSerieScore).append(',')
                    }
                    append(';')
                }
                append('@').append(myAverage ?: "")
                append('~')
                if (myTrend != null) {
                    append(myTrend.fromX).append(',').append(myTrend.fromY).append(';')
                        .append(myTrend.toX).append(',').append(myTrend.toY)
                }
                append('|').append(isHitsBased)
            }
            if (chart.tag == signature) {
                return@AndroidView
            }
            chart.tag = signature

            val dataSets = mutableListOf<ScatterDataSet>()
            val scatterShapeSizeDp = scatterShapeSizePx
            val labelMap = mutableMapOf<Int, String>()
            var tagCounter = 0

            // My data
            if (myData.isNotEmpty()) {
                val entries = myData.sortedBy { it.date }.map { dp ->
                    val tag = tagCounter++
                    labelMap[tag] = "$myResultsLabel\n${dp.date}: ${"%.1f".format(dp.averageSerieScore).removeSuffix(".0").removeSuffix(",0")} p"
                    Entry(dateIndexMap[dp.date] ?: 0f, dp.averageSerieScore.toFloat()).apply {
                        data = tag
                    }
                }
                val myDataSet = ScatterDataSet(entries, myResultsLabel).apply {
                    color = CHART_COLORS[0]
                    shapeRenderer = CHART_SHAPE_RENDERERS[0]
                    scatterShapeSize = scatterShapeSizeDp
                    setDrawValues(false)
                }
                dataSets.add(myDataSet)
            }

            // Compared shooters
            comparedShooters.entries.forEachIndexed { index, (_, info) ->
                if (info.chartData.isNotEmpty()) {
                    val colorIndex = (index + 1) % CHART_COLORS.size
                    val shapeIndex = (index + 1) % CHART_SHAPE_RENDERERS.size
                    val entries = info.chartData.sortedBy { it.date }.map { dp ->
                        val tag = tagCounter++
                        labelMap[tag] = "${info.name}\n${dp.date}: ${"%.1f".format(dp.averageSerieScore).removeSuffix(".0").removeSuffix(",0")} p"
                        Entry(dateIndexMap[dp.date] ?: 0f, dp.averageSerieScore.toFloat()).apply {
                            data = tag
                        }
                    }
                    val dataSet = ScatterDataSet(entries, info.name).apply {
                        color = CHART_COLORS[colorIndex]
                        shapeRenderer = CHART_SHAPE_RENDERERS[shapeIndex]
                        scatterShapeSize = scatterShapeSizeDp
                        setDrawValues(false)
                    }
                    dataSets.add(dataSet)
                }
            }

            // Trend line: two endpoints of the regression line, rendered as a
            // real LineDataSet inside a CombinedChart.
            val sortedMy = myData.sortedBy { it.date }
            val trendLineDataSet: LineDataSet? =
                if (!isHitsBased && myTrend != null && sortedMy.size >= 2) {
                    val firstChartX = dateIndexMap[sortedMy.first().date] ?: 0f
                    val lastChartX = dateIndexMap[sortedMy.last().date] ?: 0f
                    val trendEntries = listOf(
                        Entry(firstChartX, myTrend.fromY),
                        Entry(lastChartX, myTrend.toY)
                    )
                    LineDataSet(trendEntries, "Trend").apply {
                        color = TREND_COLOR_ARGB
                        lineWidth = 2f
                        setDrawCircles(false)
                        setDrawValues(false)
                        isHighlightEnabled = false
                    }
                } else null

            // Configure axis-max flag BEFORE assigning chart.data: calcMinMax()
            // fires inside that assignment and locks in the axis range using the
            // current mCustomAxisMax flag. Setting it afterwards leaves the fält
            // cap sticky on the next precision render.
            if (isHitsBased) {
                chart.axisLeft.axisMaximum = 6f
            } else {
                chart.axisLeft.resetAxisMaximum()
            }

            if (dataSets.isNotEmpty() || trendLineDataSet != null) {
                val combined = CombinedData().apply {
                    if (dataSets.isNotEmpty()) {
                        setData(ScatterData(dataSets.toList()))
                    }
                    if (trendLineDataSet != null) {
                        setData(LineData(trendLineDataSet))
                    }
                }
                chart.data = combined
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(sortedDates)
                chart.xAxis.labelCount = minOf(sortedDates.size, 6)
            } else {
                chart.data = null
            }

            // Average line: horizontal LimitLine on the left axis.
            chart.axisLeft.removeAllLimitLines()
            if (myAverage != null) {
                val avgLine = LimitLine(myAverage).apply {
                    lineColor = AVERAGE_COLOR_ARGB
                    lineWidth = 2f
                }
                chart.axisLeft.addLimitLine(avgLine)
            }

            chart.marker = ChartsMarkerView(chart.context, labelMap)

            chart.invalidate()
        }
    )
}

@Preview(showBackground = true, name = "Charts - Default with data")
@Composable
fun ChartsScreenDefaultPreview() {
    val mock = MockCharts()
    ChartsScreen(
        viewModel = ChartsViewModelMock(
            ChartsUiState(
                chartData = mock.allChartData,
                availableResultsTypes = mock.availableResultsTypes,
                selectedResultsType = "precision",
                clubMembers = mock.clubMembers
            )
        )
    )
}

@Preview(showBackground = true, name = "Charts - Loading")
@Composable
fun ChartsScreenLoadingPreview() {
    ChartsScreen(
        viewModel = ChartsViewModelMock(
            ChartsUiState(isLoading = true)
        )
    )
}

@Preview(showBackground = true, name = "Charts - Error")
@Composable
fun ChartsScreenErrorPreview() {
    ChartsScreen(
        viewModel = ChartsViewModelMock(
            ChartsUiState(hasError = true)
        )
    )
}

@Preview(showBackground = true, name = "Charts - Empty")
@Composable
fun ChartsScreenEmptyPreview() {
    val mock = MockCharts()
    ChartsScreen(
        viewModel = ChartsViewModelMock(
            ChartsUiState(
                chartData = emptyMap(),
                availableResultsTypes = mock.availableResultsTypes,
                selectedResultsType = "precision"
            )
        )
    )
}

@Preview(showBackground = true, name = "Charts - With compared shooters")
@Composable
fun ChartsScreenComparedPreview() {
    val mock = MockCharts()
    ChartsScreen(
        viewModel = ChartsViewModelMock(
            ChartsUiState(
                chartData = mock.allChartData,
                comparedShooters = mock.comparedShooters,
                availableResultsTypes = mock.availableResultsTypes,
                selectedResultsType = "precision",
                clubMembers = mock.clubMembers
            )
        )
    )
}
