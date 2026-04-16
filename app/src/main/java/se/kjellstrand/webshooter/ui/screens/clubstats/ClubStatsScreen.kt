package se.kjellstrand.webshooter.ui.screens.clubstats

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import android.graphics.Color as AndroidColor

@Composable
fun ClubStatsScreen(viewModel: ClubStatsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
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
            }
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

            val entries = shooterStats.mapIndexed { index, stats ->
                Entry(
                    stats.averagePoints.toFloat(),
                    stats.competitionCount.toFloat()
                ).apply {
                    data = index
                }
            }

            val dataSet = ScatterDataSet(entries, "Club Members").apply {
                color = AndroidColor.rgb(76, 175, 80)
                setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                scatterShapeSize = 24.dp.value
                setDrawValues(false)
            }

            chart.data = ScatterData(dataSet)

            chart.xAxis.axisMinimum = 0f
            axisLabel(chart, onSurfaceColor)

            chart.marker = ShooterMarkerView(chart.context, shooterStats)

            chart.invalidate()
        }
    )
}

private fun axisLabel(chart: ScatterChart, textColor: Int) {
    chart.xAxis.textColor = textColor
    chart.axisLeft.textColor = textColor
}
