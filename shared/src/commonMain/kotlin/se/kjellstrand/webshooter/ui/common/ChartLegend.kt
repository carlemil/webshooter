package se.kjellstrand.webshooter.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** A single legend chip. [shapeIndex] indexes into [ChartShape.forIndex] for
 *  shapes (0..6); 7 = horizontal line (used for "average" series), 8 = sloped
 *  line (used for "trend" series). */
data class UserLegendItem(
    val label: String,
    val color: Color,
    val shapeIndex: Int,
    val id: String = label,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChartLegend(
    items: List<UserLegendItem>,
    modifier: Modifier = Modifier,
    highlightedId: String? = null,
    onItemClick: ((String) -> Unit)? = null,
) {
    FlowRow(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            val dimmed = highlightedId != null && highlightedId != item.id
            val rowModifier = if (onItemClick != null) {
                Modifier.clickable { onItemClick(item.id) }
            } else Modifier
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = rowModifier.alpha(if (dimmed) 0.35f else 1f)
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.minDimension / 2f
                    val strokeWidth = 2.dp.toPx()
                    when (item.shapeIndex) {
                        in 0 until CHART_SHAPE_COUNT ->
                            drawScatterShape(
                                shape = ChartShape.forIndex(item.shapeIndex),
                                color = item.color,
                                center = Offset(cx, cy),
                                size = r * 2f,
                            )
                        // 7 = horizontal line (average series legend swatch)
                        7 -> drawLine(
                            color = item.color,
                            start = Offset(cx - r, cy),
                            end = Offset(cx + r, cy),
                            strokeWidth = strokeWidth,
                        )
                        // 8 = sloped line (trend series legend swatch)
                        8 -> drawLine(
                            color = item.color,
                            start = Offset(cx - r, cy + r * 0.6f),
                            end = Offset(cx + r, cy - r * 0.6f),
                            strokeWidth = strokeWidth,
                        )
                    }
                }
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
