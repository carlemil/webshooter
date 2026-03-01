package se.kjellstrand.webshooter.ui.competitionsignups

import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupEntry

enum class SignupsListSortField { Name, Club, WeaponGroup }
enum class SortDirection { Ascending, Descending }

data class CompetitionSignupsUiState(
    val allSignups: List<CompetitionSignupEntry> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val total: Int = 0,
    val isLoading: Boolean = false,
    val filterClub: String? = null,
    val filterWeaponGroup: String? = null,
    val sortField: SignupsListSortField = SignupsListSortField.Name,
    val sortDirection: SortDirection = SortDirection.Ascending
) {
    val availableClubs: List<String>
        get() = allSignups.map { it.club.name }.distinct().sorted()

    val availableWeaponGroups: List<String>
        get() = allSignups.map { it.weaponclass.classnameGeneral }.distinct().sorted()

    val filteredAndSorted: List<CompetitionSignupEntry>
        get() {
            var result = allSignups
            if (filterClub != null) {
                result = result.filter { it.club.name == filterClub }
            }
            if (filterWeaponGroup != null) {
                result = result.filter { it.weaponclass.classnameGeneral == filterWeaponGroup }
            }
            val comparator: Comparator<CompetitionSignupEntry> = when (sortField) {
                SignupsListSortField.Name -> compareBy { "${it.user.name} ${it.user.lastname}" }
                SignupsListSortField.Club -> compareBy { it.club.name }
                SignupsListSortField.WeaponGroup -> compareBy { it.weaponclass.classnameGeneral }
            }
            return if (sortDirection == SortDirection.Ascending) {
                result.sortedWith(comparator)
            } else {
                result.sortedWith(comparator.reversed())
            }
        }
}
