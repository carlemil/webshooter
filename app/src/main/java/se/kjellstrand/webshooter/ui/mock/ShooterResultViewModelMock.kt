package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultUiState
import se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultViewModel

class ShooterResultViewModelMock(
    initialState: ShooterResultUiState = ShooterResultUiState(isLoading = false, shooterName = "Erik Svensson")
) : ShooterResultViewModel {
    override val uiState: StateFlow<ShooterResultUiState> = MutableStateFlow(initialState)
}
