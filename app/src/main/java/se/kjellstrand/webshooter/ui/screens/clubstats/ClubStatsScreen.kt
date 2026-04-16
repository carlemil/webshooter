package se.kjellstrand.webshooter.ui.screens.clubstats

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.widget.TextView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.clubstats.ShooterStats
import se.kjellstrand.webshooter.ui.common.CHART_COLORS
import se.kjellstrand.webshooter.ui.common.CHART_SHAPES

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
        when {
            uiState.isLoading && uiState.shooterStats.isEmpty() -> {
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

            uiState.shooterStats.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.charts_no_data),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                ClubStatsScatterChart(
                    shooterStats = uiState.shooterStats,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                ShooterLegend(
                    shooterStats = uiState.shooterStats,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShooterLegend(
    shooterStats: List<ShooterStats>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        shooterStats.forEachIndexed { index, stats ->
            val color = Color(CHART_COLORS[index % CHART_COLORS.size])
            val shapeIndex = index % CHART_SHAPES.size
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawScatterShape(shapeIndex, color)
                }
                Text(
                    text = stats.fullname,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun DrawScope.drawScatterShape(shapeIndex: Int, color: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension / 2f * 0.8f
    val strokeWidth = 2.dp.toPx()

    when (shapeIndex) {
        0 -> { // CIRCLE
            drawCircle(color = color, radius = r, center = Offset(cx, cy))
        }
        1 -> { // SQUARE
            drawRect(
                color = color,
                topLeft = Offset(cx - r, cy - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
            )
        }
        2 -> { // TRIANGLE
            val path = Path().apply {
                moveTo(cx, cy - r)
                lineTo(cx + r, cy + r)
                lineTo(cx - r, cy + r)
                close()
            }
            drawPath(path, color)
        }
        3 -> { // CROSS (+)
            drawLine(color, Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth)
            drawLine(color, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth)
        }
        4 -> { // X
            drawLine(color, Offset(cx - r, cy - r), Offset(cx + r, cy + r), strokeWidth)
            drawLine(color, Offset(cx + r, cy - r), Offset(cx - r, cy + r), strokeWidth)
        }
        5 -> { // CHEVRON_DOWN
            val path = Path().apply {
                moveTo(cx - r, cy - r * 0.5f)
                lineTo(cx, cy + r * 0.5f)
                lineTo(cx + r, cy - r * 0.5f)
            }
            drawPath(path, color, style = Stroke(strokeWidth))
        }
        6 -> { // CHEVRON_UP
            val path = Path().apply {
                moveTo(cx - r, cy + r * 0.5f)
                lineTo(cx, cy - r * 0.5f)
                lineTo(cx + r, cy + r * 0.5f)
            }
            drawPath(path, color, style = Stroke(strokeWidth))
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
            textView.text = shooterStats[index].fullname
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
                axisLeft.granularity = 1f
                axisRight.isEnabled = false

                legend.isEnabled = false

                setExtraBottomOffset(16f)
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
                    setScatterShape(CHART_SHAPES[index % CHART_SHAPES.size])
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
