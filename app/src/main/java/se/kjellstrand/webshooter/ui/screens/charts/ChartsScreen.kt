package se.kjellstrand.webshooter.ui.screens.charts

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.charts.ChartDataPoint
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import androidx.compose.ui.graphics.Color
import se.kjellstrand.webshooter.ui.common.AddShooterButton
import se.kjellstrand.webshooter.ui.common.CHART_COLORS
import se.kjellstrand.webshooter.ui.common.CHART_SHAPE_RENDERERS
import se.kjellstrand.webshooter.ui.common.ChartStateWrapper
import se.kjellstrand.webshooter.ui.common.ShooterPickerDialog
import se.kjellstrand.webshooter.ui.common.UserLegend
import se.kjellstrand.webshooter.ui.common.UserLegendItem
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize
import se.kjellstrand.webshooter.ui.common.applyBaseChartStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(viewModel: ChartsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var isFilterBottomSheetOpen by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (uiState.availableWeaponClasses.isNotEmpty()) {
                FloatingActionButton(onClick = { isFilterBottomSheetOpen = true }) {
                    Icon(
                        painter = painterResource(R.drawable.filter_list),
                        contentDescription = "Open Filters"
                    )
                }
            }
        }
    ) { _ ->
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

    if (isFilterBottomSheetOpen) {
        ChartsFilterBottomSheet(
            availableWeaponClasses = uiState.availableWeaponClasses,
            selectedWeaponClasses = uiState.selectedWeaponClasses,
            onToggleWeaponClass = viewModel::toggleWeaponClass,
            onDismissRequest = { isFilterBottomSheetOpen = false }
        )
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChartsFilterBottomSheet(
    availableWeaponClasses: List<String>,
    selectedWeaponClasses: Set<String>,
    onToggleWeaponClass: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.competitions_filter_competition_type),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                availableWeaponClasses.forEach { weaponClass ->
                    WeaponClassBadge(
                        modifier = Modifier.clickable { onToggleWeaponClass(weaponClass) },
                        weaponGroupName = weaponClass,
                        isHighlighted = weaponClass in selectedWeaponClasses,
                        size = WeaponClassBadgeSize.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onDismissRequest) {
                    Text(stringResource(R.string.results_done_button))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChartsContent(uiState: ChartsUiState, viewModel: ChartsViewModel) {
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
                        text = {
                            Text(
                                ResultsType.fromApiString(type)?.displayName
                                    ?: type.replaceFirstChar { it.uppercase() })
                        }
                    )
                }
            }
        }

        val chartData = uiState.filteredChartData
        val comparedShooters = uiState.filteredComparedShooters
        val hasAnyData = chartData.isNotEmpty() ||
                comparedShooters.values.any { it.chartData.isNotEmpty() }

        // Render the chart frame as soon as metadata (tabs) exists, even if no
        // datapoints have streamed in yet — they pop in progressively.
        if (uiState.availableResultsTypes.isNotEmpty()) {
            ChartScatterChart(
                myData = chartData,
                comparedShooters = comparedShooters,
                myAverage = uiState.myAverage,
                myTrend = uiState.myTrend,
                modifier = Modifier
                    .weight(1f)
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
            }
            if (legendItems.isNotEmpty()) {
                UserLegend(
                    items = legendItems,
                    modifier = Modifier
                        .fillMaxWidth()
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

private const val TREND_COLOR_ARGB: Int = 0xFF00FFFF.toInt()
private const val AVERAGE_COLOR_ARGB: Int = 0xFFFF00FF.toInt()
private const val TREND_SAMPLES: Int = 30

@SuppressLint("ClickableViewAccessibility")
@Composable
fun ChartScatterChart(
    myData: List<ChartDataPoint>,
    comparedShooters: Map<Long, ShooterChartInfo>,
    myAverage: Float?,
    myTrend: ChartsUiState.TrendLine?,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val myResultsLabel = stringResource(R.string.charts_my_results)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            ScatterChart(context).apply {
                applyBaseChartStyle(onSurfaceColor)
                xAxis.labelRotationAngle = -45f
                legend.isEnabled = false
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
            }
            if (chart.tag == signature) {
                return@AndroidView
            }
            chart.tag = signature

            val dataSets = mutableListOf<ScatterDataSet>()
            val scatterShapeSizeDp = 24.dp.value
            val labelMap = mutableMapOf<Int, String>()
            var tagCounter = 0

            // My data
            if (myData.isNotEmpty()) {
                val entries = myData.sortedBy { it.date }.map { dp ->
                    val tag = tagCounter++
                    labelMap[tag] = "$myResultsLabel\n${dp.date}: ${dp.averageSerieScore} p"
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
                        labelMap[tag] = "${info.name}\n${dp.date}: ${dp.averageSerieScore} p"
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

            // Trend line: sample points along the regression line, using the chart's
            // date-indexed X space.
            val sortedMy = myData.sortedBy { it.date }
            if (myTrend != null && sortedMy.size >= 2) {
                val firstChartX = dateIndexMap[sortedMy.first().date] ?: 0f
                val lastChartX = dateIndexMap[sortedMy.last().date] ?: 0f
                val trendEntries = (0..TREND_SAMPLES).map { i ->
                    val t = i.toFloat() / TREND_SAMPLES.toFloat()
                    val x = firstChartX + (lastChartX - firstChartX) * t
                    val y = myTrend.fromY + (myTrend.toY - myTrend.fromY) * t
                    Entry(x, y)
                }
                val trendDataSet = ScatterDataSet(trendEntries, "Trend").apply {
                    color = TREND_COLOR_ARGB
                    shapeRenderer = CHART_SHAPE_RENDERERS[0]
                    scatterShapeSize = 6.dp.value
                    setDrawValues(false)
                    isHighlightEnabled = false
                }
                dataSets.add(trendDataSet)
            }

            if (dataSets.isNotEmpty()) {
                chart.data = ScatterData(dataSets.toList())
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
                    enableDashedLine(8f, 4f, 0f)
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
    val mock = se.kjellstrand.webshooter.ui.mock.MockCharts()
    ChartsScreen(
        viewModel = se.kjellstrand.webshooter.ui.mock.ChartsViewModelMock(
            ChartsUiState(
                chartData = mock.allChartData,
                availableResultsTypes = mock.availableResultsTypes,
                availableWeaponClasses = mock.availableWeaponClasses,
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
        viewModel = se.kjellstrand.webshooter.ui.mock.ChartsViewModelMock(
            ChartsUiState(isLoading = true)
        )
    )
}

@Preview(showBackground = true, name = "Charts - Error")
@Composable
fun ChartsScreenErrorPreview() {
    ChartsScreen(
        viewModel = se.kjellstrand.webshooter.ui.mock.ChartsViewModelMock(
            ChartsUiState(hasError = true)
        )
    )
}

@Preview(showBackground = true, name = "Charts - Empty")
@Composable
fun ChartsScreenEmptyPreview() {
    val mock = se.kjellstrand.webshooter.ui.mock.MockCharts()
    ChartsScreen(
        viewModel = se.kjellstrand.webshooter.ui.mock.ChartsViewModelMock(
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
    val mock = se.kjellstrand.webshooter.ui.mock.MockCharts()
    ChartsScreen(
        viewModel = se.kjellstrand.webshooter.ui.mock.ChartsViewModelMock(
            ChartsUiState(
                chartData = mock.allChartData,
                comparedShooters = mock.comparedShooters,
                availableResultsTypes = mock.availableResultsTypes,
                availableWeaponClasses = mock.availableWeaponClasses,
                selectedResultsType = "precision",
                clubMembers = mock.clubMembers
            )
        )
    )
}
