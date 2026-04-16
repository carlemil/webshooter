package se.kjellstrand.webshooter.ui.common

import com.github.mikephil.charting.charts.ScatterChart
import android.graphics.Color as AndroidColor

val CHART_COLORS = listOf(
    AndroidColor.rgb(76, 175, 80),   // Green
    AndroidColor.rgb(33, 150, 243),  // Blue
    AndroidColor.rgb(255, 152, 0),   // Orange
    AndroidColor.rgb(156, 39, 176),  // Purple
    AndroidColor.rgb(121, 85, 72),   // Brown
    AndroidColor.rgb(0, 188, 212),   // Cyan
    AndroidColor.rgb(255, 235, 59),  // Yellow
    AndroidColor.rgb(255, 27, 24)    // Red
)

val CHART_SHAPES = listOf(
    ScatterChart.ScatterShape.CIRCLE,
    ScatterChart.ScatterShape.SQUARE,
    ScatterChart.ScatterShape.TRIANGLE,
    ScatterChart.ScatterShape.CROSS,
    ScatterChart.ScatterShape.X,
    ScatterChart.ScatterShape.CHEVRON_DOWN,
    ScatterChart.ScatterShape.CHEVRON_UP
)
