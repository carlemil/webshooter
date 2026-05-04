package se.kjellstrand.webshooter.data.competitionpatrols.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompetitionPatrolsResponse(
    val patrols: List<PatrolEntry>
)

@Serializable
data class PatrolEntry(
    val id: Long,
    val sortorder: Int,
    @SerialName("start_time_human") val startTimeHuman: String,
    @SerialName("end_time_human") val endTimeHuman: String,
    val signups: List<PatrolSignupEntry>
)

@Serializable
data class PatrolSignupEntry(
    val id: Long,
    val lane: Int,
    val user: PatrolSignupUser,
    val club: PatrolSignupClub,
    val weaponclass: PatrolSignupWeaponClass
)

@Serializable
data class PatrolSignupUser(
    @SerialName("user_id") val userId: Long,
    val name: String,
    val lastname: String
)

@Serializable
data class PatrolSignupClub(val name: String)

@Serializable
data class PatrolSignupWeaponClass(
    val classname: String,
    @SerialName("classname_general") val classnameGeneral: String
)
