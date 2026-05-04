package se.kjellstrand.webshooter.data.common

import kotlinx.serialization.Serializable

@Serializable
data class Club (
    val id: Long,
    val name: String
)