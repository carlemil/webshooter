package se.kjellstrand.webshooter.ui.screens.charts.seriespoints

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import android.graphics.Color as AndroidColor
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.seriespoints.CompetitionSeries
import se.kjellstrand.webshooter.ui.common.AddShooterButton
import se.kjellstrand.webshooter.ui.common.CHART_COLORS
import se.kjellstrand.webshooter.ui.common.CHART_MIN_HEIGHT_FRACTION
import se.kjellstrand.webshooter.ui.common.ChartLegend
import se.kjellstrand.webshooter.ui.common.ChartStateWrapper
import se.kjellstrand.webshooter.ui.common.ShooterPickerDialog
import se.kjellstrand.webshooter.ui.common.UserLegendItem
import se.kjellstrand.webshooter.ui.common.WeaponClassGroupFilter
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

private val LATEST_COLOR_INT = AndroidColor.rgb(76, 255, 120)
private val OLDER_HSV = floatArrayOf(120f, 0.55f, 1f)

private fun colorForAge(indexFromNewest: Int, total: Int): Int {
    if (indexFromNewest == 0) return LATEST_COLOR_INT
    val denom = (total - 1).coerceAtLeast(1)
    val t = indexFromNewest.toFloat() / denom
    val value = 0.75f - 0.55f * t
    return AndroidColor.HSVToColor(floatArrayOf(OLDER_HSV[0], OLDER_HSV[1], value))
}

private val TREND_COLOR_ARGB: Int = CHART_COLORS[7]
private val AVERAGE_COLOR_ARGB: Int = CHART_COLORS[1]

internal const val SP_LEGEND_ID_AVERAGE = "average"
internal const val SP_LEGEND_ID_TREND = "trend"
internal const val SP_LEGEND_ID_COMP_PREFIX = "comp:"
internal fun competitionLegendId(comp: CompetitionSeries): String =
    "$SP_LEGEND_ID_COMP_PREFIX${comp.competitionId}"

@Composable
fun SeriesPointsScreen(viewModel: SeriesPointsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh()
        }
    }

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
            relevantUserIds = null
        )
    }
}

