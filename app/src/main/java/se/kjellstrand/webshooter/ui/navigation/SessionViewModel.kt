package se.kjellstrand.webshooter.ui.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import se.kjellstrand.webshooter.data.SessionManager
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    val sessionManager: SessionManager
) : ViewModel()
