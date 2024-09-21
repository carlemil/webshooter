package se.kjellstrand.webshooter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.Resource
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import javax.inject.Inject

@HiltViewModel
class CompetitionsViewModel @Inject constructor(
    private val competitionsRepository: CompetitionsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetitionsUiState())
    val uiState: StateFlow<CompetitionsUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = CompetitionsUiState()
        getCompetitions()
    }

    private fun getCompetitions() {
        viewModelScope.launch {
            competitionsRepository.get().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        println("getCompetitions Success: ${resource.data}")
                        _uiState.value = CompetitionsUiState(resource.data.competitions)
                    }

                    is Resource.Error -> {
                        println("getCompetitions Failed: ${resource.error}")
                    }

                    else -> {
                        // Handle other states
                        println("getCompetitions other state")
                    }
                }
            }
        }
    }

    fun getCompetitionById(competitionId: Long): Datum? {
        return uiState.value.competitions?.data?.find { it.id == competitionId }
    }
}