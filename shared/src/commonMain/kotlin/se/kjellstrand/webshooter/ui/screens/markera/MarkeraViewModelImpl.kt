package se.kjellstrand.webshooter.ui.screens.markera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import se.kjellstrand.webshooter.data.vision.Detection
import se.kjellstrand.webshooter.data.vision.TargetCalibration

class MarkeraViewModelImpl : ViewModel(), MarkeraViewModel {

    private val _uiState = MutableStateFlow(MarkeraUiState())
    override val uiState: StateFlow<MarkeraUiState> = _uiState.asStateFlow()

    override fun onFrameAnalysed(detections: List<Detection>, imageWidth: Int, imageHeight: Int) {
        _uiState.update {
            it.copy(
                detections = detections,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                isProcessing = false,
                error = null,
            )
        }
    }

    override fun setCalibration(fresh: TargetCalibration?) {
        if (fresh == null) return
        _uiState.update { it.copy(calibration = fresh) }
    }

    override fun clearResults() {
        _uiState.update {
            it.copy(
                detections = emptyList(),
                imageWidth = 0,
                imageHeight = 0,
                isProcessing = false,
                error = null,
            )
        }
    }

    override fun setProcessing(isProcessing: Boolean) {
        _uiState.update { it.copy(isProcessing = isProcessing) }
    }

    override fun setError(message: String?) {
        _uiState.update { it.copy(error = message, isProcessing = false) }
    }
}
