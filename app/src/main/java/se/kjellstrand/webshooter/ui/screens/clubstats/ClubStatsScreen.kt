package se.kjellstrand.webshooter.ui.screens.clubstats

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import se.kjellstrand.webshooter.ui.common.CHART_SHAPE_RENDERERS
import se.kjellstrand.webshooter.ui.common.ChartStateWrapper
import se.kjellstrand.webshooter.ui.common.UserLegend
import se.kjellstrand.webshooter.ui.common.UserLegendItem
import se.kjellstrand.webshooter.ui.common.applyBaseChartStyle

@Composable
fun ClubStatsScreen(viewModel: ClubStatsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.year != 0) {
            Text(
                text = stringResource(R.string.web_shooter_club_stats) + " ${uiState.year}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
            )
            Text(
                text = stringResource(R.string.club_stats_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
            )
        }
        ChartStateWrapper(
            isLoading = uiState.isLoading && uiState.shooterStats.isEmpty(),
            hasError = uiState.hasError,
            isEmpty = uiState.shooterStats.isEmpty()
        ) {
            ClubStatsScatterChart(
                shooterStats = uiState.shooterStats,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            val legendItems = uiState.shooterStats.mapIndexed { index, stats ->
                UserLegendItem(
                    label = stats.fullname,
                    color = Color(CHART_COLORS[index % CHART_COLORS.size]),
                    shapeIndex = index % CHART_SHAPE_RENDERERS.size
                )
            }
            UserLegend(
                items = legendItems,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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

@SuppressLint("ClickableViewAccessibility")
@Composable
fun ClubStatsScatterChart(
    shooterStats: List<ShooterStats>,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

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
            }
            if (chart.tag == signature) {
                return@AndroidView
            }
            chart.tag = signature

            val scatterShapeSizeDp = 24.dp.value
            val dataSets = shooterStats.mapIndexed { index, stats ->
                val entry = Entry(
                    stats.competitionCount.toFloat(),
                    stats.averagePoints.toFloat()
                ).apply {
                    data = index
                }
                ScatterDataSet(listOf(entry), stats.fullname).apply {
                    color = CHART_COLORS[index % CHART_COLORS.size]
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
