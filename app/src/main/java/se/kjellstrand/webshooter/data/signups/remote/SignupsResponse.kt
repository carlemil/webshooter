package se.kjellstrand.webshooter.data.signups.remote

import com.google.gson.annotations.SerializedName

data class SignupsResponse(
    @SerializedName("grouped_signups") val groupedSignups: Map<String, SignupGroup>
)

data class SignupGroup(
    val signups: List<SignupEntry>
)

data class SignupEntry(
    val id: Long,

    @SerializedName("competitions_id") val competitionsId: Long,
    @SerializedName("weaponclasses_id") val weaponClassesId: Long,
    @SerializedName("patrols_id") val patrolsId: Long,

    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?,

    val lane: Long,
    val note: String?,

    @SerializedName("registration_fee") val registrationFee: Long,
    @SerializedName("special_wishes") val specialWishes: String,

    @SerializedName("start_time_human") val startTimeHuman: String,
    @SerializedName("end_time_human") val endTimeHuman: String,

    val competition: SignupCompetition,
    val weaponclass: SignupWeaponClass,
    val patrol: SignupPatrol?
)

data class SignupCompetition(
    val id: Long,
    val name: String,
    val date: String,
    val status: String,

    @SerializedName("status_human") val statusHuman: String,
    @SerializedName("contact_name") val contactName: String,
    @SerializedName("contact_city") val contactCity: String,
    @SerializedName("results_type") val resultsType: String,
    @SerializedName("results_type_human") val resultsTypeHuman: String
)

data class SignupWeaponClass(
    val id: Long,
    val classname: String,

    @SerializedName("classname_general") val classnameGeneral: String
)

data class SignupPatrol(
    val id: Long,

    @SerializedName("competitions_id") val competitionsId: Long,
    @SerializedName("start_time_human") val startTimeHuman: String,
    @SerializedName("end_time_human") val endTimeHuman: String
)
