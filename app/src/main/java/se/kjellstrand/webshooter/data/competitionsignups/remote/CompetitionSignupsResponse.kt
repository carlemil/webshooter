package se.kjellstrand.webshooter.data.competitionsignups.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompetitionSignupsResponse(
    @SerialName("signups") val signups: CompetitionSignupsPaged
)

@Serializable
data class CompetitionSignupsPaged(
    @SerialName("current_page") val currentPage: Int,
    @SerialName("data") val data: List<CompetitionSignupEntry>,
    @SerialName("last_page") val lastPage: Int,
    @SerialName("total") val total: Int
)

@Serializable
data class CompetitionSignupEntry(
    val id: Long,
    val user: CompetitionSignupUser?,
    val club: CompetitionSignupClub?,
    val weaponclass: CompetitionSignupWeaponClass?
)

@Serializable
data class CompetitionSignupUser(val name: String, val lastname: String)

@Serializable
data class CompetitionSignupClub(val name: String)

@Serializable
data class CompetitionSignupWeaponClass(
    val classname: String,
    @SerialName("classname_general") val classnameGeneral: String
)
