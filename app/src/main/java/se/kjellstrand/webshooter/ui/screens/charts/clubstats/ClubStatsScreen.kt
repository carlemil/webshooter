package se.kjellstrand.webshooter.ui.screens.charts.clubstats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.clubstats.ShooterStats
import se.kjellstrand.webshooter.ui.common.CHART_COLORS
import se.kjellstrand.webshooter.ui.common.CHART_MIN_HEIGHT_FRACTION
import se.kjellstrand.webshooter.ui.common.CHART_SHAPE_COUNT
import se.kjellstrand.webshooter.ui.common.ChartLegend
import se.kjellstrand.webshooter.ui.common.ChartShape
import se.kjellstrand.webshooter.ui.common.ChartStateWrapper
import se.kjellstrand.webshooter.ui.common.UserLegendItem
import se.kjellstrand.webshooter.ui.common.WeaponClassGroupFilter
import se.kjellstrand.webshooter.ui.common.drawScatterShape
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun ClubStatsScreen(viewModel: ClubStatsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh()
        }
    }

    var highlightedLegendId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.year, uiState.selectedGroup) {
        highlightedLegendId = null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.web_shooter_club_stats),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
        )
        Text(
            text = stringResource(R.string.club_stats_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
        )

        if (uiState.availableYears.isNotEmpty()) {
            val tabYears: List<Int> = listOf(0) + uiState.availableYears
            val selectedIndex = tabYears.indexOf(uiState.year).coerceAtLeast(0)
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 0.dp
            ) {
                tabYears.forEach { year ->
                    Tab(
                        selected = year == uiState.year,
                        onClick = { viewModel.selectYear(year) },
                        text = {
                            Text(
                                if (year == 0) stringResource(R.string.club_stats_all_years)
                                else year.toString()
                            )
                        }
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
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        ChartStateWrapper(
            isLoading = uiState.isLoading && uiState.shooterStats.isEmpty(),
            hasError = uiState.hasError,
            isEmpty = uiState.shooterStats.isEmpty()
        ) {
            ClubStatsScatterChart(
                shooterStats = uiState.shooterStats,
                highlightedId = highlightedLegendId,
                onPointTap = { id ->
                    highlightedLegendId = if (highlightedLegendId == id) null else id
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = screenHeight * CHART_MIN_HEIGHT_FRACTION)
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            val legendItems = uiState.shooterStats.mapIndexed { index, stats ->
                UserLegendItem(
                    label = stats.fullname,
                    color = Color(CHART_COLORS[index % CHART_COLORS.size]),
                    shapeIndex = index % CHART_SHAPE_COUNT,
                    id = "shooter:${stats.userId}"
                )
            }
            ChartLegend(
                items = legendItems,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = screenHeight * (1f - CHART_MIN_HEIGHT_FRACTION))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                highlightedId = highlightedLegendId,
                onItemClick = { id ->
                    highlightedLegendId = if (highlightedLegendId == id) null else id
                }
            )
        }
    }
}

@Composable
fun ClubStatsScatterChart(
    shooterStats: List<ShooterStats>,
    highlightedId: String?,
    onPointTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (shooterStats.isEmpty()) {
        Box(modifier = modifier)
        return
    }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = onSurfaceVariant.copy(alpha = 0.18f)
    val labelStyle = MaterialTheme.typography.bodySmall.copy(color = onSurfaceVariant)
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val pointSizePx = with(density) { 18.dp.toPx() }
    val tapRadiusPx = with(density) { 24.dp.toPx() }

    val xValues = shooterStats.map { it.competitionCount.toFloat() }
    val yValues = shooterStats.map { it.averagePoints.toFloat() }
    val xMin = 0f
    val xMax = (xValues.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val yRaw0 = yValues.minOrNull() ?: 0f
    val yRaw1 = yValues.maxOrNull() ?: 1f
    val yPad = ((yRaw1 - yRaw0).coerceAtLeast(1f)) * 0.05f
    val yMin = floor(yRaw0 - yPad)
    val yMax = ceil(yRaw1 + yPad)

    val points = remember(shooterStats) {
        shooterStats.mapIndexed { index, stats ->
            ScatterPoint(
                id = "shooter:${stats.userId}",
                x = stats.competitionCount.toFloat(),
                y = stats.averagePoints.toFloat(),
                color = Color(CHART_COLORS[index % CHART_COLORS.size]),
                shape = ChartShape.forIndex(index),
                stats = stats,
            )
        }
    }

    var tappedPoint by remember(shooterStats, highlightedId) {
        mutableStateOf<ScatterPoint?>(highlightedId?.let { id -> points.firstOrNull { it.id == id } })
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points, xMin, xMax, yMin, yMax) {
                    detectTapGestures { tap ->
                        val plot = computePlotArea(size.width.toFloat(), size.height.toFloat(), density)
                        val nearest = findNearestPoint(
                            points = points,
                            tap = tap,
                            plot = plot,
                            xMin = xMin, xMax = xMax,
                            yMin = yMin, yMax = yMax,
                            maxDistancePx = tapRadiusPx,
                        )
                        if (nearest != null) {
                            tappedPoint = nearest
                            onPointTap(nearest.id)
                        }
                    }
                }
        ) {
            val plot = computePlotArea(size.width, size.height, density)
            drawAxes(
                plot = plot,
                xMin = xMin, xMax = xMax,
                yMin = yMin, yMax = yMax,
                axisColor = onSurface,
                gridColor = gridColor,
                labelStyle = labelStyle,
                textMeasurer = textMeasurer,
            )
            // Plot points; dim non-highlighted when something is highlighted.
            points.forEach { point ->
                val px = plot.left + (point.x - xMin) / (xMax - xMin) * plot.width
                val py = plot.bottom - (point.y - yMin) / (yMax - yMin) * plot.height
                val dim = highlightedId != null && highlightedId != point.id
                val effectiveColor = if (dim) point.color.copy(alpha = 0.25f) else point.color
                drawScatterShape(
                    shape = point.shape,
                    color = effectiveColor,
                    center = Offset(px, py),
                    size = pointSizePx,
                )
            }
        }

        // Marker overlay for the most recently tapped point.
        val markerPoint = tappedPoint
        if (markerPoint != null && (highlightedId == null || highlightedId == markerPoint.id)) {
            BoxWithMarkerPosition(
                point = markerPoint,
                xMin = xMin, xMax = xMax,
                yMin = yMin, yMax = yMax,
            )
        }
    }
}

@Composable
private fun BoxWithMarkerPosition(
    point: ScatterPoint,
    xMin: Float, xMax: Float,
    yMin: Float, yMax: Float,
) {
    val density = LocalDensity.current
    val avg = "%.1f".format(point.stats.averagePoints)
    val markerText = "${point.stats.fullname}\n$avg p · ${point.stats.competitionCount} tävlingar"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {} // swallow taps on marker
    ) {
        Box(
            modifier = Modifier
                .layoutOffsetForMarker(
                    point = point,
                    xMin = xMin, xMax = xMax,
                    yMin = yMin, yMax = yMax,
                    density = density,
                )
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = markerText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun Modifier.layoutOffsetForMarker(
    point: ScatterPoint,
    xMin: Float, xMax: Float,
    yMin: Float, yMax: Float,
    density: androidx.compose.ui.unit.Density,
): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    val plot = computePlotArea(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat(), density)
    val px = plot.left + (point.x - xMin) / (xMax - xMin) * plot.width
    val py = plot.bottom - (point.y - yMin) / (yMax - yMin) * plot.height
    val x = (px - placeable.width / 2f).toInt()
        .coerceIn(0, (constraints.maxWidth - placeable.width).coerceAtLeast(0))
    val y = (py - placeable.height - 8 * density.density).toInt().coerceAtLeast(0)
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place(IntOffset(x, y))
    }
}

private data class ScatterPoint(
    val id: String,
    val x: Float,
    val y: Float,
    val color: Color,
    val shape: ChartShape,
    val stats: ShooterStats,
)

private data class PlotArea(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

private fun computePlotArea(canvasWidth: Float, canvasHeight: Float, density: androidx.compose.ui.unit.Density): PlotArea {
    val leftPad = with(density) { 36.dp.toPx() }
    val bottomPad = with(density) { 24.dp.toPx() }
    val rightPad = with(density) { 8.dp.toPx() }
    val topPad = with(density) { 8.dp.toPx() }
    return PlotArea(
        left = leftPad,
        top = topPad,
        right = canvasWidth - rightPad,
        bottom = canvasHeight - bottomPad,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAxes(
    plot: PlotArea,
    xMin: Float, xMax: Float,
    yMin: Float, yMax: Float,
    axisColor: Color,
    gridColor: Color,
    labelStyle: TextStyle,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    // Axis lines.
    drawLine(
        color = axisColor,
        start = Offset(plot.left, plot.top),
        end = Offset(plot.left, plot.bottom),
        strokeWidth = 1.dp.toPx(),
    )
    drawLine(
        color = axisColor,
        start = Offset(plot.left, plot.bottom),
        end = Offset(plot.right, plot.bottom),
        strokeWidth = 1.dp.toPx(),
    )
    // Y-axis ticks: 5 evenly spaced.
    val ySteps = 5
    for (i in 0..ySteps) {
        val frac = i.toFloat() / ySteps
        val y = plot.bottom - frac * plot.height
        val value = yMin + frac * (yMax - yMin)
        drawLine(
            color = gridColor,
            start = Offset(plot.left, y),
            end = Offset(plot.right, y),
            strokeWidth = 0.5.dp.toPx(),
        )
        val label = textMeasurer.measure(AnnotatedString("%.0f".format(value)), labelStyle)
        drawText(
            textLayoutResult = label,
            topLeft = Offset(plot.left - label.size.width - 4.dp.toPx(), y - label.size.height / 2f),
        )
    }
    // X-axis ticks: at integer competition counts, max 6 labels.
    val xRange = (xMax - xMin).toInt().coerceAtLeast(1)
    val xStep = ceil(xRange / 6f).toInt().coerceAtLeast(1)
    var i = 0
    while (i <= xRange) {
        val xVal = xMin + i
        val x = plot.left + (xVal - xMin) / (xMax - xMin) * plot.width
        drawLine(
            color = gridColor,
            start = Offset(x, plot.top),
            end = Offset(x, plot.bottom),
            strokeWidth = 0.5.dp.toPx(),
            pathEffect = null,
        )
        val label = textMeasurer.measure(AnnotatedString(xVal.toInt().toString()), labelStyle)
        drawText(
            textLayoutResult = label,
            topLeft = Offset(x - label.size.width / 2f, plot.bottom + 4.dp.toPx()),
        )
        i += xStep
    }
}

private fun findNearestPoint(
    points: List<ScatterPoint>,
    tap: Offset,
    plot: PlotArea,
    xMin: Float, xMax: Float,
    yMin: Float, yMax: Float,
    maxDistancePx: Float,
): ScatterPoint? {
    var best: ScatterPoint? = null
    var bestDist = Float.MAX_VALUE
    points.forEach { p ->
        val px = plot.left + (p.x - xMin) / (xMax - xMin) * plot.width
        val py = plot.bottom - (p.y - yMin) / (yMax - yMin) * plot.height
        val d = abs(tap.x - px) + abs(tap.y - py)
        if (d < bestDist) {
            bestDist = d
            best = p
        }
    }
    return if (bestDist <= maxDistancePx * 2f) best else null
}
