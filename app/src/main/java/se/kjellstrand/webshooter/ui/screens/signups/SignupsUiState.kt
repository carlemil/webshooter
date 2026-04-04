package se.kjellstrand.webshooter.ui.screens.signups

import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupEntry

enum class SignupsListSortField { Name, Club, WeaponGroup }
enum class SortDirection { Ascending, Descending }

data class CompetitionSignupsUiState(
    val allSignups: List<CompetitionSignupEntry> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val total: Int = 0,
    val isLoading: Boolean = false,
    val filterClubs: Set<String> = emptySet(),
    val filterWeaponGroups: Set<String> = emptySet(),
    val sortField: SignupsListSortField = SignupsListSortField.Name,
    val sortDirection: SortDirection = SortDirection.Ascending,
    val currentUserFullName: String? = null,
    val currentUserClubName: String? = null
) {
    val uniquePersonCount: Int
        get() = allSignups.map { "${it.user?.name} ${it.user?.lastname}" }.distinct().size

    val totalSignupsCount: Int
        get() = allSignups.size

    val availableClubs: List<String>
        get() = allSignups.mapNotNull { it.club?.name }.distinct().sorted()

    val availableWeaponGroups: List<String>
        get() = allSignups.mapNotNull { it.weaponclass?.classnameGeneral }.distinct().sorted()

    val filteredAndSorted: List<CompetitionSignupEntry>
        get() {
            var result = allSignups
            if (filterClubs.isNotEmpty()) {
                result = result.filter { it.club?.name in filterClubs }
            }
            if (filterWeaponGroups.isNotEmpty()) {
                result = result.filter { it.weaponclass?.classnameGeneral in filterWeaponGroups }
            }
            val comparator: Comparator<CompetitionSignupEntry> = when (sortField) {
                SignupsListSortField.Name -> compareBy { "${it.user?.name} ${it.user?.lastname}" }
                SignupsListSortField.Club -> compareBy { it.club?.name }
                SignupsListSortField.WeaponGroup -> compareBy { it.weaponclass?.classnameGeneral }
            }
            return if (sortDirection == SortDirection.Ascending) {
                result.sortedWith(comparator)
            } else {
                result.sortedWith(comparator.reversed())
            }
        }
}