@Composable
private fun SeriesPointsContent(
    uiState: SeriesPointsUiState,
    viewModel: SeriesPointsViewModel
) {
    var highlightedLegendId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(
        uiState.selectedUserId,
        uiState.selectedYear,
        uiState.selectedGroup,
        uiState.competitions.size
    ) {
        highlightedLegendId = null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = uiState.selectedUserName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp)
        )

        if (uiState.availableYears.isNotEmpty()) {
            val tabYears: List<Int> = listOf(0) + uiState.availableYears
            val selectedIndex = tabYears.indexOf(uiState.selectedYear).coerceAtLeast(0)
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 0.dp
            ) {
                tabYears.forEach { year ->
                    Tab(
                        selected = year == uiState.selectedYear,
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
            onSelectGroup = { group -> group?.let(viewModel::selectWeaponGroup) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        val competitions = uiState.filteredCompetitions
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp

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
                seriesAverage = uiState.seriesAverage,
                seriesTrend = uiState.seriesTrend,
                highlightedId = highlightedLegendId,
                onHighlightChanged = { id ->
                    highlightedLegendId = if (highlightedLegendId == id) null else id
                },
                modifier = Modifier
                    .height(screenHeight * CHART_MIN_HEIGHT_FRACTION)
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            val legendItems = buildList {
                if (uiState.seriesAverage != null) {
                    add(
                        UserLegendItem(
                            label = stringResource(R.string.charts_legend_average),
                            color = Color(CHART_COLORS[1]),
                            shapeIndex = 7,
                            id = SP_LEGEND_ID_AVERAGE
                        )
                    )
                }
                if (uiState.seriesTrend != null) {
                    add(
                        UserLegendItem(
                            label = stringResource(R.string.charts_legend_trend),
                            color = Color(CHART_COLORS[7]),
                            shapeIndex = 8,
                            id = SP_LEGEND_ID_TREND
                        )
                    )
                }
                val ordered = competitions.asReversed()
                val total = ordered.size
                ordered.forEachIndexed { indexFromNewest, comp ->
                    add(
                        UserLegendItem(
                            label = "${comp.date} ${comp.competitionName}",
                            color = Color(colorForAge(indexFromNewest, total)),
                            shapeIndex = 7,
                            id = competitionLegendId(comp)
                        )
                    )
                }
            }
            if (legendItems.isNotEmpty()) {
                ChartLegend(
                    items = legendItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    highlightedId = highlightedLegendId,
                    onItemClick = { id ->
                        highlightedLegendId = if (highlightedLegendId == id) null else id
                    }
                )
            }
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
private fun SeriesPointsChart(
    competitions: List<CompetitionSeries>,
    seriesAverage: Float?,
    seriesTrend: SeriesPointsUiState.TrendLine?,
    highlightedId: String?,
    onHighlightChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (competitions.isEmpty()) {
        Box(modifier = modifier)
        return
    }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = onSurfaceVariant.copy(alpha = 0.18f)
    val labelStyle = MaterialTheme.typography.bodySmall.copy(color = onSurfaceVariant)
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val tapRadiusPx = with(density) { 24.dp.toPx() }

    // Newest competition draws on top + thicker. Compute color/styling per
    // competition once and reuse for both rendering and hit-testing.
    val orderedNewestFirst = remember(competitions) { competitions.asReversed() }
    val total = orderedNewestFirst.size
    val series = remember(competitions, highlightedId) {
        orderedNewestFirst.mapIndexed { indexFromNewest, comp ->
            val baseColor = Color(colorForAge(indexFromNewest, total))
            val id = competitionLegendId(comp)
            val dim = highlightedId != null && highlightedId != id
            val effective = if (dim) baseColor.copy(alpha = 0.25f) else baseColor
            ChartSeries(
                id = id,
                competition = comp,
                color = effective,
                isLatest = indexFromNewest == 0,
            )
        }.reversed() // draw oldest first → newest last (on top)
    }

    val xMin = 1f
    val xMax = (competitions.maxOf { it.seriesPoints.size }).toFloat().coerceAtLeast(xMin)
    val yValues = competitions.flatMap { it.seriesPoints.map { p -> p.toFloat() } } +
        (seriesAverage?.let { listOf(it) } ?: emptyList()) +
        (seriesTrend?.let { listOf(it.fromY, it.toY) } ?: emptyList())
    val yRaw0 = yValues.minOrNull() ?: 0f
    val yRaw1 = yValues.maxOrNull() ?: 1f
    val yPad = ((yRaw1 - yRaw0).coerceAtLeast(1f)) * 0.05f
    val yMin = floor(yRaw0 - yPad)
    val yMax = ceil(yRaw1 + yPad)

    var tappedPoint by remember(series) { mutableStateOf<TappedPoint?>(null) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(series, xMin, xMax, yMin, yMax) {
                    detectTapGestures { tap ->
                        val plot = computePlotArea(size.width.toFloat(), size.height.toFloat(), density)
                        val nearest = findNearestPoint(
                            series = series,
                            tap = tap,
                            plot = plot,
                            xMin = xMin, xMax = xMax,
                            yMin = yMin, yMax = yMax,
                            maxDistancePx = tapRadiusPx,
                        )
                        if (nearest != null) {
                            tappedPoint = nearest
                            onHighlightChanged(nearest.seriesId)
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

            // Average horizontal limit line.
            if (seriesAverage != null) {
                val avgBase = Color(AVERAGE_COLOR_ARGB)
                val avgEffective = if (highlightedId != null && highlightedId != SP_LEGEND_ID_AVERAGE)
                    avgBase.copy(alpha = 0.25f) else avgBase
                val y = plot.bottom - (seriesAverage - yMin) / (yMax - yMin) * plot.height
                drawLine(
                    color = avgEffective,
                    start = Offset(plot.left, y),
                    end = Offset(plot.right, y),
                    strokeWidth = 2.dp.toPx(),
                )
            }

            // Trend sloped limit line.
            if (seriesTrend != null) {
                val trendBase = Color(TREND_COLOR_ARGB)
                val trendEffective = if (highlightedId != null && highlightedId != SP_LEGEND_ID_TREND)
                    trendBase.copy(alpha = 0.25f) else trendBase
                val (x0, y0) = projectPoint(seriesTrend.fromX, seriesTrend.fromY, plot, xMin, xMax, yMin, yMax)
                val (x1, y1) = projectPoint(seriesTrend.toX, seriesTrend.toY, plot, xMin, xMax, yMin, yMax)
                drawLine(
                    color = trendEffective,
                    start = Offset(x0, y0),
                    end = Offset(x1, y1),
                    strokeWidth = 2.dp.toPx(),
                )
            }

            // Each competition's polyline (oldest → newest, so newest is on top).
            series.forEach { s ->
                if (s.competition.seriesPoints.isEmpty()) return@forEach
                val path = Path()
                s.competition.seriesPoints.forEachIndexed { i, points ->
                    val (px, py) = projectPoint((i + 1).toFloat(), points.toFloat(), plot, xMin, xMax, yMin, yMax)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                drawPath(
                    path = path,
                    color = s.color,
                    style = Stroke(
                        width = (if (s.isLatest) 3.5.dp else 1.5.dp).toPx(),
                        cap = StrokeCap.Round,
                    ),
                )
                // Latest series gets visible point markers.
                if (s.isLatest) {
                    s.competition.seriesPoints.forEachIndexed { i, points ->
                        val (px, py) = projectPoint((i + 1).toFloat(), points.toFloat(), plot, xMin, xMax, yMin, yMax)
                        drawCircle(color = s.color, radius = 4.dp.toPx(), center = Offset(px, py))
                    }
                }
            }
        }

        val tp = tappedPoint
        if (tp != null && (highlightedId == null || highlightedId == tp.seriesId)) {
            SeriesMarker(
                tapped = tp,
                xMin = xMin, xMax = xMax,
                yMin = yMin, yMax = yMax,
            )
        }
    }
}

@Composable
private fun SeriesMarker(
    tapped: TappedPoint,
    xMin: Float, xMax: Float,
    yMin: Float, yMax: Float,
) {
    val density = LocalDensity.current
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                    val plot = computePlotArea(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat(), density)
                    val (px, py) = projectPoint(tapped.x, tapped.y, plot, xMin, xMax, yMin, yMax)
                    val x = (px - placeable.width / 2f).toInt()
                        .coerceIn(0, (constraints.maxWidth - placeable.width).coerceAtLeast(0))
                    val y = (py - placeable.height - 8 * density.density).toInt().coerceAtLeast(0)
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(IntOffset(x, y))
                    }
                }
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${tapped.y.toLong()} p",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private data class ChartSeries(
    val id: String,
    val competition: CompetitionSeries,
    val color: Color,
    val isLatest: Boolean,
)

private data class TappedPoint(
    val seriesId: String,
    val x: Float,
    val y: Float,
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

private fun computePlotArea(canvasWidth: Float, canvasHeight: Float, density: Density): PlotArea {
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

private fun projectPoint(
    x: Float,
    y: Float,
    plot: PlotArea,
    xMin: Float, xMax: Float,
    yMin: Float, yMax: Float,
): Pair<Float, Float> {
    val px = plot.left + (x - xMin) / (xMax - xMin) * plot.width
    val py = plot.bottom - (y - yMin) / (yMax - yMin) * plot.height
    return px to py
}

private fun DrawScope.drawAxes(
    plot: PlotArea,
    xMin: Float, xMax: Float,
    yMin: Float, yMax: Float,
    axisColor: Color,
    gridColor: Color,
    labelStyle: TextStyle,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
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
    val xRange = (xMax - xMin).toInt().coerceAtLeast(1)
    val xStep = ceil(xRange / 10f).toInt().coerceAtLeast(1)
    var i = 0
    while (i <= xRange) {
        val xVal = xMin + i
        val x = plot.left + (xVal - xMin) / (xMax - xMin) * plot.width
        drawLine(
            color = gridColor,
            start = Offset(x, plot.top),
            end = Offset(x, plot.bottom),
            strokeWidth = 0.5.dp.toPx(),
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
    series: List<ChartSeries>,
    tap: Offset,
    plot: PlotArea,
    xMin: Float, xMax: Float,
    yMin: Float, yMax: Float,
    maxDistancePx: Float,
): TappedPoint? {
    var best: TappedPoint? = null
    var bestDist = Float.MAX_VALUE
    series.forEach { s ->
        s.competition.seriesPoints.forEachIndexed { i, points ->
            val xVal = (i + 1).toFloat()
            val yVal = points.toFloat()
            val (px, py) = projectPoint(xVal, yVal, plot, xMin, xMax, yMin, yMax)
            val d = abs(tap.x - px) + abs(tap.y - py)
            if (d < bestDist) {
                bestDist = d
                best = TappedPoint(seriesId = s.id, x = xVal, y = yVal)
            }
        }
    }
    return if (bestDist <= maxDistancePx * 2f) best else null
}
