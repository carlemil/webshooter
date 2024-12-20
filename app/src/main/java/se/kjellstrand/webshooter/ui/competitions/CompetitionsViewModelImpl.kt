package se.kjellstrand.webshooter.ui.competitions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import javax.inject.Inject

@HiltViewModel
class CompetitionsViewModelImpl @Inject constructor(
    private val competitionsRepository: CompetitionsRepository
) : ViewModel(), CompetitionsViewModel {

    private val _uiState = MutableStateFlow(CompetitionsUiState())
    override val uiState: StateFlow<CompetitionsUiState> = _uiState.asStateFlow()

    init {
        getCompetitions()
        _uiState.value = CompetitionsUiState()
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
                        println("getCompetitions resource: $resource")
                    }
                }
            }
        }
    }

    override fun getCompetitionById(competitionId: Long): Datum? {
        return uiState.value.competitions?.data?.find { it.id == competitionId }
    }
}