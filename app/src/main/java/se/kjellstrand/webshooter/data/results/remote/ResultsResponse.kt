package se.kjellstrand.webshooter.data.results.remote

import com.google.gson.annotations.SerializedName
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.WeaponClass

data class ResultsResponse(
    val results: List<Result>
)

data class Result(
    val id: Long,

    @SerializedName("competitions_id")
    val competitionsID: Long,

    @SerializedName("signups_id")
    val signupsID: Long,

    val placement: Long,
    val price: Any? = null,

    @SerializedName("figure_hits")
    val figureHits: Long,

    val hits: Long,
    val points: Long,

    @SerializedName("weaponclasses_id")
    val weaponclassesID: Long,

    @SerializedName("std_medal")
    val stdMedal: StdMedal? = null,

    val signup: Signup,

    @SerializedName("weaponclass")
    val weaponClass: WeaponClass,
    val results: List<StationResult>,

    @SerializedName("results_distinguish")
    val resultsDistinguish: List<Any?>,

    @SerializedName("results_finals")
    val resultsFinals: List<Any?>
)

data class StationResult(
    val id: Long,

    @SerializedName("signups_id")
    val signupsID: Long,

    val finals: Long,
    val distinguish: Long,

    @SerializedName("stations_id")
    val stationsID: Long,

    @SerializedName("figure_hits")
    val figureHits: Long,

    val hits: Long,
    val points: Long,

    @SerializedName("station_figure_hits")
    val stationFigureHits: List<Long>,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("deleted_at")
    val deletedAt: Any? = null
)

data class Signup(
    val id: Long,

    @SerializedName("competitions_id")
    val competitionsID: Long,

    @SerializedName("patrols_id")
    val patrolsID: Long,

    @SerializedName("patrols_finals_id")
    val patrolsFinalsID: Long,

    @SerializedName("lane_finals")
    val laneFinals: Long,

    @SerializedName("patrols_distinguish_id")
    val patrolsDistinguishID: Long,

    @SerializedName("lane_distinguish")
    val laneDistinguish: Long,

    @SerializedName("start_time")
    val startTime: String,

    @SerializedName("end_time")
    val endTime: String,

    val lane: Long,

    @SerializedName("weaponclasses_id")
    val weaponclassesID: Long,

    @SerializedName("registration_fee")
    val registrationFee: Long,

    @SerializedName("invoices_id")
    val invoicesID: Long? = null,

    @SerializedName("clubs_id")
    val clubsID: Long,

    @SerializedName("start_before")
    val startBefore: String,

    @SerializedName("start_after")
    val startAfter: String,

    @SerializedName("first_last_patrol")
    val firstLastPatrol: String? = null,

    @SerializedName("share_weapon_with")
    val shareWeaponWith: Long? = null,

    @SerializedName("participate_out_of_competition")
    val participateOutOfCompetition: Any? = null,

    @SerializedName("exclude_from_standardmedal")
    val excludeFromStandardmedal: Long? = null,

    @SerializedName("share_patrol_with")
    val sharePatrolWith: Long,

    @SerializedName("shoot_not_simultaneously_with")
    val shootNotSimultaneouslyWith: Long,

    val note: String? = null,

    @SerializedName("requires_approval")
    val requiresApproval: Long,

    @SerializedName("is_approved_by")
    val isApprovedBy: Long,

    @SerializedName("created_by")
    val createdBy: Long,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("special_wishes")
    val specialWishes: String,

    @SerializedName("first_last_patrol_human")
    val firstLastPatrolHuman: String,

    @SerializedName("start_time_human")
    val startTimeHuman: String,

    @SerializedName("end_time_human")
    val endTimeHuman: String,

    val user: User,
    val club: Club
)


data class User(
    val name: String,
    val lastname: String,

    @SerializedName("shooting_card_number")
    val shootingCardNumber: String? = null,

    @SerializedName("grade_field")
    val gradeField: String? = null,

    @SerializedName("grade_trackshooting")
    val gradeTrackshooting: String? = null,

    @SerializedName("api_token")
    val apiToken: String,

    @SerializedName("user_id")
    val userID: Long,

    val fullname: String,

    @SerializedName("clubs_id")
    val clubsID: Long,

    val status: Status,
    val clubs: List<Club>
)

enum class Status(val value: String) {
    Active("active"),
    Inactive("inactive"),
    Noaccount("noaccount");

    companion object {
        public fun fromValue(value: String): Status = when (value) {
            "active" -> Active
            "inactive" -> Inactive
            "noaccount" -> Noaccount
            else -> throw IllegalArgumentException()
        }
    }
}

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
