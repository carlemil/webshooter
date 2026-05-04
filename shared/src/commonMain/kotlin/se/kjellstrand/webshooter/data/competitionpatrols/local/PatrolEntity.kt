package se.kjellstrand.webshooter.data.competitionpatrols.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "patrols",
    primaryKeys = ["id", "competitionId"],
    indices = [Index("competitionId")]
)
data class PatrolEntity(
    val id: Long,
    val competitionId: Long,
    val sortorder: Int,
    val startTimeHuman: String,
    val endTimeHuman: String,
    val signupsJson: String
)
