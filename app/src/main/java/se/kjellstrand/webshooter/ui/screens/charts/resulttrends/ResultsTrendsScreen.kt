package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.charts.ChartDataPoint
import se.kjellstrand.webshooter.ui.common.AddShooterButton
import se.kjellstrand.webshooter.ui.common.CHART_COLORS
import se.kjellstrand.webshooter.ui.common.CHART_MIN_HEIGHT_FRACTION
import se.kjellstrand.webshooter.ui.common.CHART_SHAPE_COUNT
import se.kjellstrand.webshooter.ui.common.ChartLegend
import se.kjellstrand.webshooter.ui.common.ChartShape
import se.kjellstrand.webshooter.ui.common.ChartStateWrapper
import se.kjellstrand.webshooter.ui.common.ShooterPickerDialog
import se.kjellstrand.webshooter.ui.common.UserLegendItem
import se.kjellstrand.webshooter.ui.common.WeaponClassGroupFilter
import se.kjellstrand.webshooter.ui.common.drawScatterShape
import se.kjellstrand.webshooter.ui.mock.ChartsViewModelMock
import se.kjellstrand.webshooter.ui.mock.MockCharts
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun ChartsScreen(viewModel: ResultsTrendsViewModel) {
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
                text = stringResource(R.string.charts_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
            )
            ChartStateWrapper(
                isLoading = uiState.isLoading && uiState.chartData.isEmpty(),
                hasError = uiState.hasError
            ) {
                ChartsContent(uiState, viewModel)
            }
        }
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
            onDismiss = { viewModel.setShowSearchDialog(false) },
            relevantUserIds = uiState.relevantUserIds
        )
    }
}

