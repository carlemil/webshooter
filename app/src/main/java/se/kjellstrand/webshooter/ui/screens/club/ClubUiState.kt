package se.kjellstrand.webshooter.ui.screens.club

import se.kjellstrand.webshooter.data.club.remote.ClubData
import se.kjellstrand.webshooter.data.club.remote.ClubMember

enum class ClubTab { INFORMATION, ADMINS, USERS }

data class ClubUiState(
    val selectedTab: ClubTab = ClubTab.INFORMATION,
    val clubData: ClubData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val admins: List<ClubMember> get() {
        val emailByUserId = (clubData?.users ?: emptyList())
            .mapNotNull { u -> u.email?.takeIf { it.isNotBlank() }?.let { u.userId to it } }
            .toMap()
        return (clubData?.admins ?: emptyList())
            .map { admin -> if (admin.email.isNullOrBlank()) admin.copy(email = emailByUserId[admin.userId]) else admin }
            .sortedWith(compareBy({ it.name }, { it.lastname }))
    }
    val users: List<ClubMember> get() = (clubData?.users ?: emptyList())
        .sortedWith(compareBy({ it.name }, { it.lastname }))
}
