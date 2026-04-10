package se.kjellstrand.webshooter.data.competitionpatrols.remote

import com.google.gson.annotations.SerializedName

data class CompetitionPatrolsResponse(
    val patrols: List<PatrolEntry>
)

data class PatrolEntry(
    val id: Long,
    val sortorder: Int,
    @SerializedName("start_time_human") val startTimeHuman: String,
    @SerializedName("end_time_human") val endTimeHuman: String,
    val signups: List<PatrolSignupEntry>
)

data class PatrolSignupEntry(
    val id: Long,
    val lane: Int,
    val user: PatrolSignupUser,
    val club: PatrolSignupClub,
    val weaponclass: PatrolSignupWeaponClass
)

data class PatrolSignupUser(
    @SerializedName("user_id") val userId: Long,
    val name: String,
    val lastname: String
)

data class PatrolSignupClub(val name: String)

data class PatrolSignupWeaponClass(
    val classname: String,
    @SerializedName("classname_general") val classnameGeneral: String
)
