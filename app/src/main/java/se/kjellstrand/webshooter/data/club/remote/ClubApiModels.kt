package se.kjellstrand.webshooter.data.club.remote

import com.google.gson.annotations.SerializedName

data class ClubInfoResponse(
    val club: ClubData
)

data class ClubData(
    val id: Long,
    @SerializedName("clubs_nr") val clubsNr: String?,
    val name: String,
    val email: String?,
    val phone: String?,
    @SerializedName("address_street") val addressStreet: String?,
    @SerializedName("address_zipcode") val addressZipcode: String?,
    @SerializedName("address_city") val addressCity: String?,
    @SerializedName("address_country") val addressCountry: String?,
    val bankgiro: String?,
    val postgiro: String?,
    val swish: String?,
    val admins: List<ClubMember> = emptyList(),
    val users: List<ClubMember> = emptyList()
)

data class ClubMember(
    @SerializedName("user_id") val userId: Long,
    val name: String,
    val lastname: String? = null,
    val fullname: String? = null,
    val email: String? = null,
    @SerializedName("shooting_card_number") val shootingCardNumber: String? = null,
    val status: String? = null
)
