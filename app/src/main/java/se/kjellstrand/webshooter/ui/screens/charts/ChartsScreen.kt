package se.kjellstrand.webshooter.ui.screens.charts

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.clickable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.charts.ChartDataPoint
import se.kjellstrand.webshooter.data.club.remote.ClubMember
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize

private val CHART_COLORS = listOf(
    AndroidColor.rgb(76, 175, 80),   // Green (user)
    AndroidColor.rgb(33, 150, 243),  // Blue
    AndroidColor.rgb(255, 152, 0),   // Orange
    AndroidColor.rgb(156, 39, 176),  // Purple
    AndroidColor.rgb(244, 67, 54),   // Red
    AndroidColor.rgb(0, 188, 212),   // Cyan
    AndroidColor.rgb(121, 85, 72),   // Brown
    AndroidColor.rgb(255, 235, 59),  // Yellow
)

@Composable
fun ChartsScreen(viewModel: ChartsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
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

    if (uiState.showSearchDialog) {
        AddShooterDialog(
            uiState = uiState,
            onSearchQueryChanged = viewModel::setSearchQuery,
            onAddShooter = viewModel::addShooter,
            onRemoveShooter = viewModel::removeShooter,
            onDismiss = { viewModel.setShowSearchDialog(false) }
        )
    }
}

@Composable
private fun ChartsContent(uiState: ChartsUiState, viewModel: ChartsViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.availableResultsTypes.isNotEmpty()) {
            val selectedIndex = uiState.availableResultsTypes.indexOf(uiState.selectedResultsType)
                .coerceAtLeast(0)

            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 8.dp
            ) {
                uiState.availableResultsTypes.forEach { type ->
                    Tab(
                        selected = type == uiState.selectedResultsType,
                        onClick = { viewModel.selectTab(type) },
                        text = { Text(formatResultsType(type)) }
                    )
                }
            }
        }

        if (uiState.availableWeaponClasses.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                uiState.availableWeaponClasses.forEach { weaponClass ->
                    WeaponClassBadge(
                        modifier = Modifier.clickable { viewModel.toggleWeaponClass(weaponClass) },
                        weaponGroupName = weaponClass,
                        isHighlighted = weaponClass in uiState.selectedWeaponClasses,
                        size = WeaponClassBadgeSize.Medium
                    )
                }
            }
        }

        val chartData = uiState.filteredChartData
        val comparedShooters = uiState.filteredComparedShooters

        if (chartData.isEmpty() && comparedShooters.values.all { it.chartData.isEmpty() }) {
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
            ChartLineChart(
                myData = chartData,
                comparedShooters = comparedShooters,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }

        OutlinedButton(
            onClick = { viewModel.setShowSearchDialog(true) },
            modifier = Modifier
                .fillMaxWidth()
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

@Composable
fun ChartLineChart(
    myData: List<ChartDataPoint>,
    comparedShooters: Map<Long, ShooterChartInfo>,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setPinchZoom(true)
                isDragEnabled = true
                setScaleEnabled(true)
                isDoubleTapToZoomEnabled = true

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.textColor = onSurfaceColor
                xAxis.labelRotationAngle = -45f

                axisLeft.textColor = onSurfaceColor
                axisRight.isEnabled = false

                legend.textColor = onSurfaceColor
                legend.isWordWrapEnabled = true
            }
        },
        update = { chart ->
            val dataSets = mutableListOf<LineDataSet>()
            val allDates = mutableListOf<String>()

            // Collect all dates for X axis
            val allDataPoints = myData + comparedShooters.values.flatMap { it.chartData }
            val sortedDates = allDataPoints.map { it.date }.distinct().sorted()
            allDates.addAll(sortedDates)
            val dateIndexMap = sortedDates.withIndex().associate { (i, d) -> d to i.toFloat() }

            // My data
            if (myData.isNotEmpty()) {
                val entries = myData.sortedBy { it.date }.map { dp ->
                    Entry(dateIndexMap[dp.date] ?: 0f, dp.averageSerieScore.toFloat())
                }
                val myDataSet = LineDataSet(entries, chart.context.getString(R.string.charts_my_results)).apply {
                    color = CHART_COLORS[0]
                    setCircleColor(CHART_COLORS[0])
                    lineWidth = 2f
                    circleRadius = 4f
                    setDrawValues(false)
                }
                dataSets.add(myDataSet)
            }

            // Compared shooters
            comparedShooters.entries.forEachIndexed { index, (_, info) ->
                if (info.chartData.isNotEmpty()) {
                    val colorIndex = (index + 1) % CHART_COLORS.size
                    val entries = info.chartData.sortedBy { it.date }.map { dp ->
                        Entry(dateIndexMap[dp.date] ?: 0f, dp.averageSerieScore.toFloat())
                    }
                    val dataSet = LineDataSet(entries, info.name).apply {
                        color = CHART_COLORS[colorIndex]
                        setCircleColor(CHART_COLORS[colorIndex])
                        lineWidth = 2f
                        circleRadius = 3f
                        setDrawValues(false)
                    }
                    dataSets.add(dataSet)
                }
            }

            if (dataSets.isNotEmpty()) {
                chart.data = LineData(dataSets.toList())
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(sortedDates)
                chart.xAxis.labelCount = minOf(sortedDates.size, 6)
            } else {
                chart.data = null
            }

            chart.invalidate()
        }
    )
}

@Composable
fun AddShooterDialog(
    uiState: ChartsUiState,
    onSearchQueryChanged: (String) -> Unit,
    onAddShooter: (Long) -> Unit,
    onRemoveShooter: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.charts_add_shooter)) },
        text = {
            Column {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    label = { Text(stringResource(R.string.charts_search_shooter)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show added shooters with remove button
                if (uiState.comparedShooters.isNotEmpty()) {
                    Text(
                        text = "${uiState.comparedShooters.size} valda",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    uiState.comparedShooters.forEach { (userId, info) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = info.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(onClick = { onRemoveShooter(userId) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.charts_remove)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Search results
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    val filtered = uiState.filteredClubMembers.filter {
                        !uiState.comparedShooters.containsKey(it.userId)
                    }
                    items(filtered) { member ->
                        Text(
                            text = member.fullname ?: "${member.name} ${member.lastname ?: ""}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddShooter(member.userId) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.charts_close))
            }
        }
    )
}

private fun formatResultsType(type: String): String {
    return when (type) {
        "precision" -> "Precision"
        "military" -> "Militär"
        "field" -> "Fält"
        "pointfield" -> "Poängfält"
        else -> type.replaceFirstChar { it.uppercase() }
    }
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
