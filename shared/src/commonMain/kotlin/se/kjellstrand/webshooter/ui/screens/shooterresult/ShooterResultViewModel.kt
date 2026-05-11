package se.kjellstrand.webshooter.ui.screens.shooterresult

import kotlinx.coroutines.flow.StateFlow

interface ShooterResultViewModel {
    val uiState: StateFlow<ShooterResultUiState>
}
