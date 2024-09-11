package se.kjellstrand.webshooter.data.competitions.remote.parsing

import com.beust.klaxon.Json


data class Usersignup (
    val id: Long,

    @Json(name = "competitions_id")
    val competitionsID: Long,

    @Json(name = "patrols_id")
    val patrolsID: Long,

    @Json(name = "patrols_finals_id")
    val patrolsFinalsID: Long,

    @Json(name = "lane_finals")
    val laneFinals: Long,

    @Json(name = "patrols_distinguish_id")
    val patrolsDistinguishID: Long,

    @Json(name = "lane_distinguish")
    val laneDistinguish: Long,

    @Json(name = "start_time")
    val startTime: String? = null,

    @Json(name = "end_time")
    val endTime: String? = null,

    val lane: Long,

    @Json(name = "weaponclasses_id")
    val weaponclassesID: Long,

    @Json(name = "registration_fee")
    val registrationFee: Long,

    @Json(name = "invoices_id")
    val invoicesID: Any? = null,

    @Json(name = "clubs_id")
    val clubsID: Long,

    @Json(name = "start_before")
    val startBefore: String,

    @Json(name = "start_after")
    val startAfter: String,

    @Json(name = "first_last_patrol")
    val firstLastPatrol: Any? = null,

    @Json(name = "share_weapon_with")
    val shareWeaponWith: Long,

    @Json(name = "participate_out_of_competition")
    val participateOutOfCompetition: Any? = null,

    @Json(name = "exclude_from_standardmedal")
    val excludeFromStandardmedal: Any? = null,

    @Json(name = "share_patrol_with")
    val sharePatrolWith: Long,

    @Json(name = "shoot_not_simultaneously_with")
    val shootNotSimultaneouslyWith: Long,

    val note: String? = null,

    @Json(name = "requires_approval")
    val requiresApproval: Long,

    @Json(name = "is_approved_by")
    val isApprovedBy: Long,

    @Json(name = "created_by")
    val createdBy: Long,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "special_wishes")
    val specialWishes: String,

    @Json(name = "first_last_patrol_human")
    val firstLastPatrolHuman: String,

    @Json(name = "start_time_human")
    val startTimeHuman: String,

    @Json(name = "end_time_human")
    val endTimeHuman: String
)