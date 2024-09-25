package se.kjellstrand.webshooter.ui

sealed class UiEvent {
    object NavigateToLandingPage : UiEvent()
    data class ShowErrorMessage(val message: String) : UiEvent()
}