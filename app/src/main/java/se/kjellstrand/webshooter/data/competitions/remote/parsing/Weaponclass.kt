package se.kjellstrand.webshooter.data.competitions.remote.parsing

import com.beust.klaxon.Json


data class Weaponclass (
    val id: Long,

    @Json(name = "weapongroups_id")
    val weapongroupsID: Long,

    val classname: String,
    val championship: Long,

    @Json(name = "classname_general")
    val classnameGeneral: ClassnameGeneral,

    val pivot: Pivot
)
