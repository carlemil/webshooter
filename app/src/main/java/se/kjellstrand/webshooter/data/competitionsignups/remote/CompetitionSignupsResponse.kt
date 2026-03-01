package se.kjellstrand.webshooter.data.competitionsignups.remote

import com.google.gson.annotations.SerializedName

data class CompetitionSignupsResponse(
    @SerializedName("signups") val signups: CompetitionSignupsPaged
)

data class CompetitionSignupsPaged(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("data") val data: List<CompetitionSignupEntry>,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("total") val total: Int
)

data class CompetitionSignupEntry(
    val id: Long,
    val user: CompetitionSignupUser,
    val club: CompetitionSignupClub,
    val weaponclass: CompetitionSignupWeaponClass
)

data class CompetitionSignupUser(val name: String, val lastname: String)

data class CompetitionSignupClub(val name: String)

data class CompetitionSignupWeaponClass(
    val classname: String,
    @SerializedName("classname_general") val classnameGeneral: String
)
