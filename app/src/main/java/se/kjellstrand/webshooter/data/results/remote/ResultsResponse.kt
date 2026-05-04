package se.kjellstrand.webshooter.data.results.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.WeaponClass

@Serializable
data class ResultsResponse(
    val results: List<Result>
)

@Serializable
data class Result(
    val id: Long,

    @SerialName("signups_id")
    val signupsID: Long,

    val placement: Long,

    @SerialName("figure_hits")
    val figureHits: Long,

    val hits: Long,
    val points: Long,

    @SerialName("std_medal")
    val stdMedal: StdMedal? = null,

    val signup: Signup,

    @SerialName("weaponclass")
    val weaponClass: WeaponClass,
    val results: List<StationResult>
)

@Serializable
data class StationResult(
    val id: Long,

    @SerialName("figure_hits")
    val figureHits: Long,

    val hits: Long,
    val points: Long
)

@Serializable
data class Signup(
    val user: User,
    val club: Club?
)

@Serializable
data class User(
    val name: String,
    val lastname: String,

    @SerialName("user_id")
    val userID: Long,

    val fullname: String
)

@Serializable
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
