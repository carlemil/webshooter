package se.kjellstrand.webshooter.data.competitions.remote.parsing

import com.beust.klaxon.Json

data class Competitiontype (
    val id: Long,
    val name: String,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "updated_at")
    val updatedAt: String,

    @Json(name = "deleted_at")
    val deletedAt: Any? = null
)