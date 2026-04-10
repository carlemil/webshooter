package se.kjellstrand.webshooter.data.competitionteams.remote

import com.google.gson.annotations.SerializedName

data class CompetitionTeamsResponse(
    val teams: List<TeamEntry>
)

data class TeamEntry(
    val id: Long,
    val name: String,
    val weapongroup: TeamWeaponGroup? = null,
    val signups: List<TeamSignupEntry>
)

data class TeamWeaponGroup(
    val id: Long,
    val name: String
)

data class TeamSignupEntry(
    val id: Long,
    val pivot: TeamPivot,
    val user: TeamSignupUser,
    val club: TeamSignupClub?
)

data class TeamPivot(
    val position: Int
)

data class TeamSignupUser(
    @SerializedName("user_id") val userId: Long,
    val name: String,
    val lastname: String
)

data class TeamSignupClub(val name: String)
