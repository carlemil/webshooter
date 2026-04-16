package se.kjellstrand.webshooter.ui.screens.seriespoints

import se.kjellstrand.webshooter.data.charts.Participant
import se.kjellstrand.webshooter.data.club.remote.ClubMember
import se.kjellstrand.webshooter.data.seriespoints.CompetitionSeries

enum class WeaponClassGroup(val prefix: Char) {
    A('A'), B('B'), C('C');

    fun matches(weaponClass: String): Boolean =
        weaponClass.isNotEmpty() && weaponClass[0].equals(prefix, ignoreCase = true)
}

data class SeriesPointsUiState(
    val currentUserId: Long = 0L,
    val selectedUserId: Long = 0L,
    val selectedUserName: String = "",
    val competitions: List<CompetitionSeries> = emptyList(),
    val selectedGroup: WeaponClassGroup? = null,
    val availableGroups: Set<WeaponClassGroup> = emptySet(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val clubMembers: List<ClubMember> = emptyList(),
    val allParticipants: List<Participant> = emptyList(),
    val searchQuery: String = "",
    val showSearchDialog: Boolean = false
) {
    val filteredCompetitions: List<CompetitionSeries>
        get() = selectedGroup?.let { group ->
            competitions.filter { group.matches(it.weaponClass) }
        } ?: competitions

    val precisionClubMembers: List<ClubMember>
        get() {
            val participantIds = allParticipants.map { it.userId }.toSet()
            return clubMembers.filter { it.userId in participantIds }
        }
}
