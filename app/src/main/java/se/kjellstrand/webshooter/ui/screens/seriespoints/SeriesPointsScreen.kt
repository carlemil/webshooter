package se.kjellstrand.webshooter.ui.screens.seriespoints

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.seriespoints.CompetitionSeries
import se.kjellstrand.webshooter.ui.common.ShooterPickerDialog
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
            when {
                uiState.isLoading && uiState.competitions.isEmpty() -> {
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
                    SeriesPointsContent(uiState, viewModel)
                }
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
            showSelectedSection = false
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

        WeaponClassGroupFilter(
            availableGroups = uiState.availableGroups,
            selectedGroup = uiState.selectedGroup,
            onSelectGroup = viewModel::selectWeaponGroup,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        val competitions = uiState.filteredCompetitions

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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            CompetitionsLegend(
                competitions = competitions,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = screenHeight / 3)
                    .padding(horizontal = 16.dp)
            )
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
            Text(stringResource(R.string.series_points_change_shooter))
        }
    }
}

@Composable
private fun CompetitionsLegend(
    competitions: List<CompetitionSeries>,
    modifier: Modifier = Modifier
) {
    // Competitions are sorted oldest→newest; the legend shows newest first.
    val ordered = competitions.asReversed()
    val total = ordered.size
    LazyColumn(modifier = modifier) {
        items(ordered) { comp ->
            val indexFromNewest = ordered.indexOf(comp)
            val swatchColor = Color(colorForAge(indexFromNewest, total))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(swatchColor, RoundedCornerShape(2.dp))
                )
                Text(
                    text = "${comp.date} ${comp.competitionName}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun SeriesPointsChart(
    competitions: List<CompetitionSeries>,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setNoDataText("")
                setPinchZoom(true)
                isDragEnabled = true
                setScaleEnabled(true)
                isDoubleTapToZoomEnabled = true

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
                    false
                }

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.textColor = onSurfaceColor

                axisLeft.textColor = onSurfaceColor
                axisRight.isEnabled = false

                legend.isEnabled = false

                setExtraBottomOffset(16f)
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
            }
            if (chart.tag == signature) return@AndroidView
            chart.tag = signature

            // Competitions are sorted by date ascending, so the last entry is
            // the newest. Iterate from newest→oldest so the newest renders on top.
            val ordered = competitions.reversed()
            val total = ordered.size
            val dataSets = mutableListOf<LineDataSet>()

            ordered.forEachIndexed { indexFromNewest, comp ->
                if (comp.seriesPoints.isEmpty()) return@forEachIndexed
                val entries = comp.seriesPoints.mapIndexed { i, points ->
                    Entry((i + 1).toFloat(), points.toFloat())
                }
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
                dataSets.add(dataSet)
            }

            // Reverse so the newest dataset is drawn LAST (on top).
            dataSets.reverse()

            if (dataSets.isNotEmpty()) {
                chart.data = LineData(dataSets.toList())
                val maxSeries = competitions.maxOf { it.seriesPoints.size }
                chart.xAxis.labelCount = minOf(maxSeries, 10)
            } else {
                chart.data = null
            }

            chart.invalidate()
        }
    )
}
