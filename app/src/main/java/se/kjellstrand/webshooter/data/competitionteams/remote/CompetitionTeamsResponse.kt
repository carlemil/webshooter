package se.kjellstrand.webshooter.data.competitionteams.remote

import com.google.gson.annotations.SerializedName

data class CompetitionTeamsResponse(
    val teams: List<TeamEntry>
)

data class TeamEntry(
    val id: Long,
    @SerializedName("competitions_id") val competitionsId: Long,
    @SerializedName("clubs_id") val clubsId: Long,
    val name: String,
    @SerializedName("weapongroups_id") val weapongroupsId: Long,
    val weapongroup: TeamWeaponGroup? = null,
    val signups: List<TeamSignupEntry>
)

data class TeamWeaponGroup(
    val id: Long,
    val name: String,
    val displayname: String
)

data class TeamSignupEntry(
    val id: Long,
    val lane: Int,
    @SerializedName("weaponclasses_id") val weaponclassesId: Long,
    val pivot: TeamPivot,
    val user: TeamSignupUser,
    val club: TeamSignupClub?
)

data class TeamPivot(
    @SerializedName("teams_id") val teamsId: Long,
    @SerializedName("signups_id") val signupsId: Long,
    val position: Int
)

data class TeamSignupUser(
    @SerializedName("user_id") val userId: Long,
    val name: String,
    val lastname: String,
    val fullname: String
)

data class TeamSignupClub(val name: String)
