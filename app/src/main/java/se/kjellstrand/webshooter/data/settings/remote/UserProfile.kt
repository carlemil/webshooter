package se.kjellstrand.webshooter.data.settings.remote

import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import se.kjellstrand.webshooter.R

enum class Gender(@StringRes val labelRes: Int) {
    UNSET(R.string.select_gender),
    MALE(R.string.male),
    FEMALE(R.string.female);

    val apiValue: String? get() = when (this) {
        MALE -> "male"
        FEMALE -> "female"
        UNSET -> null
    }

    companion object {
        fun fromApiValue(value: String?): Gender = when (value) {
            "male" -> MALE
            "female" -> FEMALE
            else -> UNSET
        }
    }
}

data class UserProfileResponse(
    val user: UserProfile
)

data class UserProfile(
    val name: String,
    val lastname: String,
    val email: String,
    @SerializedName("shooting_card_number") val shootingCardNumber: String?,
    @SerializedName("no_shooting_card_number") val noShootingCardNumber: String?,
    val birthday: String?,
    val gender: String?,
    val phone: String?,
    val mobile: String?,
    @SerializedName("grade_field") val gradeField: String?,
    @SerializedName("grade_trackshooting") val gradeTrackshooting: String?,
    @SerializedName("api_token") val apiToken: String?,
    @SerializedName("user_id") val userId: Long,
    val fullname: String,
    @SerializedName("clubs_id") val clubsId: Long,
    val status: String,
    val clubs: List<Club> = emptyList(),
)

data class Club(
    val id: Long,
    @SerializedName("clubs_nr") val clubsNr: Int?,
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
)

data class UpdatePasswordRequest(
    val current_password: String,
    val password: String,
    val password_confirmation: String
)
