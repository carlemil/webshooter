package se.kjellstrand.webshooter.data.competitions.remote.parsing

import com.beust.klaxon.Json

data class Pivot (
    @Json(name = "competitions_id")
    val competitionsID: Long,

    @Json(name = "weaponclasses_id")
    val weaponclassesID: Long,

    @Json(name = "registration_fee")
    val registrationFee: Long
)