package se.kjellstrand.webshooter.data.results.remote

import com.google.gson.annotations.SerializedName
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.WeaponClass

data class ResultsResponse(
    val results: List<Result>
)

data class Result(
    val id: Long,

    @SerializedName("signups_id")
    val signupsID: Long,

    val placement: Long,

    @SerializedName("figure_hits")
    val figureHits: Long,

    val hits: Long,
    val points: Long,

    @SerializedName("std_medal")
    val stdMedal: StdMedal? = null,

    val signup: Signup,

    @SerializedName("weaponclass")
    val weaponClass: WeaponClass,
    val results: List<StationResult>
)

data class StationResult(
    val id: Long,

    @SerializedName("figure_hits")
    val figureHits: Long,

    val hits: Long,
    val points: Long
)

data class Signup(
    val user: User,
    val club: Club?
)

data class User(
    val name: String,
    val lastname: String,

    @SerializedName("user_id")
    val userID: Long,

    val fullname: String
)

enum class StdMedal(val value: String) {
    B("B"),
    S("S");

    companion object {
        public fun fromValue(value: String): StdMedal = when (value) {
            "B" -> B
            "S" -> S
            else -> throw IllegalArgumentException()
        }
    }
}