@Composable
private fun ChartsContent(uiState: ChartsUiState, viewModel: ResultsTrendsViewModel) {
    var highlightedLegendId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(
        uiState.selectedResultsType,
        uiState.selectedGroup,
        uiState.comparedShooters.keys
    ) {
        highlightedLegendId = null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.availableResultsTypes.isNotEmpty()) {
            val selectedIndex = uiState.availableResultsTypes.indexOf(uiState.selectedResultsType)
                .coerceAtLeast(0)

            TabRow(selectedTabIndex = selectedIndex) {
                uiState.availableResultsTypes.forEach { type ->
                    Tab(
                        selected = type == uiState.selectedResultsType,
                        onClick = { viewModel.selectTab(type) },
                        text = { Text(trendsTabDisplayName(type)) }
                    )
                }
            }
        }

        WeaponClassGroupFilter(
            availableGroups = uiState.availableGroups,
            selectedGroup = uiState.selectedGroup,
            onSelectGroup = viewModel::selectWeaponGroup,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            showAllOption = uiState.selectedResultsType == MAGNUMPRECISION_TAB_KEY
        )

        val chartData = uiState.filteredChartData
        val comparedShooters = uiState.filteredComparedShooters
        val hasAnyData = chartData.isNotEmpty() ||
            comparedShooters.values.any { it.chartData.isNotEmpty() }
        val isHitsBased = uiState.selectedResultsType == "field" ||
            uiState.selectedResultsType == "pointfield"

        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        if (uiState.availableResultsTypes.isNotEmpty()) {
            ChartScatterChart(
                myData = chartData,
                comparedShooters = comparedShooters,
                myAverage = uiState.myAverage,
                myTrend = uiState.myTrend,
                isHitsBased = isHitsBased,
                highlightedId = highlightedLegendId,
                onHighlightChanged = { id ->
                    highlightedLegendId = if (highlightedLegendId == id) null else id
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = screenHeight * CHART_MIN_HEIGHT_FRACTION)
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            val legendItems = buildList {
                if (chartData.isNotEmpty()) {
                    add(
                        UserLegendItem(
                            label = stringResource(R.string.charts_my_results),
                            color = Color(CHART_COLORS[0]),
                            shapeIndex = 0,
                            id = LEGEND_ID_ME
                        )
                    )
                }
                comparedShooters.entries.forEachIndexed { index, (userId, info) ->
                    if (info.chartData.isNotEmpty()) {
                        val colorIndex = (index + 1) % CHART_COLORS.size
                        val shapeIndex = (index + 1) % CHART_SHAPE_COUNT
                        add(
                            UserLegendItem(
                                label = info.name,
                                color = Color(CHART_COLORS[colorIndex]),
                                shapeIndex = shapeIndex,
                                id = "$LEGEND_ID_COMPARED_PREFIX$userId"
                            )
                        )
                    }
                }
                if (uiState.myAverage != null) {
                    add(
                        UserLegendItem(
                            label = stringResource(R.string.charts_legend_average),
                            color = Color(AVERAGE_COLOR_ARGB),
                            shapeIndex = 7,
                            id = LEGEND_ID_AVERAGE
                        )
                    )
                }
                if (uiState.myTrend != null && !isHitsBased) {
                    add(
                        UserLegendItem(
                            label = stringResource(R.string.charts_legend_trend),
                            color = Color(TREND_COLOR_ARGB),
                            shapeIndex = 8,
                            id = LEGEND_ID_TREND
                        )
                    )
                }
            }
            if (legendItems.isNotEmpty()) {
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

        AddShooterButton(
            text = stringResource(R.string.charts_add_shooter),
            onClick = { viewModel.setShowSearchDialog(true) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private val AVERAGE_COLOR_ARGB: Int = 0xFF7FFF00.toInt()
private val TREND_COLOR_ARGB: Int = 0xFF00FF40.toInt()

internal const val LEGEND_ID_ME = "me"
internal const val LEGEND_ID_COMPARED_PREFIX = "compared:"
internal const val LEGEND_ID_AVERAGE = "average"
internal const val LEGEND_ID_TREND = "trend"

@Composable
fun ChartScatterChart(
    myData: List<ChartDataPoint>,
    comparedShooters: Map<Long, ShooterChartInfo>,
    myAverage: Float?,
    myTrend: ChartsUiState.TrendLine?,
    isHitsBased: Boolean,
    modifier: Modifier = Modifier,
    highlightedId: String? = null,
    onHighlightChanged: (String) -> Unit = {},
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = onSurfaceVariant.copy(alpha = 0.18f)
    val labelStyle = MaterialTheme.typography.bodySmall.copy(color = onSurfaceVariant)
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val pointSizePx = with(density) { 18.dp.toPx() }
    val tapRadiusPx = with(density) { 24.dp.toPx() }
    val myResultsLabel = stringResource(R.string.charts_my_results)

    // Distinct sorted dates form the X-axis. Each date's float position is its
    // index in this list, so each series can map its dates to X coordinates.
    val sortedDates = remember(myData, comparedShooters) {
        (myData + comparedShooters.values.flatMap { it.chartData })
            .map { it.date }
            .distinct()
            .sorted()
    }

    val series = remember(myData, comparedShooters, highlightedId, sortedDates) {
        buildList {
            if (myData.isNotEmpty()) {
                add(
                    buildSeries(
                        id = LEGEND_ID_ME,
                        label = myResultsLabel,
                        baseColor = Color(CHART_COLORS[0]),
                        shape = ChartShape.forIndex(0),
                        chartData = myData,
                        sortedDates = sortedDates,
                        highlightedId = highlightedId,
                    )
                )
            }
            comparedShooters.entries.forEachIndexed { index, (userId, info) ->
                if (info.chartData.isNotEmpty()) {
                    val colorIndex = (index + 1) % CHART_COLORS.size
                    val shapeIndex = (index + 1) % CHART_SHAPE_COUNT
                    add(
                        buildSeries(
                            id = "$LEGEND_ID_COMPARED_PREFIX$userId",
                            label = info.name,
                            baseColor = Color(CHART_COLORS[colorIndex]),
                            shape = ChartShape.forIndex(shapeIndex),
                            chartData = info.chartData,
                            sortedDates = sortedDates,
                            highlightedId = highlightedId,
                        )
                    )
                }
            }
        }
    }

    val xMin = 0f
    val xMax = (sortedDates.size - 1).toFloat().coerceAtLeast(0f)
    val rawYValues = series.flatMap { s -> s.points.map { it.y } } +
        (myAverage?.let { listOf(it) } ?: emptyList()) +
        (myTrend?.let { listOf(it.fromY, it.toY) } ?: emptyList())
    val yRaw0 = rawYValues.minOrNull() ?: 0f
    val yRaw1 = if (isHitsBased) 6f else (rawYValues.maxOrNull() ?: 1f)
    val yPad = ((yRaw1 - yRaw0).coerceAtLeast(1f)) * 0.05f
    val yMin = if (isHitsBased) 0f else floor(yRaw0 - yPad)
    val yMax = if (isHitsBased) 6f else ceil(yRaw1 + yPad)

    var tappedPoint by remember(series) { mutableStateOf<TappedScatter?>(null) }

    Box(modifier = modifier) {
        if (sortedDates.isEmpty()) {
            // Keep the chart frame visible even before data streams in.
            Canvas(modifier = Modifier.fillMaxSize()) {
                val plot = computePlotArea(size.width, size.height, density)
                drawChartFrame(plot, onSurface)
            }
            return@Box
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(series, xMin, xMax, yMin, yMax) {
                    detectTapGestures { tap ->
                        val plot = computePlotArea(size.width.toFloat(), size.height.toFloat(), density)
                        val nearest = findNearestScatter(
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
            drawAxesWithDateLabels(
                plot = plot,
                sortedDates = sortedDates,
                xMin = xMin, xMax = xMax,
                yMin = yMin, yMax = yMax,
                axisColor = onSurface,
                gridColor = gridColor,
                labelStyle = labelStyle,
                textMeasurer = textMeasurer,
            )

            // Average horizontal line.
            if (myAverage != null) {
                val avgBase = Color(AVERAGE_COLOR_ARGB)
                val avgEffective = if (highlightedId != null && highlightedId != LEGEND_ID_AVERAGE)
                    avgBase.copy(alpha = 0.25f) else avgBase
                val y = plot.bottom - (myAverage - yMin) / (yMax - yMin) * plot.height
                drawLine(
                    color = avgEffective,
                    start = Offset(plot.left, y),
                    end = Offset(plot.right, y),
                    strokeWidth = 2.dp.toPx(),
                )
            }

            // Trend line: drawn from the first to the last "my data" point on
            // the X-axis; only present for non-hits-based result types.
            val sortedMy = myData.sortedBy { it.date }
            if (myTrend != null && !isHitsBased && sortedMy.size >= 2) {
                val firstX = sortedDates.indexOf(sortedMy.first().date).toFloat().coerceAtLeast(0f)
                val lastX = sortedDates.indexOf(sortedMy.last().date).toFloat().coerceAtLeast(0f)
                val (x0, y0) = projectPoint(firstX, myTrend.fromY, plot, xMin, xMax, yMin, yMax)
                val (x1, y1) = projectPoint(lastX, myTrend.toY, plot, xMin, xMax, yMin, yMax)
                val trendBase = Color(TREND_COLOR_ARGB)
                val trendEffective = if (highlightedId != null && highlightedId != LEGEND_ID_TREND)
                    trendBase.copy(alpha = 0.25f) else trendBase
                drawLine(
                    color = trendEffective,
                    start = Offset(x0, y0),
                    end = Offset(x1, y1),
                    strokeWidth = 2.dp.toPx(),
                )
            }

            // Scatter points across all series.
            series.forEach { s ->
                s.points.forEach { p ->
                    val (px, py) = projectPoint(p.x, p.y, plot, xMin, xMax, yMin, yMax)
                    drawScatterShape(
                        shape = s.shape,
                        color = s.color,
                        center = Offset(px, py),
                        size = pointSizePx,
                    )
                }
            }
        }

        val tp = tappedPoint
        if (tp != null && (highlightedId == null || highlightedId == tp.seriesId)) {
            ScatterMarker(
                tapped = tp,
                xMin = xMin, xMax = xMax,
                yMin = yMin, yMax = yMax,
            )
        }
    }
}

@Composable
private fun ScatterMarker(
    tapped: TappedScatter,
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
                text = tapped.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private data class ScatterSeries(
    val id: String,
    val label: String,
    val color: Color,
    val shape: ChartShape,
    val points: List<ScatterPoint>,
)

private data class ScatterPoint(
    val x: Float,
    val y: Float,
    val displayLabel: String,
)

private data class TappedScatter(
    val seriesId: String,
    val x: Float,
    val y: Float,
    val label: String,
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

private fun buildSeries(
    id: String,
    label: String,
    baseColor: Color,
    shape: ChartShape,
    chartData: List<ChartDataPoint>,
    sortedDates: List<String>,
    highlightedId: String?,
): ScatterSeries {
    val dim = highlightedId != null && highlightedId != id
    val effective = if (dim) baseColor.copy(alpha = 0.25f) else baseColor
    val points = chartData.sortedBy { it.date }.map { dp ->
        val xVal = sortedDates.indexOf(dp.date).toFloat().coerceAtLeast(0f)
        val yVal = dp.averageSerieScore.toFloat()
        val display = "%.1f".format(dp.averageSerieScore).removeSuffix(".0").removeSuffix(",0")
        ScatterPoint(
            x = xVal,
            y = yVal,
            displayLabel = "$label\n${dp.date}: $display p",
        )
    }
    return ScatterSeries(
        id = id,
        label = label,
        color = effective,
        shape = shape,
        points = points,
    )
}

private fun computePlotArea(canvasWidth: Float, canvasHeight: Float, density: Density): PlotArea {
    val leftPad = with(density) { 36.dp.toPx() }
    // Extra bottom padding for the rotated date labels.
    val bottomPad = with(density) { 44.dp.toPx() }
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
    // Guard against single-point ranges (xMax == xMin) which would otherwise
    // produce a NaN coordinate.
    val xSpan = (xMax - xMin).takeIf { it > 0f } ?: 1f
    val ySpan = (yMax - yMin).takeIf { it > 0f } ?: 1f
    val px = plot.left + (x - xMin) / xSpan * plot.width
    val py = plot.bottom - (y - yMin) / ySpan * plot.height
    return px to py
}

private fun DrawScope.drawChartFrame(plot: PlotArea, axisColor: Color) {
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
}

private fun DrawScope.drawAxesWithDateLabels(
    plot: PlotArea,
    sortedDates: List<String>,
    xMin: Float, xMax: Float,
    yMin: Float, yMax: Float,
    axisColor: Color,
    gridColor: Color,
    labelStyle: TextStyle,
    textMeasurer: TextMeasurer,
) {
    drawChartFrame(plot, axisColor)

    // Y-axis: 5 evenly spaced ticks.
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

    // X-axis: rotated -45° date labels, max 6.
    val maxLabels = 6
    val total = sortedDates.size
    if (total == 0) return
    val step = ceil(total.toFloat() / maxLabels).toInt().coerceAtLeast(1)
    val xSpan = (xMax - xMin).takeIf { it > 0f } ?: 1f
    var i = 0
    while (i < total) {
        val dateString = sortedDates[i]
        val xVal = i.toFloat()
        val x = plot.left + (xVal - xMin) / xSpan * plot.width
        drawLine(
            color = gridColor,
            start = Offset(x, plot.top),
            end = Offset(x, plot.bottom),
            strokeWidth = 0.5.dp.toPx(),
        )
        val label = textMeasurer.measure(AnnotatedString(dateString), labelStyle)
        // Rotate -45° around the label's anchor (just below the axis line).
        rotate(degrees = -45f, pivot = Offset(x, plot.bottom + 4.dp.toPx())) {
            drawText(
                textLayoutResult = label,
                topLeft = Offset(x - label.size.width, plot.bottom + 4.dp.toPx()),
            )
        }
        i += step
    }
}

private fun findNearestScatter(
    series: List<ScatterSeries>,
    tap: Offset,
    plot: PlotArea,
    xMin: Float, xMax: Float,
    yMin: Float, yMax: Float,
    maxDistancePx: Float,
): TappedScatter? {
    var best: TappedScatter? = null
    var bestDist = Float.MAX_VALUE
    series.forEach { s ->
        s.points.forEach { p ->
            val (px, py) = projectPoint(p.x, p.y, plot, xMin, xMax, yMin, yMax)
            val d = abs(tap.x - px) + abs(tap.y - py)
            if (d < bestDist) {
                bestDist = d
                best = TappedScatter(
                    seriesId = s.id,
                    x = p.x,
                    y = p.y,
                    label = p.displayLabel,
                )
            }
        }
    }
    return if (bestDist <= maxDistancePx * 2f) best else null
}

@Preview(showBackground = true, name = "Charts - Default with data")
@Composable
fun ChartsScreenDefaultPreview() {
    val mock = MockCharts()
    ChartsScreen(
        viewModel = ChartsViewModelMock(
            ChartsUiState(
                chartData = mock.allChartData,
                availableResultsTypes = mock.availableResultsTypes,
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
        viewModel = ChartsViewModelMock(
            ChartsUiState(isLoading = true)
        )
    )
}

@Preview(showBackground = true, name = "Charts - Error")
@Composable
fun ChartsScreenErrorPreview() {
    ChartsScreen(
        viewModel = ChartsViewModelMock(
            ChartsUiState(hasError = true)
        )
    )
}

@Preview(showBackground = true, name = "Charts - Empty")
@Composable
fun ChartsScreenEmptyPreview() {
    val mock = MockCharts()
    ChartsScreen(
        viewModel = ChartsViewModelMock(
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
    val mock = MockCharts()
    ChartsScreen(
        viewModel = ChartsViewModelMock(
            ChartsUiState(
                chartData = mock.allChartData,
                comparedShooters = mock.comparedShooters,
                availableResultsTypes = mock.availableResultsTypes,
                selectedResultsType = "precision",
                clubMembers = mock.clubMembers
            )
        )
    )
}
