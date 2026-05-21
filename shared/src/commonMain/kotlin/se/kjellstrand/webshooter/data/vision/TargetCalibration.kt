package se.kjellstrand.webshooter.data.vision

import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sqrt

/** Black 7-ring outer radius in mm — used to convert pixels to mm. */
const val TARGET_BLACK_RING_RADIUS_MM: Double = 100.0

data class TargetCalibration(
    val centerX: Float,
    val centerY: Float,
    val radiusPx: Float,
    val mmPerPx: Double,
    val confidence: Float,
)

/**
 * Otsu → connected components → blob selection → perimeter → circle fit.
 * Coordinates are in the [gray] buffer's own pixel space; the Android
 * adapter scales them back to original Bitmap pixels.
 */
fun calibrateFromGrayscale(gray: ByteArray, width: Int, height: Int): TargetCalibration? {
    if (width <= 0 || height <= 0 || gray.size < width * height) return null

    val threshold = otsuThreshold(gray, width, height)
    if (threshold <= 0) return null

    val minAxis = min(width, height)
    val minArea = (PI * (0.05 * minAxis) * (0.05 * minAxis)).toInt().coerceAtLeast(16)
    val cc = findDarkBlobs(gray, width, height, threshold, minArea)
    if (cc.blobs.isEmpty()) return null

    val imageCx = width / 2f
    val imageCy = height / 2f
    val halfAxis = minAxis / 2f
    val maxRadius = 0.45f * minAxis
    val minRadius = 0.05f * minAxis

    // Score blobs by central position * solidity, with hard size gate.
    data class Scored(val blob: Blob, val score: Double, val radiusEstimate: Double, val solidity: Double)
    val candidates = cc.blobs.mapNotNull { b ->
        val rEst = sqrt(b.pixelCount.toDouble() / PI)
        if (rEst < minRadius || rEst > maxRadius) return@mapNotNull null
        val solidity = b.pixelCount.toDouble() / (PI * rEst * rEst)
        if (solidity < 0.6) return@mapNotNull null
        val dx = b.centroidX - imageCx
        val dy = b.centroidY - imageCy
        val dist = sqrt(dx * dx + dy * dy)
        val centralness = (1.0 - dist / halfAxis).coerceIn(0.0, 1.0)
        Scored(b, centralness * solidity, rEst, solidity)
    }
    val pick = candidates.maxByOrNull { it.score } ?: return null

    // Extract perimeter: pixels with the chosen label that have at least
    // one 4-neighbour with a different label (or are at the image edge).
    val labelTarget = pick.blob.label
    val labels = cc.labels
    val perimeter = ArrayList<IntArray>()
    for (y in pick.blob.minY..pick.blob.maxY) {
        for (x in pick.blob.minX..pick.blob.maxX) {
            val idx = y * width + x
            if (labels[idx] != labelTarget) continue
            val n = if (y == 0) 0 else labels[idx - width]
            val s = if (y == height - 1) 0 else labels[idx + width]
            val w = if (x == 0) 0 else labels[idx - 1]
            val e = if (x == width - 1) 0 else labels[idx + 1]
            if (n != labelTarget || s != labelTarget || w != labelTarget || e != labelTarget) {
                perimeter += intArrayOf(x, y)
            }
        }
    }
    if (perimeter.size < 8) return null

    val fit = fitCircle(perimeter) ?: return null
    if (fit.r < minRadius || fit.r > maxRadius) return null

    val rmsToRadius = (fit.rmsError / fit.r).coerceIn(0f, 1f)
    val confidence = (pick.solidity.toFloat() * (1f - rmsToRadius)).coerceIn(0f, 1f)

    return TargetCalibration(
        centerX = fit.cx,
        centerY = fit.cy,
        radiusPx = fit.r,
        mmPerPx = TARGET_BLACK_RING_RADIUS_MM / fit.r,
        confidence = confidence,
    )
}
