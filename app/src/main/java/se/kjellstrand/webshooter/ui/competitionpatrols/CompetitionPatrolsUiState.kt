package se.kjellstrand.webshooter.ui.competitionpatrols

import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolEntry
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolSignupEntry

enum class PatrolsSortField { Name, Club, WeaponGroup }
enum class PatrolsSortDirection { Ascending, Descending }

data class CompetitionPatrolsUiState(
    val patrols: List<PatrolEntry> = emptyList(),
    val isLoading: Boolean = false,
    val filterClub: String? = null,
    val filterWeaponGroup: String? = null,
    val sortField: PatrolsSortField = PatrolsSortField.Name,
    val sortDirection: PatrolsSortDirection = PatrolsSortDirection.Ascending
) {
    val availableClubs: List<String>
        get() = patrols.flatMap { it.signups }.map { it.club.name }.distinct().sorted()

    val availableWeaponGroups: List<String>
        get() = patrols.flatMap { it.signups }.map { it.weaponclass.classnameGeneral }.distinct().sorted()

    val filteredPatrols: List<PatrolEntry>
        get() {
            val comparator: Comparator<PatrolSignupEntry> = when (sortField) {
                PatrolsSortField.Name -> compareBy { "${it.user.name} ${it.user.lastname}" }
                PatrolsSortField.Club -> compareBy { it.club.name }
                PatrolsSortField.WeaponGroup -> compareBy { it.weaponclass.classnameGeneral }
            }
            return patrols.map { patrol ->
                var sigs = patrol.signups
                if (filterClub != null) sigs = sigs.filter { it.club.name == filterClub }
                if (filterWeaponGroup != null) sigs = sigs.filter { it.weaponclass.classnameGeneral == filterWeaponGroup }
                val sorted = if (sortDirection == PatrolsSortDirection.Ascending) {
                    sigs.sortedWith(comparator)
                } else {
                    sigs.sortedWith(comparator.reversed())
                }
                patrol.copy(signups = sorted)
            }.filter { it.signups.isNotEmpty() }
        }
}
