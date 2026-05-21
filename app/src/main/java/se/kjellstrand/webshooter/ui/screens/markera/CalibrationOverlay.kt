package se.kjellstrand.webshooter.ui.screens.markera

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import se.kjellstrand.webshooter.data.vision.TargetCalibration
import kotlin.math.min

/**
 * Draws the calibrated target centre as a crosshair plus the fitted
 * 7-ring as a circle. Same FIT_CENTER mapping as [DetectionOverlay] so
 * the markings land on top of the same image the user sees.
 */
@Composable
fun CalibrationOverlay(
    calibration: TargetCalibration?,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier,
    color: Color = Color(0x9900E5FF), // cyan, ~60% alpha
    strokeWidthPx: Float = 3f,
    crosshairHalfPx: Float = 18f,
) {
    Canvas(modifier = modifier) {
        if (calibration == null || imageWidth <= 0 || imageHeight <= 0) return@Canvas
        val scale = min(size.width / imageWidth, size.height / imageHeight)
        val offsetX = (size.width - imageWidth * scale) / 2f
        val offsetY = (size.height - imageHeight * scale) / 2f
        val cx = calibration.centerX * scale + offsetX
        val cy = calibration.centerY * scale + offsetY
        val r = calibration.radiusPx * scale

        // 7-ring circle
        drawCircle(
            color = color,
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = strokeWidthPx),
        )
        // Crosshair
        drawLine(
            color = color,
            start = Offset(cx - crosshairHalfPx, cy),
            end = Offset(cx + crosshairHalfPx, cy),
            strokeWidth = strokeWidthPx,
        )
        drawLine(
            color = color,
            start = Offset(cx, cy - crosshairHalfPx),
            end = Offset(cx, cy + crosshairHalfPx),
            strokeWidth = strokeWidthPx,
        )
    }
}
