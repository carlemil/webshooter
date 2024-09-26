package se.kjellstrand.webshooter.ui.common

sealed class UiEvent {
    object NavigateToLandingPage : UiEvent()
    data class ShowErrorMessage(val message: String) : UiEvent()
}