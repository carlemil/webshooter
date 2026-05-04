package se.kjellstrand.webshooter.data.settings.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val user: UserProfile
)

@Serializable
data class UserProfile(
    val name: String,
    val lastname: String,
    val email: String,
    @SerialName("shooting_card_number") val shootingCardNumber: String?,
    @SerialName("no_shooting_card_number") val noShootingCardNumber: String?,
    val birthday: String?,
    val gender: String?,
    val phone: String?,
    val mobile: String?,
    @SerialName("grade_field") val gradeField: String?,
    @SerialName("grade_trackshooting") val gradeTrackshooting: String?,
    @SerialName("api_token") val apiToken: String?,
    @SerialName("user_id") val userId: Long,
    val fullname: String,
    @SerialName("clubs_id") val clubsId: Long,
    val status: String,
    val clubs: List<Club> = emptyList(),
)

@Serializable
data class Club(
    val id: Long,
    @SerialName("clubs_nr") val clubsNr: Int?,
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
)

@Serializable
data class UpdatePasswordRequest(
    val current_password: String,
    val password: String,
    val password_confirmation: String
)
