package se.kjellstrand.webshooter.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import se.kjellstrand.webshooter.data.settings.remote.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionProvider @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    fun load() {
        scope.launch {
            settingsRepository.getUserProfile().collect { resource ->
                if (resource is Resource.Success) {
                    _userProfile.value = resource.data
                }
            }
        }
    }

    fun clear() {
        _userProfile.value = null
    }
}
