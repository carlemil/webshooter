package se.kjellstrand.webshooter.data.competitionpatrols.local

import androidx.room.Entity

@Entity(tableName = "patrols", primaryKeys = ["id", "competitionId"])
data class PatrolEntity(
    val id: Long,
    val competitionId: Long,
    val sortorder: Int,
    val startTimeHuman: String,
    val endTimeHuman: String,
    val patrolSize: Int,
    val signupsJson: String
)
