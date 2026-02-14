package se.kjellstrand.webshooter.ui.shooterresult

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.results.ResultsRepository
import javax.inject.Inject

@HiltViewModel
class ShooterResultViewModel @Inject constructor(
    private val resultsRepository: ResultsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val competitionId: Int = checkNotNull(savedStateHandle["competitionId"])
    private val shooterId: Int = checkNotNull(savedStateHandle["shooterId"])

    private val _uiState = MutableStateFlow(ShooterResultUiState(isLoading = true))
    val uiState: StateFlow<ShooterResultUiState> = _uiState.asStateFlow()

    init {
        Log.d("ShooterResultViewModel", "competitionId: $competitionId, shooterId: $shooterId")
        getShooterResults(competitionId, shooterId)
    }

    private fun getShooterResults(competitionId: Int, shooterId: Int) {
        viewModelScope.launch {
            resultsRepository.getShooterResults(competitionId, shooterId).collect { resource ->
                Log.d("ShooterResultViewModel", "resource: $resource")
                when (resource) {
                    is Resource.Success -> {
                        val results = resource.data.results
                        val shooterName = results.firstOrNull()?.signup?.user?.name ?: ""
                        _uiState.value = ShooterResultUiState(
                            isLoading = false,
                            shooterName = shooterName,
                            results = results
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = ShooterResultUiState(
                            isLoading = false,
                            error = resource.error.name
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}