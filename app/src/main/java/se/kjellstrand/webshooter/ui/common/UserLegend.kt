package se.kjellstrand.webshooter.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class UserLegendItem(
    val label: String,
    val color: Color,
    val shapeIndex: Int,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserLegend(
    items: List<UserLegendItem>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawScatterShape(item.shapeIndex, item.color)
                }
                Text(
                    text = item.label,
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
                size = Size(r * 2, r * 2)
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
        7 -> { // HORIZONTAL LINE (for average)
            drawLine(color, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth)
        }
        8 -> { // SLOPED LINE (for trend)
            drawLine(color, Offset(cx - r, cy + r * 0.6f), Offset(cx + r, cy - r * 0.6f), strokeWidth)
        }
    }
}
