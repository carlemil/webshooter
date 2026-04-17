package se.kjellstrand.webshooter.ui.screens.seriespoints

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
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
import androidx.compose.material3.MaterialTheme
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
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.seriespoints.CompetitionSeries
import se.kjellstrand.webshooter.ui.common.AddShooterButton
import se.kjellstrand.webshooter.ui.common.ChartStateWrapper
import se.kjellstrand.webshooter.ui.common.ShooterPickerDialog
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
            relevantUserIds = uiState.precisionClubMembers.map { it.userId }.toSet()
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

        AddShooterButton(
            text = stringResource(R.string.series_points_change_shooter),
            onClick = { viewModel.setShowSearchDialog(true) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
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
                val perEntryLabels = comp.seriesPoints.mapIndexed { i, points ->
                    "${comp.date} ${comp.competitionName}\nSerie ${i + 1}: $points p"
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
            val dataSets = builtDataSets.map { it.first }

            if (dataSets.isNotEmpty()) {
                chart.data = LineData(dataSets.toList())
                val maxSeries = competitions.maxOf { it.seriesPoints.size }
                chart.xAxis.labelCount = minOf(maxSeries, 10)
            } else {
                chart.data = null
            }

            chart.marker = SeriesPointsMarkerView(chart.context, labelMap)

            chart.invalidate()
        }
    )
}
