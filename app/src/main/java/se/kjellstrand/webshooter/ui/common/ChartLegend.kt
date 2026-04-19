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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.utils.ViewPortHandler
import android.graphics.Paint as AndroidPaint

data class UserLegendItem(
    val label: String,
    val color: Color,
    val shapeIndex: Int,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChartLegend(
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
                Canvas(modifier = Modifier.size(24.dp)) {
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

private val legendViewPortHandler = ViewPortHandler()

private fun DrawScope.drawScatterShape(shapeIndex: Int, color: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension / 2f
    val strokeWidth = 2.dp.toPx()

    if (shapeIndex in CHART_SHAPE_RENDERERS.indices) {
        val argb = color.toArgb()
        val paint = AndroidPaint().apply {
            this.color = argb
            isAntiAlias = true
        }
        val dataSet = ScatterDataSet(mutableListOf<Entry>(), "").apply {
            scatterShapeSize = r * 2f
        }
        CHART_SHAPE_RENDERERS[shapeIndex].renderShape(
            drawContext.canvas.nativeCanvas,
            dataSet,
            legendViewPortHandler,
            cx, cy,
            paint
        )
        return
    }

    when (shapeIndex) {
        7 -> { // HORIZONTAL LINE (for average)
            drawLine(color, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth)
        }
        8 -> { // SLOPED LINE (for trend)
            drawLine(color, Offset(cx - r, cy + r * 0.6f), Offset(cx + r, cy - r * 0.6f), strokeWidth)
        }
    }
}
