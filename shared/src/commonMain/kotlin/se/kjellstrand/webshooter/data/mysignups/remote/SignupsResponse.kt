package se.kjellstrand.webshooter.data.mysignups.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignupsResponse(
    @SerialName("grouped_signups") val groupedSignups: Map<String, SignupGroup>
)

@Serializable
data class SignupGroup(
    val signups: List<SignupEntry>
)

@Serializable
data class SignupEntry(
    val id: Long,

    @SerialName("competitions_id") val competitionsId: Long,
    @SerialName("weaponclasses_id") val weaponClassesId: Long,
    @SerialName("patrols_id") val patrolsId: Long,

    @SerialName("start_time") val startTime: String?,
    @SerialName("end_time") val endTime: String?,

    val lane: Long,
    val note: String?,

    @SerialName("registration_fee") val registrationFee: Long,
    @SerialName("special_wishes") val specialWishes: String,

    @SerialName("start_time_human") val startTimeHuman: String,
    @SerialName("end_time_human") val endTimeHuman: String,

    val competition: SignupCompetition,
    val weaponclass: SignupWeaponClass,
    val patrol: SignupPatrol?,

    @SerialName("results_placements") val resultsPlacements: SignupResultsPlacement?
)

@Serializable
data class SignupCompetition(
    val id: Long,
    val name: String,
    val date: String,
    val status: String,

    @SerialName("status_human") val statusHuman: String,
    @SerialName("results_type_human") val resultsTypeHuman: String
)

@Serializable
data class SignupWeaponClass(
    val classname: String,

    @SerialName("classname_general") val classnameGeneral: String
)

@Serializable
data class SignupPatrol(
    val id: Long,

    @SerialName("start_time_human") val startTimeHuman: String,
    @SerialName("end_time_human") val endTimeHuman: String
)

@Serializable
data class SignupResultsPlacement(
    val id: Long,

    @SerialName("std_medal") val stdMedal: String?,
    val points: Long
)
