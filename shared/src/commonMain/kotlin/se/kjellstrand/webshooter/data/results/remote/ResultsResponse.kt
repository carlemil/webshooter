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

    // The server can return `null` for any of the numeric station fields
    // when a station hasn't been scored yet (mid-competition data, partial
    // patrols, etc.). With `Json { coerceInputValues = true }` configured
    // in `NetworkModule`, these defaults turn null/missing → 0L so the
    // whole results payload still parses.
    @SerialName("figure_hits")
    val figureHits: Long = 0L,

    val hits: Long = 0L,
    val points: Long = 0L,
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
