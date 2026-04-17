package se.kjellstrand.webshooter.ui.common

import android.annotation.SuppressLint
import android.view.MotionEvent
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.XAxis

@SuppressLint("ClickableViewAccessibility")
fun <T : BarLineChartBase<*>> T.applyBaseChartStyle(onSurfaceColor: Int): T {
    description.isEnabled = false
    setNoDataText("")
    setPinchZoom(true)
    isDragEnabled = true
    setScaleEnabled(true)
    isDoubleTapToZoomEnabled = true
    setExtraBottomOffset(16f)

    // Prevent the Compose host from intercepting multi-touch / drag
    // gestures before MPAndroidChart sees them.
    setOnTouchListener { v, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_MOVE ->
                v.parent?.requestDisallowInterceptTouchEvent(true)

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL ->
                v.parent?.requestDisallowInterceptTouchEvent(false)
        }
        false
    }

    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.granularity = 1f
    xAxis.textColor = onSurfaceColor
    axisLeft.textColor = onSurfaceColor
    axisRight.isEnabled = false
    return this
}
