package se.kjellstrand.webshooter.data.common

import com.google.gson.annotations.SerializedName

data class CompetitionType (
    val id: Int, // 2, 3, 9, 10 <- fält
    val name: String,
)