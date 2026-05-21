package se.kjellstrand.webshooter.ui.screens.markera

import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.data.vision.Detection
import se.kjellstrand.webshooter.data.vision.TargetCalibration

interface MarkeraViewModel {
    val uiState: StateFlow<MarkeraUiState>

    /** Push the result of one CameraX frame's analysis. */
    fun onFrameAnalysed(detections: List<Detection>, imageWidth: Int, imageHeight: Int)

    /** Push the result of a single gallery-pick inference. */
    fun onGalleryImageAnalysed(detections: List<Detection>, imageWidth: Int, imageHeight: Int)

    /**
     * Update the cached calibration. A non-null value replaces the
     * stored one; a null value keeps the last good calibration so the
     * overlay and scoring don't snap back to defaults the moment a
     * single noisy frame fails to detect the 7-ring.
     */
    fun setCalibration(fresh: TargetCalibration?)

    fun switchMode(mode: MarkeraMode)

    fun setProcessing(isProcessing: Boolean)

    fun setError(message: String?)
}
