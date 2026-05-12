package se.kjellstrand.webshooter.ui.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

const val CHART_MIN_HEIGHT_FRACTION = 0.5f

/**
 * Default per-series color palette. Uses ARGB ints so that the value is
 * portable across Android Color and Compose Color (use `Color(value)` to lift).
 */
val CHART_COLORS: List<Int> = listOf(
    0xFF4CAF50.toInt(), // Green
    0xFF2196F3.toInt(), // Blue
    0xFFFF9800.toInt(), // Orange
    0xFF9C27B0.toInt(), // Purple
    0xFFF44336.toInt(), // Red (Material Red 500)
    0xFFE91EC7.toInt(), // Magenta/Fuchsia
    0xFF00BCD4.toInt(), // Cyan
    0xFFFFEB3B.toInt(), // Yellow
)

/**
 * Number of distinct scatter shapes (kept as a constant so callers don't need
 * to import [ChartShape] just to compute `index % shapeCount`).
 */
const val CHART_SHAPE_COUNT: Int = 7

/** The 7 scatter shapes, ordered by `shapeIndex`. */
enum class ChartShape {
    Circle, Square, Triangle, Cross, X, ChevronDown, ChevronUp;

    companion object {
        fun forIndex(index: Int): ChartShape = entries[index.mod(entries.size)]
    }
}

/**
 * Draw a scatter shape into the current [DrawScope] centered on [center].
 * [size] is the bounding diameter in pixels; stroke shapes use a 2.dp line.
 */
fun DrawScope.drawScatterShape(
    shape: ChartShape,
    color: Color,
    center: Offset,
    size: Float,
) {
    val half = size / 2f
    val strokeWidth = 2.dp.toPx()
    when (shape) {
        ChartShape.Circle -> drawCircle(color = color, radius = half, center = center)
        ChartShape.Square -> drawRect(
            color = color,
            topLeft = Offset(center.x - half, center.y - half),
            size = androidx.compose.ui.geometry.Size(size, size),
        )
        ChartShape.Triangle -> {
            val path = Path().apply {
                moveTo(center.x, center.y - half)
                lineTo(center.x + half, center.y + half)
                lineTo(center.x - half, center.y + half)
                close()
            }
            drawPath(path = path, color = color)
        }
        ChartShape.Cross -> {
            drawLine(
                color = color,
                start = Offset(center.x - half, center.y),
                end = Offset(center.x + half, center.y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(center.x, center.y - half),
                end = Offset(center.x, center.y + half),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
        ChartShape.X -> {
            val arm = half * 0.8f
            drawLine(
                color = color,
                start = Offset(center.x - arm, center.y - arm),
                end = Offset(center.x + arm, center.y + arm),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(center.x + arm, center.y - arm),
                end = Offset(center.x - arm, center.y + arm),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
        ChartShape.ChevronDown -> {
            val path = Path().apply {
                moveTo(center.x - half, center.y - half * 0.5f)
                lineTo(center.x, center.y + half * 0.5f)
                lineTo(center.x + half, center.y - half * 0.5f)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
        ChartShape.ChevronUp -> {
            val path = Path().apply {
                moveTo(center.x - half, center.y + half * 0.5f)
                lineTo(center.x, center.y - half * 0.5f)
                lineTo(center.x + half, center.y + half * 0.5f)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}
