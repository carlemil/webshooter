package se.kjellstrand.webshooter.ui.common

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer
import com.github.mikephil.charting.utils.ViewPortHandler
import android.graphics.Color as AndroidColor

const val CHART_MIN_HEIGHT_FRACTION = 0.5f

val CHART_COLORS = listOf(
    AndroidColor.rgb(76, 175, 80),   // Green
    AndroidColor.rgb(33, 150, 243),  // Blue
    AndroidColor.rgb(255, 152, 0),   // Orange
    AndroidColor.rgb(156, 39, 176),  // Purple
    AndroidColor.rgb(244, 67, 54),   // Red (Material Red 500)
    AndroidColor.rgb(233, 30, 199),  // Magenta/Fuchsia (replacing Brown)
    AndroidColor.rgb(0, 188, 212),   // Cyan
    AndroidColor.rgb(255, 235, 59)   // Yellow
)

private val STROKE_WIDTH = 6.dp.value
private val SHAPE_SIZE_MOD = 2.6.dp.value

private class CircleRenderer : IShapeRenderer {
    override fun renderShape(
        c: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeHalf = dataSet.scatterShapeSize / SHAPE_SIZE_MOD
        renderPaint.style = Paint.Style.FILL
        c.drawCircle(posX, posY, shapeHalf, renderPaint)
    }
}

private class SquareRenderer : IShapeRenderer {
    override fun renderShape(
        c: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeHalf = dataSet.scatterShapeSize / SHAPE_SIZE_MOD
        renderPaint.style = Paint.Style.FILL
        c.drawRect(
            posX - shapeHalf, posY - shapeHalf,
            posX + shapeHalf, posY + shapeHalf, renderPaint
        )
    }
}

private class TriangleRenderer : IShapeRenderer {
    private val path = Path()
    override fun renderShape(
        c: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeHalf = dataSet.scatterShapeSize / SHAPE_SIZE_MOD
        renderPaint.style = Paint.Style.FILL
        path.reset()
        path.moveTo(posX, posY - shapeHalf)
        path.lineTo(posX + shapeHalf, posY + shapeHalf)
        path.lineTo(posX - shapeHalf, posY + shapeHalf)
        path.close()
        c.drawPath(path, renderPaint)
    }
}

private class CrossRenderer : IShapeRenderer {
    override fun renderShape(
        c: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeHalf = dataSet.scatterShapeSize / SHAPE_SIZE_MOD
        renderPaint.style = Paint.Style.STROKE
        renderPaint.strokeWidth = STROKE_WIDTH
        c.drawLine(posX - shapeHalf, posY, posX + shapeHalf, posY, renderPaint)
        c.drawLine(posX, posY - shapeHalf, posX, posY + shapeHalf, renderPaint)
    }
}

private class XRenderer : IShapeRenderer {
    override fun renderShape(
        c: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeSize = dataSet.scatterShapeSize / SHAPE_SIZE_MOD * 0.7f
        renderPaint.style = Paint.Style.STROKE
        renderPaint.strokeWidth = STROKE_WIDTH
        c.drawLine(
            posX - shapeSize, posY - shapeSize,
            posX + shapeSize, posY + shapeSize, renderPaint
        )
        c.drawLine(
            posX + shapeSize, posY - shapeSize,
            posX - shapeSize, posY + shapeSize, renderPaint
        )
    }
}

private class ChevronDownRenderer : IShapeRenderer {
    private val path = Path()
    override fun renderShape(
        c: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeHalf = dataSet.scatterShapeSize / SHAPE_SIZE_MOD
        renderPaint.style = Paint.Style.STROKE
        renderPaint.strokeWidth = STROKE_WIDTH
        renderPaint.strokeCap = Paint.Cap.ROUND
        renderPaint.strokeJoin = Paint.Join.ROUND
        path.reset()
        path.moveTo(posX - shapeHalf, posY - shapeHalf * 0.5f)
        path.lineTo(posX, posY + shapeHalf * 0.5f)
        path.lineTo(posX + shapeHalf, posY - shapeHalf * 0.5f)
        c.drawPath(path, renderPaint)
    }
}

private class ChevronUpRenderer : IShapeRenderer {
    private val path = Path()
    override fun renderShape(
        c: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeHalf = dataSet.scatterShapeSize / SHAPE_SIZE_MOD
        renderPaint.style = Paint.Style.STROKE
        renderPaint.strokeWidth = STROKE_WIDTH
        renderPaint.strokeCap = Paint.Cap.ROUND
        renderPaint.strokeJoin = Paint.Join.ROUND
        path.reset()
        path.moveTo(posX - shapeHalf, posY + shapeHalf * 0.5f)
        path.lineTo(posX, posY - shapeHalf * 0.5f)
        path.lineTo(posX + shapeHalf, posY + shapeHalf * 0.5f)
        c.drawPath(path, renderPaint)
    }
}

val CHART_SHAPE_RENDERERS: List<IShapeRenderer> = listOf(
    CircleRenderer(),
    SquareRenderer(),
    TriangleRenderer(),
    CrossRenderer(),
    XRenderer(),
    ChevronDownRenderer(),
    ChevronUpRenderer()
)
