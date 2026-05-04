package se.kjellstrand.webshooter.data.competitionteams.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompetitionTeamsResponse(
    val teams: List<TeamEntry>
)

@Serializable
data class TeamEntry(
    val id: Long,
    val name: String,
    val weapongroup: TeamWeaponGroup? = null,
    val signups: List<TeamSignupEntry>
)

@Serializable
data class TeamWeaponGroup(
    val id: Long,
    val name: String
)

@Serializable
data class TeamSignupEntry(
    val id: Long,
    val pivot: TeamPivot,
    val user: TeamSignupUser,
    val club: TeamSignupClub?
)

@Serializable
data class TeamPivot(
    val position: Int
)

@Serializable
data class TeamSignupUser(
    @SerialName("user_id") val userId: Long,
    val name: String,
    val lastname: String
)

@Serializable
data class TeamSignupClub(val name: String)
