package se.kjellstrand.webshooter.ui.navigation

import androidx.lifecycle.ViewModel
import se.kjellstrand.webshooter.data.SessionManager

class SessionViewModel(
    val sessionManager: SessionManager,
) : ViewModel()
