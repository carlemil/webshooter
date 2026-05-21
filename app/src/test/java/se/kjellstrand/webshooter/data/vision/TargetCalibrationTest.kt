package se.kjellstrand.webshooter.data.vision

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetCalibrationTest {

    /** Build a grayscale buffer with one dark disc on a white background. */
    private fun discBuffer(
        w: Int,
        h: Int,
        cx: Int,
        cy: Int,
        r: Int,
        discValue: Int = 30,
        bgValue: Int = 220,
        extra: (ByteArray, Int) -> Unit = { _, _ -> },
    ): ByteArray {
        val buf = ByteArray(w * h) { bgValue.toByte() }
        val r2 = r * r
        for (y in 0 until h) {
            for (x in 0 until w) {
                val dx = x - cx
                val dy = y - cy
                if (dx * dx + dy * dy <= r2) buf[y * w + x] = discValue.toByte()
            }
        }
        extra(buf, w)
        return buf
    }

    @Test
    fun `centred disc recovers centre and scale`() {
        val buf = discBuffer(w = 400, h = 400, cx = 200, cy = 200, r = 50)
        val c = calibrateFromGrayscale(buf, 400, 400)
        assertNotNull(c)
        c!!
        assertEquals(200f, c.centerX, 1.5f)
        assertEquals(200f, c.centerY, 1.5f)
        assertEquals(50f, c.radiusPx, 2f)
        // mmPerPx = 100 / r → ~2.0
        assertEquals(2.0, c.mmPerPx, 0.1)
        assertTrue("confidence should be high for a clean disc", c.confidence > 0.6f)
    }

    @Test
    fun `off-centre disc still recovers centre and scale`() {
        val buf = discBuffer(w = 400, h = 400, cx = 120, cy = 280, r = 40)
        val c = calibrateFromGrayscale(buf, 400, 400)
        assertNotNull(c)
        c!!
        assertEquals(120f, c.centerX, 2f)
        assertEquals(280f, c.centerY, 2f)
        assertEquals(40f, c.radiusPx, 2f)
        assertEquals(2.5, c.mmPerPx, 0.15)
    }

    @Test
    fun `tiny speck near the disc does not pull the centre`() {
        val buf = discBuffer(w = 400, h = 400, cx = 200, cy = 200, r = 50) { b, w ->
            // 3x3 dark speck in the corner — below minArea + size gate
            for (y in 5..7) for (x in 5..7) b[y * w + x] = 30.toByte()
        }
        val c = calibrateFromGrayscale(buf, 400, 400)
        assertNotNull(c)
        c!!
        assertEquals(200f, c.centerX, 1.5f)
        assertEquals(200f, c.centerY, 1.5f)
    }

    @Test
    fun `all-white buffer returns null`() {
        val buf = ByteArray(400 * 400) { 240.toByte() }
        assertNull(calibrateFromGrayscale(buf, 400, 400))
    }

    @Test
    fun `tiny disc fails the size gate and returns null`() {
        val buf = discBuffer(w = 400, h = 400, cx = 200, cy = 200, r = 3)
        assertNull(calibrateFromGrayscale(buf, 400, 400))
    }
}
