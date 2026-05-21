package se.kjellstrand.webshooter.ui.screens.markera

import android.graphics.Bitmap
import se.kjellstrand.webshooter.data.vision.TARGET_BLACK_RING_RADIUS_MM
import se.kjellstrand.webshooter.data.vision.TargetCalibration
import se.kjellstrand.webshooter.data.vision.calibrateFromGrayscale

/**
 * Subsample [this] Bitmap by [stride] in each axis, convert to grayscale
 * using BT.601 luma weights, and run [calibrateFromGrayscale]. Returns a
 * [TargetCalibration] in **original full-resolution Bitmap coordinates**
 * — centre and radius scaled back up by [stride].
 */
fun Bitmap.calibrate(stride: Int = 4): TargetCalibration? {
    val sw = (width + stride - 1) / stride
    val sh = (height + stride - 1) / stride
    if (sw < 8 || sh < 8) return null

    val gray = ByteArray(sw * sh)
    val row = IntArray(width)
    var dst = 0
    var srcY = 0
    for (y in 0 until sh) {
        getPixels(row, 0, width, 0, srcY, width, 1)
        var srcX = 0
        for (x in 0 until sw) {
            val p = row[srcX]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val luma = (0.299 * r + 0.587 * g + 0.114 * b).toInt().coerceIn(0, 255)
            gray[dst++] = luma.toByte()
            srcX += stride
            if (srcX >= width) srcX = width - 1
        }
        srcY += stride
        if (srcY >= height) srcY = height - 1
    }

    val small = calibrateFromGrayscale(gray, sw, sh) ?: return null
    val s = stride.toFloat()
    val cxFull = small.centerX * s
    val cyFull = small.centerY * s
    val rFull = small.radiusPx * s
    return TargetCalibration(
        centerX = cxFull,
        centerY = cyFull,
        radiusPx = rFull,
        mmPerPx = TARGET_BLACK_RING_RADIUS_MM / rFull,
        confidence = small.confidence,
    )
}
