package se.kjellstrand.webshooter.ui.screens.competitions

import io.github.aakira.napier.Napier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.data.competitions.remote.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.Datum

class CompetitionsViewModelImpl(
    private val competitionsRepository: CompetitionsRepository,
) : ViewModel(), CompetitionsViewModel {

    private val _uiState = MutableStateFlow(CompetitionsUiState(isLoading = true))
    override val uiState: StateFlow<CompetitionsUiState> = _uiState.asStateFlow()

    @kotlin.concurrent.Volatile private var syncTriggered = false
    @kotlin.concurrent.Volatile private var hasBeenOpenedOnce = false

    init {
        viewModelScope.launch {
            competitionsRepository.observeAll().collect { data ->
                if (data.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        competitions = Competitions(
                            currentPage = 1,
                            data = data,
                            lastPage = 1,
                            total = data.size.toLong(),
                            status = "",
                            competitionTypes = emptyList()
                        ),
                        isLoading = false,
                        hasError = false
                    )
                } else if (!syncTriggered) {
                    syncTriggered = true
                    _uiState.value = _uiState.value.copy(
                        competitions = Competitions(
                            currentPage = 1,
                            data = data,
                            lastPage = 1,
                            total = 0L,
                            status = "",
                            competitionTypes = emptyList()
                        ),
                        isLoading = true,
                        hasError = false
                    )
                    launch {
                        try {
                            competitionsRepository.syncAll()
                            if (_uiState.value.competitions?.data?.isEmpty() == true) {
                                _uiState.value = _uiState.value.copy(isLoading = false)
                            }
                        } catch (e: Exception) {
                            Napier.w("Self-sync failed", e, TAG)
                            if (_uiState.value.competitions?.data?.isEmpty() == true) {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    hasError = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onScreenOpened() {
        if (!hasBeenOpenedOnce) {
            hasBeenOpenedOnce = true
            return
        }
        if (_uiState.value.isLoading) return
        reload()
    }

    override fun reload() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, hasError = false)
            try {
                competitionsRepository.syncAll()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasError = _uiState.value.competitions?.data.isNullOrEmpty()
                )
            }
        }
    }

    override fun getCompetitionById(competitionId: Long): Datum? {
        return uiState.value.competitions?.data?.find { it.id == competitionId }
    }

    override fun setSelectedStatuses(statuses: Set<String>) {
        _uiState.value = _uiState.value.copy(selectedStatuses = statuses)
    }

    companion object {
        private const val TAG = "CompetitionsViewModelImpl"
    }
}
