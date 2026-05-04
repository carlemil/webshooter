package se.kjellstrand.webshooter.data.charts

import org.junit.Test
import org.junit.Assert.*

class MpAndroidChartDependencyTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `MPAndroidChart LineChart class is available on classpath`() {
        val clazz = Class.forName("com.github.mikephil.charting.charts.LineChart")
        assertNotNull(clazz)
    }

    @Test
    fun `MPAndroidChart LineDataSet class is available on classpath`() {
        val clazz = Class.forName("com.github.mikephil.charting.data.LineDataSet")
        assertNotNull(clazz)
    }

    @Test
    fun `MPAndroidChart Entry class is available on classpath`() {
        val clazz = Class.forName("com.github.mikephil.charting.data.Entry")
        assertNotNull(clazz)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `Ktor client dependency is available`() {
        val clazz = Class.forName("io.ktor.client.HttpClient")
        assertNotNull(clazz)
    }

    @Test
    fun `OkHttp dependency is available`() {
        val clazz = Class.forName("okhttp3.OkHttpClient")
        assertNotNull(clazz)
    }
}
