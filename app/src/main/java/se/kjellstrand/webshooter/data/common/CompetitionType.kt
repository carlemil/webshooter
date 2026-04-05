package se.kjellstrand.webshooter.data.common

import com.google.gson.annotations.SerializedName

data class CompetitionType (
    val id: Int,
    val name: String,
) {
    companion object {
        val FALT_TYPE_IDS = setOf(2, 3, 9, 10)
    }
}