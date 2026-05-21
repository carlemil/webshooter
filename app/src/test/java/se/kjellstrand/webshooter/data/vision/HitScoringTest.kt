package se.kjellstrand.webshooter.data.vision

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HitScoringTest {

    /** Detection at the given horizontal offset from `(centerX, centerY)`. */
    private fun atOffset(centerX: Float, centerY: Float, dx: Float, dy: Float = 0f): Detection {
        val cx = centerX + dx
        val cy = centerY + dy
        return Detection(left = cx - 2f, top = cy - 2f, right = cx + 2f, bottom = cy + 2f, conf = 0.9f)
    }

    @Test
    fun `dead-centre hit scores ring 10 with inner-X`() {
        val s = computeHitScores(listOf(atOffset(200f, 200f, 0f)), 200f, 200f, mmPerPx = 1.0).single()
        assertEquals(10, s.ring)
        assertTrue(s.isInnerTen)
        assertEquals(0.0, s.distanceMm, 1e-3)
    }

    @Test
    fun `12_5mm boundary still counts as inner-X`() {
        val s = computeHitScores(listOf(atOffset(200f, 200f, 12.5f)), 200f, 200f, mmPerPx = 1.0).single()
        assertEquals(10, s.ring)
        assertTrue(s.isInnerTen)
    }

    @Test
    fun `just past inner-X is still ring 10 but not X`() {
        val s = computeHitScores(listOf(atOffset(200f, 200f, 12.6f)), 200f, 200f, mmPerPx = 1.0).single()
        assertEquals(10, s.ring)
        assertFalse(s.isInnerTen)
    }

    @Test
    fun `25mm boundary is ring 10`() {
        val s = computeHitScores(listOf(atOffset(200f, 200f, 25f)), 200f, 200f, mmPerPx = 1.0).single()
        assertEquals(10, s.ring)
        assertFalse(s.isInnerTen)
    }

    @Test
    fun `25_01mm drops to ring 9`() {
        val s = computeHitScores(listOf(atOffset(200f, 200f, 25.01f)), 200f, 200f, mmPerPx = 1.0).single()
        assertEquals(9, s.ring)
    }

    @Test
    fun `100mm boundary is ring 7`() {
        val s = computeHitScores(listOf(atOffset(200f, 200f, 100f)), 200f, 200f, mmPerPx = 1.0).single()
        assertEquals(7, s.ring)
    }

    @Test
    fun `250mm boundary is ring 1`() {
        val s = computeHitScores(listOf(atOffset(400f, 400f, 250f)), 400f, 400f, mmPerPx = 1.0).single()
        assertEquals(1, s.ring)
    }

    @Test
    fun `past 250mm is a miss`() {
        val s = computeHitScores(listOf(atOffset(400f, 400f, 250.01f)), 400f, 400f, mmPerPx = 1.0).single()
        assertEquals(0, s.ring)
    }

    @Test
    fun `bbox centre is the midpoint of left-top-right-bottom`() {
        val d = Detection(left = 100f, top = 200f, right = 200f, bottom = 300f, conf = 0.9f)
        // Centre at (200, 200); bbox centre at (150, 250); dx=-50, dy=50; dist≈70.71
        val s = computeHitScores(listOf(d), 200f, 200f, mmPerPx = 1.0).single()
        assertEquals(150f, s.centerXpx, 1e-3f)
        assertEquals(250f, s.centerYpx, 1e-3f)
        assertEquals(70.710678, s.distanceMm, 1e-3)
    }

    @Test
    fun `non-unit mmPerPx scales distance`() {
        // 50px offset, scale 2mm/px → 100mm → ring 7
        val s = computeHitScores(listOf(atOffset(200f, 200f, 50f)), 200f, 200f, mmPerPx = 2.0).single()
        assertEquals(7, s.ring)
        assertEquals(100.0, s.distanceMm, 1e-3)
    }

    @Test
    fun `off-centre target centre is honoured`() {
        // Detection at (300, 200), target centre at (250, 200) → 50mm at 1mm/px → ring 9
        val d = Detection(left = 298f, top = 198f, right = 302f, bottom = 202f, conf = 0.9f)
        val s = computeHitScores(listOf(d), 250f, 200f, mmPerPx = 1.0).single()
        assertEquals(50.0, s.distanceMm, 1e-3)
        assertEquals(9, s.ring)
    }

    @Test
    fun `empty input returns empty list`() {
        assertTrue(computeHitScores(emptyList(), 200f, 200f, 1.0).isEmpty())
    }

    @Test
    fun `results are sorted highest-score first`() {
        val low = atOffset(400f, 400f, 240f)   // ring 1
        val high = atOffset(400f, 400f, 5f)    // inner X
        val mid = atOffset(400f, 400f, 60f)    // ring 8
        val scores = computeHitScores(listOf(low, high, mid), 400f, 400f, mmPerPx = 1.0)
        assertEquals(listOf(10, 8, 1), scores.map { it.ring })
        assertTrue(scores.first().isInnerTen)
    }
}
