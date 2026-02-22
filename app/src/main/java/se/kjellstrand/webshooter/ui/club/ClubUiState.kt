package se.kjellstrand.webshooter.ui.club

import se.kjellstrand.webshooter.data.club.remote.ClubMember
import se.kjellstrand.webshooter.data.common.Club

enum class ClubTab { INFORMATION, ADMINS, USERS }

data class ClubUiState(
    val selectedTab: ClubTab = ClubTab.INFORMATION,
    val clubInfo: Club? = null,
    val admins: List<ClubMember> = emptyList(),
    val users: List<ClubMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
