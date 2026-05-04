package se.kjellstrand.webshooter.data.common

import kotlinx.serialization.Serializable

@Serializable
data class WeaponGroup (
    val id: Long,
    val name: ClassnameGeneral
)
