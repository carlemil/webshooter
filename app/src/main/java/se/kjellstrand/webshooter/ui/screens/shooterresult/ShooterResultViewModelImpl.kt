package se.kjellstrand.webshooter.ui.screens.shooterresult

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.ui.screens.results.ResultsViewModelImpl
import javax.inject.Inject

@HiltViewModel
class ShooterResultViewModelImpl @Inject constructor(
    private val resultsRepository: ResultsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ShooterResultViewModel {

    private val competitionId: Long = checkNotNull(savedStateHandle["competitionId"])
    private val shooterId: Long = checkNotNull(savedStateHandle["shooterId"])
    private val resultsType: ResultsType = try {
        ResultsType.valueOf(checkNotNull(savedStateHandle["resultsType"]))
    } catch (e: Exception) {
        ResultsType.FIELD
    }

    private val _uiState = MutableStateFlow(ShooterResultUiState(isLoading = true))
    override val uiState: StateFlow<ShooterResultUiState> = _uiState.asStateFlow()

    init {
        getShooterResults(competitionId, shooterId)
    }

    private fun getShooterResults(competitionId: Long, shooterId: Long) {
        viewModelScope.launch {
            resultsRepository.getShooterResults(competitionId, shooterId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val results = resource.data.results
                        val shooterName =
                            "${results.firstOrNull()?.signup?.user?.name} ${results.firstOrNull()?.signup?.user?.lastname}"
                                ?: ""
                        _uiState.value = ShooterResultUiState(
                            isLoading = false,
                            shooterName = shooterName,
                            results = results,
                            groupedResults = ResultsViewModelImpl.groupResults(results, resultsType),
                            resultsType = resultsType
                        )
                    }

                    is Resource.Error -> {
                        _uiState.value = ShooterResultUiState(
                            isLoading = false,
                            error = resource.error::class.simpleName
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}
