package se.kjellstrand.webshooter.ui.screens.markera

import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.data.vision.Detection
import se.kjellstrand.webshooter.data.vision.TargetCalibration

interface MarkeraViewModel {
    val uiState: StateFlow<MarkeraUiState>

    /** Push the result of one inference pass on a frozen snapshot. */
    fun onFrameAnalysed(detections: List<Detection>, imageWidth: Int, imageHeight: Int)

    /**
     * Update the cached calibration. A non-null value replaces the
     * stored one; a null value keeps the last good calibration so the
     * overlay and scoring don't snap back to defaults the moment a
     * single noisy frame fails to detect the 7-ring.
     */
    fun setCalibration(fresh: TargetCalibration?)

    /** Clear detection overlay state (preserves cached calibration). */
    fun clearResults()

    fun setProcessing(isProcessing: Boolean)

    fun setError(message: String?)
}
