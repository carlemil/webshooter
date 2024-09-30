package se.kjellstrand.webshooter.data.common

import com.google.gson.annotations.SerializedName

data class CompetitionType (
    val id: Long,
    val name: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("deleted_at")
    val deletedAt: Any? = null
)