package se.kjellstrand.webshooter.ui.screens.charts

import android.annotation.SuppressLint
import android.view.MotionEvent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.charts.ChartDataPoint
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.ui.common.ShooterPickerDialog
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize
import android.graphics.Color as AndroidColor

private val CHART_COLORS = listOf(
    AndroidColor.rgb(76, 175, 80),   // Green (user)
    AndroidColor.rgb(233, 30, 99),   // Pink
    AndroidColor.rgb(33, 150, 243),  // Blue
    AndroidColor.rgb(255, 152, 0),   // Orange
    AndroidColor.rgb(156, 39, 176),  // Purple
    AndroidColor.rgb(121, 85, 72),   // Brown
    AndroidColor.rgb(0, 188, 212),   // Cyan
    AndroidColor.rgb(255, 235, 59),  // Yellow
    AndroidColor.rgb(244, 67, 54),   // Red
    AndroidColor.rgb(0, 77, 64),     // Teal dark
    AndroidColor.rgb(170, 102, 204), // Lavender
    AndroidColor.rgb(255, 111, 0),   // Amber dark
    AndroidColor.rgb(21, 101, 192),  // Blue dark
    AndroidColor.rgb(130, 119, 23),  // Olive
    AndroidColor.rgb(198, 40, 40),   // Crimson
    AndroidColor.rgb(0, 137, 123),   // Teal
)

private val CHART_SHAPES = listOf(
    ScatterChart.ScatterShape.CIRCLE,
    ScatterChart.ScatterShape.SQUARE,
    ScatterChart.ScatterShape.TRIANGLE,
    ScatterChart.ScatterShape.CROSS,
    ScatterChart.ScatterShape.X,
    ScatterChart.ScatterShape.CHEVRON_DOWN,
    ScatterChart.ScatterShape.CHEVRON_UP
)

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
            when {
                uiState.isLoading && uiState.chartData.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.hasError -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.competitions_load_error),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    ChartsContent(uiState, viewModel)
                }
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
            onDismiss = { viewModel.setShowSearchDialog(false) }
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            )
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

        OutlinedButton(
            onClick = { viewModel.setShowSearchDialog(true) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(R.string.charts_add_shooter))
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
fun ChartScatterChart(
    myData: List<ChartDataPoint>,
    comparedShooters: Map<Long, ShooterChartInfo>,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            ScatterChart(context).apply {
                description.isEnabled = false
                setNoDataText("")
                setPinchZoom(true)
                isDragEnabled = true
                setScaleEnabled(true)
                isDoubleTapToZoomEnabled = true

                // Prevent the Compose host from intercepting multi-touch / drag
                // gestures before MPAndroidChart sees them.
                setOnTouchListener { v, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_POINTER_DOWN,
                        MotionEvent.ACTION_MOVE ->
                            v.parent?.requestDisallowInterceptTouchEvent(true)

                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL ->
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    // Return false so the chart's own gesture handling still runs.
                    false
                }

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.textColor = onSurfaceColor
                xAxis.labelRotationAngle = -45f

                axisLeft.textColor = onSurfaceColor
                axisRight.isEnabled = false

                legend.textColor = onSurfaceColor
                legend.isWordWrapEnabled = true

                setExtraBottomOffset(16f)
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
            }
            if (chart.tag == signature) {
                return@AndroidView
            }
            chart.tag = signature

            val dataSets = mutableListOf<ScatterDataSet>()
            val scatterShapeSizeDp = 24.dp.value

            // My data
            if (myData.isNotEmpty()) {
                val entries = myData.sortedBy { it.date }.map { dp ->
                    Entry(dateIndexMap[dp.date] ?: 0f, dp.averageSerieScore.toFloat())
                }
                val myDataSet = ScatterDataSet(
                    entries,
                    chart.context.getString(R.string.charts_my_results)
                ).apply {
                    color = CHART_COLORS[0]
                    setScatterShape(CHART_SHAPES[0])
                    scatterShapeSize = scatterShapeSizeDp
                    setDrawValues(false)
                }
                dataSets.add(myDataSet)
            }

            // Compared shooters
            comparedShooters.entries.forEachIndexed { index, (_, info) ->
                if (info.chartData.isNotEmpty()) {
                    val colorIndex = (index + 1) % CHART_COLORS.size
                    val shapeIndex = (index + 1) % CHART_SHAPES.size
                    val entries = info.chartData.sortedBy { it.date }.map { dp ->
                        Entry(dateIndexMap[dp.date] ?: 0f, dp.averageSerieScore.toFloat())
                    }
                    val dataSet = ScatterDataSet(entries, info.name).apply {
                        color = CHART_COLORS[colorIndex]
                        setScatterShape(CHART_SHAPES[shapeIndex])
                        scatterShapeSize = scatterShapeSizeDp
                        setDrawValues(false)
                    }
                    dataSets.add(dataSet)
                }
            }

            if (dataSets.isNotEmpty()) {
                chart.data = ScatterData(dataSets.toList())
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(sortedDates)
                chart.xAxis.labelCount = minOf(sortedDates.size, 6)
            } else {
                chart.data = null
            }

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
