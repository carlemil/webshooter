package se.kjellstrand.webshooter.data.club.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClubInfoResponse(
    val club: ClubData
)

@Serializable
data class ClubData(
    val id: Long,
    @SerialName("clubs_nr") val clubsNr: String?,
    val name: String,
    val email: String?,
    val phone: String?,
    @SerialName("address_street") val addressStreet: String?,
    @SerialName("address_zipcode") val addressZipcode: String?,
    @SerialName("address_city") val addressCity: String?,
    @SerialName("address_country") val addressCountry: String?,
    val bankgiro: String?,
    val postgiro: String?,
    val swish: String?,
    val admins: List<ClubMember> = emptyList(),
    val users: List<ClubMember> = emptyList()
)

@Serializable
data class ClubMember(
    @SerialName("user_id") val userId: Long,
    val name: String,
    val lastname: String? = null,
    val fullname: String? = null,
    val email: String? = null,
    @SerialName("shooting_card_number") val shootingCardNumber: String? = null,
    val status: String? = null
)
